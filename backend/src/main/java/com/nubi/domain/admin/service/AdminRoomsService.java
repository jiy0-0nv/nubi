package com.nubi.domain.admin.service;

import com.nubi.domain.admin.dto.AdminRoomCreateRequestDTO;
import com.nubi.domain.admin.dto.AdminRoomResponseDTO;
import com.nubi.domain.admin.dto.AdminRoomUpdateRequestDTO;
import com.nubi.domain.admin.repository.AdminBookingsRepository;
import com.nubi.domain.admin.repository.AdminRoomsRepository;
import com.nubi.entity.BookingsEntity;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class AdminRoomsService {

    private final AdminRoomsRepository adminRoomsRepository;
    private final AdminBookingsRepository adminBookingsRepository;
    private final RoomImageStorageService roomImageStorageService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<AdminRoomResponseDTO> getRooms(Long ownerId, String keyword, String checkin, String checkout,
                                                Integer guests, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        LocalDate[] checkInOut = parseAvailabilityRange(checkin, checkout);

        return adminRoomsRepository.search(
                ownerId, normalizedKeyword, guests, checkInOut[0], checkInOut[1],
                BookingsEntity.BookingStatus.CANCELLED, pageable
        ).map(AdminRoomResponseDTO::from);
    }

    private LocalDate[] parseAvailabilityRange(String checkin, String checkout) {
        if (checkin == null || checkin.isBlank() || checkout == null || checkout.isBlank()) {
            return new LocalDate[]{null, null};
        }

        LocalDate checkInDate;
        LocalDate checkOutDate;
        try {
            checkInDate = LocalDate.parse(checkin);
            checkOutDate = LocalDate.parse(checkout);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkin/checkout must be in yyyy-MM-dd format");
        }

        if (!checkOutDate.isAfter(checkInDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkout must be after checkin");
        }

        return new LocalDate[]{checkInDate, checkOutDate};
    }

    @Transactional
    public AdminRoomResponseDTO createRoom(Long ownerId, AdminRoomCreateRequestDTO request) {
        validateCreateRequest(request);

        UsersEntity owner = entityManager.getReference(UsersEntity.class, ownerId);

        RoomsEntity room = RoomsEntity.builder()
                .owner(owner)
                .name(request.getName())
                .description(request.getDescription())
                .country(request.getCountry())
                .city(request.getCity())
                .street(request.getStreet())
                .checkinTime(request.getCheckinTime())
                .checkoutTime(request.getCheckoutTime())
                .weekendPrice(request.getWeekendPrice())
                .weekdayPrice(request.getWeekdayPrice())
                .maxGuests(request.getMaxGuests())
                .build();

        RoomsEntity saved = adminRoomsRepository.save(room);
        return AdminRoomResponseDTO.from(saved);
    }

    @Transactional(readOnly = true)
    public AdminRoomResponseDTO getRoomDetail(Long ownerId, Long roomId) {
        RoomsEntity room = getOwnedRoomOrThrow(ownerId, roomId);
        return AdminRoomResponseDTO.from(room);
    }

    @Transactional
    public AdminRoomResponseDTO updateRoom(Long ownerId, Long roomId, AdminRoomUpdateRequestDTO request) {
        validateUpdateRequest(request);

        RoomsEntity room = getOwnedRoomOrThrow(ownerId, roomId);

        RoomsEntity.RoomStatus status = parseStatus(request.getStatus());

        room.update(
                request.getName(),
                request.getDescription(),
                request.getCountry(),
                request.getCity(),
                request.getStreet(),
                request.getCheckinTime(),
                request.getCheckoutTime(),
                request.getWeekendPrice(),
                request.getWeekdayPrice(),
                request.getMaxGuests(),
                status
        );

        return AdminRoomResponseDTO.from(room);
    }

    @Transactional
    public void deleteRoom(Long ownerId, Long roomId) {
        RoomsEntity room = getOwnedRoomOrThrow(ownerId, roomId);

        if (adminBookingsRepository.existsByRoom_Id(roomId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "room has existing bookings and cannot be deleted");
        }

        adminRoomsRepository.delete(room);
        roomImageStorageService.deleteRoomDirectory(roomId);
    }

    private RoomsEntity getOwnedRoomOrThrow(Long ownerId, Long roomId) {
        return adminRoomsRepository.findByIdAndOwnerId(roomId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "room not found"));
    }

    private RoomsEntity.RoomStatus parseStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return RoomsEntity.RoomStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid status");
        }
    }

    private void validateCreateRequest(AdminRoomCreateRequestDTO request) {
        if (isBlank(request.getName())) {
            throw badRequest("name is required");
        }
        if (isBlank(request.getCountry())) {
            throw badRequest("country is required");
        }
        if (isBlank(request.getCity())) {
            throw badRequest("city is required");
        }
        if (isBlank(request.getStreet())) {
            throw badRequest("street is required");
        }
        if (request.getCheckinTime() == null) {
            throw badRequest("checkinTime is required");
        }
        if (request.getCheckoutTime() == null) {
            throw badRequest("checkoutTime is required");
        }
        if (!isPositive(request.getWeekdayPrice())) {
            throw badRequest("weekdayPrice must be a positive value");
        }
        if (!isPositive(request.getWeekendPrice())) {
            throw badRequest("weekendPrice must be a positive value");
        }
        if (request.getMaxGuests() <= 0) {
            throw badRequest("maxGuests must be a positive value");
        }
    }

    private void validateUpdateRequest(AdminRoomUpdateRequestDTO request) {
        if (request.getName() != null && request.getName().isBlank()) {
            throw badRequest("name must not be blank");
        }
        if (request.getCountry() != null && request.getCountry().isBlank()) {
            throw badRequest("country must not be blank");
        }
        if (request.getCity() != null && request.getCity().isBlank()) {
            throw badRequest("city must not be blank");
        }
        if (request.getStreet() != null && request.getStreet().isBlank()) {
            throw badRequest("street must not be blank");
        }
        if (request.getWeekdayPrice() != null && !isPositive(request.getWeekdayPrice())) {
            throw badRequest("weekdayPrice must be a positive value");
        }
        if (request.getWeekendPrice() != null && !isPositive(request.getWeekendPrice())) {
            throw badRequest("weekendPrice must be a positive value");
        }
        if (request.getMaxGuests() != null && request.getMaxGuests() <= 0) {
            throw badRequest("maxGuests must be a positive value");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
