package com.nubi.domain.rooms.service;

import com.nubi.domain.rooms.dto.RoomsDTO;
import com.nubi.domain.rooms.repository.ReviewRepository;
import com.nubi.domain.rooms.repository.RoomImagesRepository;
import com.nubi.domain.rooms.repository.RoomsRepository;
import com.nubi.entity.BookingsEntity;
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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    public Page<RoomsDTO.ListResponse> getRooms(String keyword, String checkin, String checkout,
                                                 Integer guests, Pageable pageable){
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        LocalDate[] checkInOut = parseAvailabilityRange(checkin, checkout);

        Page<RoomsEntity> rooms = roomsRepository.search(
                normalizedKeyword, guests, checkInOut[0], checkInOut[1],
                BookingsEntity.BookingStatus.CANCELLED, pageable);

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
}
