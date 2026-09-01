package com.nubi.domain.admin.service;

import com.nubi.domain.admin.dto.AdminRoomCreateRequestDTO;
import com.nubi.domain.admin.dto.AdminRoomResponseDTO;
import com.nubi.domain.admin.dto.AdminRoomUpdateRequestDTO;
import com.nubi.domain.admin.repository.AdminRoomsRepository;
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

@Service
@RequiredArgsConstructor
public class AdminRoomsService {

    private final AdminRoomsRepository adminRoomsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<AdminRoomResponseDTO> getRooms(Long ownerId, Pageable pageable) {
        return adminRoomsRepository.findByOwnerId(ownerId, pageable).map(AdminRoomResponseDTO::from);
    }

    @Transactional
    public AdminRoomResponseDTO createRoom(Long ownerId, AdminRoomCreateRequestDTO request) {
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
        adminRoomsRepository.delete(room);
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
}
