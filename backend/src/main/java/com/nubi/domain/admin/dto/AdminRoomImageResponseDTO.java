package com.nubi.domain.admin.dto;

import com.nubi.entity.RoomImagesEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminRoomImageResponseDTO {

    private Long id;
    private String url;
    private boolean thumbnail;

    @Builder
    public AdminRoomImageResponseDTO(Long id, String url, boolean thumbnail) {
        this.id = id;
        this.url = url;
        this.thumbnail = thumbnail;
    }

    public static AdminRoomImageResponseDTO from(RoomImagesEntity image) {
        return AdminRoomImageResponseDTO.builder()
                .id(image.getId())
                .url(image.getUrl())
                .thumbnail(image.isThumbnail())
                .build();
    }
}
