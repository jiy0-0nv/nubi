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

    /** 사진 정렬 순서 (호스트가 드래그로 바꿀 수 있음). 낮을수록 앞에 나옵니다. */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    public RoomImagesEntity(RoomsEntity room, String url, boolean thumbnail, int sortOrder) {
        this.room = room;
        this.url = url;
        this.thumbnail = thumbnail;
        this.sortOrder = sortOrder;
    }

    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void markAsThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;
    }
}
