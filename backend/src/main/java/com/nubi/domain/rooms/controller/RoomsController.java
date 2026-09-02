package com.nubi.domain.rooms.controller;

import com.nubi.domain.rooms.dto.RoomsDTO;
import com.nubi.domain.rooms.service.RoomsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomsController {

    private final RoomsService roomsService;

    // 1. GET /rooms
    @GetMapping
    public ResponseEntity<Page<RoomsDTO.ListResponse>> getRooms(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String checkin,
        @RequestParam(required = false) String checkout,
        @RequestParam(required = false) Integer guests,
        Pageable pageable
    ){
        Page<RoomsDTO.ListResponse> rooms = roomsService.getRooms(keyword, guests, pageable);
        return ResponseEntity.ok(rooms);
    }

    // 2. GET /rooms/{room_id}
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomsDTO.DetailResponse> getRoomDetail(@PathVariable Long roomId) {
        RoomsDTO.DetailResponse room = roomsService.getRoomDetail(roomId);
        return ResponseEntity.ok(room);
    }

    // 3. GET /rooms/{room_id}/reviews
    @GetMapping("/{roomId}/reviews")
    public ResponseEntity<Page<RoomsDTO.ReviewResponse>> getRoomReviews(
            @PathVariable Long roomId,
            Pageable pageable
    ) {
        Page<RoomsDTO.ReviewResponse> reviews = roomsService.getRoomReviews(roomId, pageable);
        return ResponseEntity.ok(reviews);
    }
}
