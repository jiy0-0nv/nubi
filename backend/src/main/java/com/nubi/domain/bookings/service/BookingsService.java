package com.nubi.domain.bookings.service;

import com.nubi.domain.bookings.dto.BookingCancelRequestDTO;
import com.nubi.domain.bookings.dto.BookingCreateRequestDTO;
import com.nubi.domain.bookings.dto.BookingsResponseDTO;
import com.nubi.domain.bookings.dto.ReviewCreateRequestDTO;
import com.nubi.domain.bookings.dto.ReviewResponseDTO;
import com.nubi.domain.bookings.repository.BookingsRepository;
import com.nubi.domain.rooms.ReviewRepository;
import com.nubi.entity.BookingsEntity;
import com.nubi.entity.ReviewEntity;
import com.nubi.entity.RoomsEntity;
import com.nubi.entity.UsersEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingsService {

    private final BookingsRepository bookingsRepository;
    private final ReviewRepository reviewRepository;

    // Rooms/Account 도메인에 아직 제대로 된 Repository가 없어서(빈 스텁 클래스),
    // 이번 작업을 Bookings 폴더 안으로만 한정하기 위해 EntityManager로 직접 조회/참조한다.
    @PersistenceContext
    private EntityManager entityManager;

    // ---------- 1) 목록 조회 (기존과 동일, 변경 없음) ----------
    public Page<BookingsResponseDTO> getBookings(Long userId, BookingsEntity.BookingStatus status, Pageable pageable) {
        Page<BookingsEntity> bookings = (status == null)
                ? bookingsRepository.findByUserId(userId, pageable)
                : bookingsRepository.findByUserIdAndStatus(userId, status, pageable);

        return bookings.map(BookingsResponseDTO::from);
    }

    // ---------- 2) 예약 생성 (POST /bookings) ----------
    // 스펙상 "결제 성공 시점에만 row 생성"이므로, 이 메서드 자체가 결제 성공 콜백이
    // 호출하는 지점이라고 가정한다. 별도 결제 검증 로직은 없음(Payment 도메인이 없음).
    @Transactional
    public BookingsResponseDTO createBooking(Long userId, BookingCreateRequestDTO request) {
        // 방이 실제로 존재하는지 확인. find()는 실제 SELECT를 날려서 null 또는 엔티티를 준다
        // (getMaxGuests/가격 계산에 실값이 필요해서 프록시가 아니라 진짜 조회가 필요함).
        RoomsEntity room = entityManager.find(RoomsEntity.class, request.getRoomId());
        if (room == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "room not found");
        }

        // 인원수는 방 최대 정원을 넘을 수 없음
        if (request.getGuestCount() > room.getMaxGuests()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "guestCount exceeds room's maxGuests");
        }

        // 체크아웃이 체크인보다 뒤여야 함
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkOutDate must be after checkInDate");
        }

        // 같은 방·기간에 취소되지 않은 예약이 이미 있으면 생성 거부
        boolean overlapping = bookingsRepository.existsOverlappingBooking(
                room.getId(), request.getCheckInDate(), request.getCheckOutDate(),
                BookingsEntity.BookingStatus.CANCELLED);
        if (overlapping) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "room already booked for this period");
        }

        // getReference()는 실제 DB round-trip 없이 FK로만 쓸 프록시를 준다.
        // userId는 인증 단계에서 이미 유효성이 보장된다고 가정(기존 getBookings와 동일한 전제).
        UsersEntity userRef = entityManager.getReference(UsersEntity.class, userId);

        BigDecimal totalPrice = calculateTotalPrice(room, request.getCheckInDate(), request.getCheckOutDate());

        BookingsEntity booking = BookingsEntity.builder()
                .user(userRef)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .guestCount(request.getGuestCount())
                .totalPrice(totalPrice)
                .build(); // status는 엔티티 생성자에서 기본 CONFIRMED로 설정됨

        BookingsEntity saved = bookingsRepository.save(booking);
        return BookingsResponseDTO.from(saved);
    }

    // 1박당 요금 합산. 금/토를 주말 요금으로 간주 — 실제 정책 확정되면 조정 필요.
    private BigDecimal calculateTotalPrice(RoomsEntity room, LocalDateTime checkIn, LocalDateTime checkOut) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate date = checkIn.toLocalDate();
        LocalDate endDate = checkOut.toLocalDate();

        while (date.isBefore(endDate)) {
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.FRIDAY || date.getDayOfWeek() == DayOfWeek.SATURDAY;
            total = total.add(isWeekend ? room.getWeekendPrice() : room.getWeekdayPrice());
            date = date.plusDays(1);
        }
        return total;
    }

    // ---------- 3) 예약 상세 조회 (GET /mypage/bookings/{booking_id}) ----------
    public BookingsResponseDTO getBookingDetail(Long userId, Long bookingId) {
        BookingsEntity booking = getOwnedBookingOrThrow(userId, bookingId);
        return BookingsResponseDTO.from(booking);
    }

    // ---------- 4) 예약 취소 (PATCH /mypage/bookings/{booking_id}/cancel) ----------
    // reason은 요청으로 받지만 저장할 컬럼이 없어서 현재는 버림 — 필요해지면
    // BookingsEntity에 컬럼을 추가하고 업데이트 쿼리에 같이 넘기면 됨.
    @Transactional
    public BookingsResponseDTO cancelBooking(Long userId, Long bookingId, BookingCancelRequestDTO request) {
        BookingsEntity booking = getOwnedBookingOrThrow(userId, bookingId);

        // 이미 취소됐거나 완료된 예약은 다시 취소할 수 없음
        if (booking.getStatus() != BookingsEntity.BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "only CONFIRMED bookings can be cancelled");
        }

        // 엔티티에 세터가 없어서 벌크 업데이트로 상태 변경 (clearAutomatically=true라
        // 아래 findById가 영속성 컨텍스트 캐시가 아니라 DB에서 새로 읽어옴)
        bookingsRepository.updateCancelledStatus(bookingId, BookingsEntity.BookingStatus.CANCELLED, LocalDateTime.now());

        BookingsEntity updated = bookingsRepository.findById(bookingId).orElseThrow();
        return BookingsResponseDTO.from(updated);
    }

    // ---------- 5) 리뷰 작성 (POST /mypage/bookings/{booking_id}/review) ----------
    @Transactional
    public ReviewResponseDTO createReview(Long userId, Long bookingId, ReviewCreateRequestDTO request) {
        BookingsEntity booking = getOwnedBookingOrThrow(userId, bookingId);

        // 완료된 예약에만 리뷰를 쓸 수 있음
        if (booking.getStatus() != BookingsEntity.BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "only COMPLETED bookings can be reviewed");
        }

        // 예약당 리뷰 1개만 허용 (ReviewEntity.booking이 unique 컬럼이라 DB에서도 막히지만,
        // 미리 체크해서 더 명확한 에러 메시지를 준다)
        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "review already exists for this booking");
        }

        // user/room은 booking에 이미 로딩된 연관관계를 재사용 (추가 조회 불필요)
        ReviewEntity review = ReviewEntity.builder()
                .booking(booking)
                .user(booking.getUser())
                .room(booking.getRoom())
                .rating(request.getRating()) // 1~5 범위 검증은 ReviewEntity 생성자 내부에서 이미 함
                .content(request.getContent())
                .build();

        ReviewEntity saved = reviewRepository.save(review);
        return ReviewResponseDTO.from(saved);
    }

    // ---------- 공통: 예약 조회 + 소유권 검증 ----------
    private BookingsEntity getOwnedBookingOrThrow(Long userId, Long bookingId) {
        BookingsEntity booking = bookingsRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your booking");
        }
        return booking;
    }
}
