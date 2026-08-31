package com.nubi.domain.admin.controller;

import com.nubi.domain.admin.dto.AdminRoomCreateRequestDTO;
import com.nubi.domain.admin.dto.AdminRoomResponseDTO;
import com.nubi.domain.admin.dto.AdminRoomUpdateRequestDTO;
import com.nubi.domain.admin.service.AdminRoomsService;
import com.nubi.global.exception.UnauthenticatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomsController {

    private final AdminRoomsService adminRoomsService;

    @GetMapping
    public Page<AdminRoomResponseDTO> getRooms(@PageableDefault(size = 20) Pageable pageable) {
        Long ownerId = requireUserId();
        return adminRoomsService.getRooms(ownerId, pageable);
    }

    @PostMapping
    public AdminRoomResponseDTO createRoom(@RequestBody AdminRoomCreateRequestDTO request) {
        Long ownerId = requireUserId();
        return adminRoomsService.createRoom(ownerId, request);
    }

    @GetMapping("/{roomId}")
    public AdminRoomResponseDTO getRoomDetail(@PathVariable Long roomId) {
        Long ownerId = requireUserId();
        return adminRoomsService.getRoomDetail(ownerId, roomId);
    }

    @PatchMapping("/{roomId}")
    public AdminRoomResponseDTO updateRoom(@PathVariable Long roomId, @RequestBody AdminRoomUpdateRequestDTO request) {
        Long ownerId = requireUserId();
        return adminRoomsService.updateRoom(ownerId, roomId, request);
    }

    @DeleteMapping("/{roomId}")
    public void deleteRoom(@PathVariable Long roomId) {
        Long ownerId = requireUserId();
        adminRoomsService.deleteRoom(ownerId, roomId);
    }

    private Long getCurrentUserId() {
        return null; // TODO: SecurityContext 등에서 꺼내오기
    }

    private Long requireUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new UnauthenticatedException();
        }
        return userId;
    }
}
