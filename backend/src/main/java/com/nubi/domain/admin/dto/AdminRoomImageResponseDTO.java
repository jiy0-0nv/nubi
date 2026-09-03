package com.nubi.domain.admin.dto;

import com.nubi.entity.RoomImagesEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "숙소 사진 응답")
@Getter
@NoArgsConstructor
public class AdminRoomImageResponseDTO {

    @Schema(description = "사진 ID. 삭제할 때 사용합니다", example = "10")
    private Long id;

    @Schema(description = "정적 서빙 경로. 앞에 서버 주소를 붙이면 바로 이미지 URL 이 됩니다",
            example = "/uploads/rooms/1/9f3c2a.png")
    private String url;

    @Schema(description = "대표 사진 여부. 공개 목록의 thumbnailUrl 로 노출됩니다", example = "true")
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
