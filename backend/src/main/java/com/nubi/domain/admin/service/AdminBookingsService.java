package com.nubi.domain.admin.service;

import com.nubi.domain.admin.dto.AdminBookingResponseDTO;
import com.nubi.domain.admin.repository.AdminBookingsRepository;
import com.nubi.entity.BookingsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminBookingsService {

    private final AdminBookingsRepository adminBookingsRepository;

    @Transactional(readOnly = true)
    public Page<AdminBookingResponseDTO> getBookings(Long ownerId, BookingsEntity.BookingStatus status,
                                                       Long roomId, Pageable pageable) {
        return adminBookingsRepository.findByRoomOwnerId(ownerId, status, roomId, pageable)
                .map(AdminBookingResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public AdminBookingResponseDTO getBookingDetail(Long ownerId, Long bookingId) {
        BookingsEntity booking = getOwnedBookingOrThrow(ownerId, bookingId);
        return AdminBookingResponseDTO.from(booking);
    }

    // 스펙상 "삭제"지만 예약 row는 남기고 취소 처리한다 (사용자 쪽 취소와 동일한 방식) —
    // 감사/환불 기록을 남기고, 이미 완료·취소된 예약은 다시 취소할 수 없게 막기 위함.
    @Transactional
    public void cancelBooking(Long ownerId, Long bookingId) {
        BookingsEntity booking = getOwnedBookingOrThrow(ownerId, bookingId);

        if (booking.getStatus() != BookingsEntity.BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "only CONFIRMED bookings can be cancelled");
        }

        adminBookingsRepository.updateCancelledStatus(bookingId, BookingsEntity.BookingStatus.CANCELLED, LocalDateTime.now());
    }

    private BookingsEntity getOwnedBookingOrThrow(Long ownerId, Long bookingId) {
        return adminBookingsRepository.findByIdAndRoom_Owner_Id(bookingId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "booking not found"));
    }
}
