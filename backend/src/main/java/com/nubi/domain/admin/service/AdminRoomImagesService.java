package com.nubi.domain.admin.service;

import com.nubi.domain.admin.dto.AdminRoomImageResponseDTO;
import com.nubi.domain.admin.repository.AdminRoomImagesRepository;
import com.nubi.domain.admin.repository.AdminRoomsRepository;
import com.nubi.entity.RoomImagesEntity;
import com.nubi.entity.RoomsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRoomImagesService {

    private final AdminRoomsRepository adminRoomsRepository;
    private final AdminRoomImagesRepository adminRoomImagesRepository;
    private final RoomImageStorageService roomImageStorageService;

    @Transactional(readOnly = true)
    public List<AdminRoomImageResponseDTO> getImages(Long ownerId, Long roomId) {
        getOwnedRoomOrThrow(ownerId, roomId);
        return adminRoomImagesRepository.findByRoom_IdOrderBySortOrderAscIdAsc(roomId).stream()
                .map(AdminRoomImageResponseDTO::from)
                .toList();
    }

    @Transactional
    public List<AdminRoomImageResponseDTO> addImages(Long ownerId, Long roomId, List<MultipartFile> files) {
        RoomsEntity room = getOwnedRoomOrThrow(ownerId, roomId);

        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one image file is required");
        }

        List<RoomImagesEntity> existing = adminRoomImagesRepository.findByRoom_IdOrderBySortOrderAscIdAsc(roomId);
        boolean assignThumbnail = existing.isEmpty();
        int nextOrder = existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getSortOrder() + 1;

        List<AdminRoomImageResponseDTO> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = roomImageStorageService.store(roomId, file);
            RoomImagesEntity image = RoomImagesEntity.builder()
                    .room(room)
                    .url(url)
                    .thumbnail(assignThumbnail)
                    .sortOrder(nextOrder++)
                    .build();
            assignThumbnail = false;
            saved.add(AdminRoomImageResponseDTO.from(adminRoomImagesRepository.save(image)));
        }
        return saved;
    }

    /** 사진 순서를 통째로 새로 지정합니다. roomId가 가진 모든 이미지 id가 빠짐없이 한 번씩 와야 합니다. */
    @Transactional
    public List<AdminRoomImageResponseDTO> reorderImages(Long ownerId, Long roomId, List<Long> orderedImageIds) {
        getOwnedRoomOrThrow(ownerId, roomId);
        List<RoomImagesEntity> images = adminRoomImagesRepository.findByRoom_IdOrderBySortOrderAscIdAsc(roomId);

        Set<Long> currentIds = images.stream().map(RoomImagesEntity::getId).collect(Collectors.toSet());
        if (orderedImageIds == null || orderedImageIds.size() != images.size()
                || !new HashSet<>(orderedImageIds).equals(currentIds)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order must include every existing image id exactly once");
        }

        Map<Long, RoomImagesEntity> byId = images.stream()
                .collect(Collectors.toMap(RoomImagesEntity::getId, image -> image));
        for (int i = 0; i < orderedImageIds.size(); i++) {
            byId.get(orderedImageIds.get(i)).updateSortOrder(i);
        }

        return adminRoomImagesRepository.findByRoom_IdOrderBySortOrderAscIdAsc(roomId).stream()
                .map(AdminRoomImageResponseDTO::from)
                .toList();
    }

    /** imageId를 이 숙소의 대표 사진으로 지정하고, 나머지는 해제합니다. */
    @Transactional
    public List<AdminRoomImageResponseDTO> setThumbnail(Long ownerId, Long roomId, Long imageId) {
        getOwnedRoomOrThrow(ownerId, roomId);
        List<RoomImagesEntity> images = adminRoomImagesRepository.findByRoom_IdOrderBySortOrderAscIdAsc(roomId);

        boolean found = false;
        for (RoomImagesEntity image : images) {
            boolean isTarget = image.getId().equals(imageId);
            image.markAsThumbnail(isTarget);
            found = found || isTarget;
        }
        if (!found) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "image not found");
        }

        return images.stream().map(AdminRoomImageResponseDTO::from).toList();
    }

    @Transactional
    public void deleteImage(Long ownerId, Long roomId, Long imageId) {
        getOwnedRoomOrThrow(ownerId, roomId);

        RoomImagesEntity image = adminRoomImagesRepository.findByIdAndRoom_Id(imageId, roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "image not found"));

        adminRoomImagesRepository.delete(image);
        roomImageStorageService.delete(image.getUrl());
    }

    private RoomsEntity getOwnedRoomOrThrow(Long ownerId, Long roomId) {
        return adminRoomsRepository.findByIdAndOwnerId(roomId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "room not found"));
    }
}
