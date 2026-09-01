package com.nubi.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room_images")
@Getter
@NoArgsConstructor
public class RoomImagesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomsEntity room;

    @Column(nullable = false)
    private String url;

    @Column(name = "is_thumbnail", nullable = false)
    private boolean thumbnail;

    @Builder
    public RoomImagesEntity(RoomsEntity room, String url, boolean thumbnail) {
        this.room = room;
        this.url = url;
        this.thumbnail = thumbnail;
    }
}
