package com.nubi.domain.admin.controller;

import com.nubi.domain.admin.dto.AdminRoomCreateRequestDTO;
import com.nubi.domain.admin.dto.AdminRoomImageResponseDTO;
import com.nubi.domain.admin.dto.AdminRoomResponseDTO;
import com.nubi.domain.admin.dto.AdminRoomUpdateRequestDTO;
import com.nubi.domain.admin.service.AdminRoomImagesService;
import com.nubi.domain.admin.service.AdminRoomsService;
import com.nubi.global.exception.UnauthenticatedException;
import com.nubi.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomsController {

    private final AdminRoomsService adminRoomsService;
    private final AdminRoomImagesService adminRoomImagesService;
    private final HttpServletRequest request;

    @GetMapping
    public Page<AdminRoomResponseDTO> getRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String checkin,
            @RequestParam(required = false) String checkout,
            @RequestParam(required = false) Integer guests,
            @PageableDefault(size = 20) Pageable pageable) {
        Long ownerId = requireUserId();
        return adminRoomsService.getRooms(ownerId, keyword, checkin, checkout, guests, pageable);
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

    @GetMapping("/{roomId}/images")
    public List<AdminRoomImageResponseDTO> getRoomImages(@PathVariable Long roomId) {
        Long ownerId = requireUserId();
        return adminRoomImagesService.getImages(ownerId, roomId);
    }

    @PostMapping("/{roomId}/images")
    public List<AdminRoomImageResponseDTO> addRoomImages(@PathVariable Long roomId,
                                                           @RequestParam("images") List<MultipartFile> images) {
        Long ownerId = requireUserId();
        return adminRoomImagesService.addImages(ownerId, roomId, images);
    }

    @DeleteMapping("/{roomId}/images/{imageId}")
    public void deleteRoomImage(@PathVariable Long roomId, @PathVariable Long imageId) {
        Long ownerId = requireUserId();
        adminRoomImagesService.deleteImage(ownerId, roomId, imageId);
    }

    /** 드래그로 바꾼 사진 순서를 저장합니다. body: 새 순서대로 나열한 imageId 배열 */
    @PatchMapping("/{roomId}/images/order")
    public List<AdminRoomImageResponseDTO> reorderRoomImages(@PathVariable Long roomId,
                                                               @RequestBody List<Long> orderedImageIds) {
        Long ownerId = requireUserId();
        return adminRoomImagesService.reorderImages(ownerId, roomId, orderedImageIds);
    }

    /** imageId를 대표 사진으로 지정합니다. */
    @PatchMapping("/{roomId}/images/{imageId}/thumbnail")
    public List<AdminRoomImageResponseDTO> setRoomImageThumbnail(@PathVariable Long roomId, @PathVariable Long imageId) {
        Long ownerId = requireUserId();
        return adminRoomImagesService.setThumbnail(ownerId, roomId, imageId);
    }

    private Long getCurrentUserId() {
        Object userId = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        return userId instanceof Long ? (Long) userId : null;
    }

    private Long requireUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new UnauthenticatedException();
        }
        return userId;
    }
}
