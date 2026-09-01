package com.nubi.domain.rooms;

import com.nubi.entity.ReviewEntity;
import com.nubi.entity.RoomImagesEntity;
import com.nubi.entity.RoomsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomsService {

    private final RoomsRepository roomsRepository;
    private final ReviewRepository reviewRepository;
    private final RoomImagesRepository roomImagesRepository;

    // 1. GET /rooms
    public Page<RoomsDTO.ListResponse> getRooms(Pageable pageable){
        Page<RoomsEntity> rooms = roomsRepository.findAll(pageable);

        List<Long> roomIds = rooms.getContent().stream().map(RoomsEntity::getId).toList();
        Map<Long, String> thumbnailByRoomId = roomImagesRepository.findByRoom_IdInAndThumbnailTrue(roomIds).stream()
                .collect(Collectors.toMap(image -> image.getRoom().getId(), RoomImagesEntity::getUrl));

        return rooms.map(room -> RoomsDTO.ListResponse.from(room, thumbnailByRoomId.get(room.getId())));
    }

    // 2. GET /rooms/{room_id}
    public RoomsDTO.DetailResponse getRoomDetail(Long roomId){
        RoomsEntity room = roomsRepository.findById(roomId)
            .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "숙소를 찾을 수 없습니다. id=" + roomId));
        List<RoomImagesEntity> images = roomImagesRepository.findByRoom_IdOrderByIdAsc(roomId);
        return RoomsDTO.DetailResponse.from(room, images);
    }

    // 3. GET /rooms/{room_id}/reviews
    public Page<RoomsDTO.ReviewResponse> getRoomReviews(Long roomId, Pageable pageable) {
        if (!roomsRepository.existsById(roomId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "숙소를 찾을 수 없습니다. id=" + roomId);
        }
        Page<ReviewEntity> reviews = reviewRepository.findByRoomId(roomId, pageable);
        return reviews.map(RoomsDTO.ReviewResponse::from);
    }
}
