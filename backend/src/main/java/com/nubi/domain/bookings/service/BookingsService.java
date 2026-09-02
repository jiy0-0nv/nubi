package com.nubi.domain.bookings.service;

import com.nubi.domain.bookings.dto.*;
import com.nubi.domain.bookings.repository.BookingsRepository;
import com.nubi.domain.rooms.repository.ReviewRepository;
import com.nubi.domain.rooms.repository.RoomsRepository;
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
    private final RoomsRepository roomsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    //  1) 목록 조회
    public Page<BookingsResponseDTO> getBookings(Long userId, BookingsEntity.BookingStatus status, Pageable pageable) {
        Page<BookingsEntity> bookings = (status == null)
                ? bookingsRepository.findByUserId(userId, pageable)
                : bookingsRepository.findByUserIdAndStatus(userId, status, pageable);

        return bookings.map(BookingsResponseDTO::from);
    }

    // 2) 예약 생성 (POST /bookings)
    @Transactional
    public BookingsResponseDTO createBooking(Long userId, BookingCreateRequestDTO request) {
        // 같은 방에 대한 동시 예약 생성 요청은 이 트랜잭션이 끝날 때까지 여기서 대기하게 되어, 아래 겹침 체크+저장이 직렬화된다.

        RoomsEntity room = roomsRepository.findByIdForUpdate(request.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "방 존재하지 않음"));

        // 인원수는 방 최대 정원을 넘을 수 없음
        if (request.getGuestCount() > room.getMaxGuests()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "예약인원이 방의 최대인원 수 초과");
        }

        // 체크아웃이 체크인보다 뒤여야 함
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "체크아웃이 체크인보다 뒤여야 함");
        }

        // 같은 방·기간에 취소되지 않은 예약이 이미 있으면 생성 거부
        boolean overlapping = bookingsRepository.existsOverlappingBooking(
                room.getId(), request.getCheckInDate(), request.getCheckOutDate(),
                BookingsEntity.BookingStatus.CANCELLED);
        if (overlapping) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 예약된 방");
        }

        // getReference()는 실제 DB round-trip 없이 FK
        UsersEntity userRef = entityManager.getReference(UsersEntity.class, userId);

        BigDecimal totalPrice = calculateTotalPrice(room, request.getCheckInDate(), request.getCheckOutDate());

        BookingsEntity booking = BookingsEntity.builder()
                .user(userRef)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .guestCount(request.getGuestCount())
                .totalPrice(totalPrice)
                .build();

        BookingsEntity saved = bookingsRepository.save(booking);
        return BookingsResponseDTO.from(saved);
    }

    // 1박당 요금 합산. //이거 내일 할거
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

    // 3) 예약 상세 조회 (GET /mypage/bookings/{booking_id})
    public BookingsResponseDTO getBookingDetail(Long userId, Long bookingId) {
        BookingsEntity booking = getOwnedBookingOrThrow(userId, bookingId);
        return BookingsResponseDTO.from(booking);
    }

    //  4) 예약 취소 (PATCH /mypage/bookings/{booking_id}/cancel)
    // reason은 요청으로 받지만 저장할 컬럼이 없어서 현재는 버림 — 필요해지면
    // BookingsEntity에 컬럼을 추가하고 업데이트 쿼리에 같이 넘기면 됨.
    @Transactional
    public BookingsResponseDTO cancelBooking(Long userId, Long bookingId, BookingCancelRequestDTO request) {
        BookingsEntity booking = getOwnedBookingOrThrow(userId, bookingId);

        // 이미 취소됐거나 완료된 예약은 다시 취소할 수 없음
        if (booking.getStatus() != BookingsEntity.BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "확정된 예약만 취소 가능");
        }

        // 엔티티에 세터가 없어서 벌크 업데이트로 상태 변경
        bookingsRepository.updateCancelledStatus(bookingId, BookingsEntity.BookingStatus.CANCELLED, LocalDateTime.now());

        BookingsEntity updated = bookingsRepository.findById(bookingId).orElseThrow();
        return BookingsResponseDTO.from(updated);
    }

    //공통: 예약 조회 + 소유권 검증
    private BookingsEntity getOwnedBookingOrThrow(Long userId, Long bookingId) {
        BookingsEntity booking = bookingsRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약 없음"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your booking");
        }
        return booking;
    }

    private final ReviewRepository reviewRepository;

    //리뷰 작성(Post/bookgs/{bookingId}/review)
    @Transactional
    public ReviewResponseDTO createReview(Long userId, Long bookingId, ReviewCreateRequestDTO request) {
        BookingsEntity booking = getOwnedBookingOrThrow(userId, bookingId);

        if (booking.getStatus() != BookingsEntity.BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "확정된 예약만 취소 가능");
        }
        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 작성된 리뷰");
        }

        ReviewEntity review = ReviewEntity.builder()
                .booking(booking)
                .user(booking.getUser())
                .room(booking.getRoom())
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        ReviewEntity saved = reviewRepository.save(review);

        Double average = reviewRepository.findAvgRatingForRoom(booking.getRoom().getId());
        roomsRepository.updateRatingAverage(booking.getRoom().getId(), average != null ? average : 0.0);

        return ReviewResponseDTO.from(saved);
    }
}
