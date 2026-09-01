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
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRoomImagesService {

    private final AdminRoomsRepository adminRoomsRepository;
    private final AdminRoomImagesRepository adminRoomImagesRepository;
    private final RoomImageStorageService roomImageStorageService;

    @Transactional(readOnly = true)
    public List<AdminRoomImageResponseDTO> getImages(Long ownerId, Long roomId) {
        getOwnedRoomOrThrow(ownerId, roomId);
        return adminRoomImagesRepository.findByRoom_IdOrderByIdAsc(roomId).stream()
                .map(AdminRoomImageResponseDTO::from)
                .toList();
    }

    @Transactional
    public List<AdminRoomImageResponseDTO> addImages(Long ownerId, Long roomId, List<MultipartFile> files) {
        RoomsEntity room = getOwnedRoomOrThrow(ownerId, roomId);

        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one image file is required");
        }

        boolean assignThumbnail = !adminRoomImagesRepository.existsByRoom_Id(roomId);

        List<AdminRoomImageResponseDTO> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = roomImageStorageService.store(roomId, file);
            RoomImagesEntity image = RoomImagesEntity.builder()
                    .room(room)
                    .url(url)
                    .thumbnail(assignThumbnail)
                    .build();
            assignThumbnail = false;
            saved.add(AdminRoomImageResponseDTO.from(adminRoomImagesRepository.save(image)));
        }
        return saved;
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
