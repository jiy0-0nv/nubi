package com.nubi.domain.rooms;

import com.nubi.entity.ReviewEntity;
import com.nubi.entity.RoomsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomsService {

    private final RoomsRepository roomsRepository;
    private final ReviewRepository reviewRepository;

    // 1. GET /rooms
    public Page<RoomsDTO.ListResponse> getRooms(Pageable pageable){
        Page<RoomsEntity> rooms = roomsRepository.findAll(pageable);
        return rooms.map(RoomsDTO.ListResponse::from);
    }

    // 2. GET /rooms/{room_id}
    public RoomsDTO.DetailResponse getRoomDetail(Long roomId){
        RoomsEntity room = roomsRepository.findById(roomId)
            .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "숙소를 찾을 수 없습니다. id=" + roomId));
        return RoomsDTO.DetailResponse.from(room);
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
