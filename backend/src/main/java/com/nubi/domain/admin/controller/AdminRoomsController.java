package com.nubi.domain.admin.controller;

import com.nubi.domain.admin.dto.AdminRoomCreateRequestDTO;
import com.nubi.domain.admin.dto.AdminRoomImageResponseDTO;
import com.nubi.domain.admin.dto.AdminRoomResponseDTO;
import com.nubi.domain.admin.dto.AdminRoomUpdateRequestDTO;
import com.nubi.domain.admin.service.AdminRoomImagesService;
import com.nubi.domain.admin.service.AdminRoomsService;
import com.nubi.global.exception.UnauthenticatedException;
import com.nubi.security.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "06. 관리자 - 숙소/사진 (Admin Rooms)",
     description = """
             호스트가 **자신이 등록한 숙소**를 관리합니다.
             모든 API 는 토큰 주인이 해당 숙소의 owner 일 때만 동작하며, 남의 숙소에 접근하면 403 입니다.
             """)
@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomsController {

    private final AdminRoomsService adminRoomsService;
    private final AdminRoomImagesService adminRoomImagesService;
    private final HttpServletRequest request;

    @Operation(
            summary = "내 숙소 목록 조회",
            description = "토큰 주인이 owner 인 숙소만 페이지 단위로 조회합니다. 검색 조건은 모두 선택값입니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public Page<AdminRoomResponseDTO> getRooms(
            @Parameter(description = "숙소명 / 지역 검색어", example = "해운대")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "체크인 날짜 (yyyy-MM-dd)", example = "2026-11-10")
            @RequestParam(required = false) String checkin,
            @Parameter(description = "체크아웃 날짜 (yyyy-MM-dd)", example = "2026-11-12")
            @RequestParam(required = false) String checkout,
            @Parameter(description = "투숙 인원", example = "2")
            @RequestParam(required = false) Integer guests,
            @Parameter(description = "페이지 정보 (기본 size=20)")
            @PageableDefault(size = 20) Pageable pageable) {
        Long ownerId = requireUserId();
        return adminRoomsService.getRooms(ownerId, keyword, checkin, checkout, guests, pageable);
    }

    @Operation(
            summary = "숙소 등록",
            description = """
                    새 숙소를 등록합니다. 토큰 주인이 자동으로 owner 가 되며 상태는 `ACTIVE` 로 시작합니다.

                    사진은 이 API 로 등록되지 않습니다. 등록 후 응답의 `id` 로
                    `POST /api/admin/rooms/{roomId}/images` 를 호출해 업로드하세요.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "필수값 누락 또는 값 형식 오류", content = @Content)
    })
    @PostMapping
    public AdminRoomResponseDTO createRoom(@RequestBody AdminRoomCreateRequestDTO request) {
        Long ownerId = requireUserId();
        return adminRoomsService.createRoom(ownerId, request);
    }

    @Operation(summary = "내 숙소 상세 조회", description = "본인 소유 숙소만 조회할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 숙소", content = @Content)
    })
    @GetMapping("/{roomId}")
    public AdminRoomResponseDTO getRoomDetail(
            @Parameter(description = "숙소 ID", example = "1")
            @PathVariable Long roomId) {
        Long ownerId = requireUserId();
        return adminRoomsService.getRoomDetail(ownerId, roomId);
    }

    @Operation(
            summary = "숙소 정보 부분 수정",
            description = """
                    **보낸 필드만 변경되는 부분 수정(PATCH)** 입니다. 생략한 필드는 기존 값이 유지됩니다.

                    노출 여부는 `status` 로 제어합니다. (`active` / `inactive`, 대소문자 무관)
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 숙소", content = @Content)
    })
    @PatchMapping("/{roomId}")
    public AdminRoomResponseDTO updateRoom(
            @Parameter(description = "수정할 숙소 ID", example = "1")
            @PathVariable Long roomId,
            @RequestBody AdminRoomUpdateRequestDTO request) {
        Long ownerId = requireUserId();
        return adminRoomsService.updateRoom(ownerId, roomId, request);
    }

    @Operation(
            summary = "숙소 삭제",
            description = "숙소를 삭제하면 등록된 사진도 함께 정리됩니다. 응답 본문은 없습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 완료", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 숙소", content = @Content)
    })
    @DeleteMapping("/{roomId}")
    public void deleteRoom(
            @Parameter(description = "삭제할 숙소 ID", example = "1")
            @PathVariable Long roomId) {
        Long ownerId = requireUserId();
        adminRoomsService.deleteRoom(ownerId, roomId);
    }

    @Operation(summary = "숙소 사진 목록 조회", description = "해당 숙소에 등록된 사진을 등록 순서대로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 숙소", content = @Content)
    })
    @GetMapping("/{roomId}/images")
    public List<AdminRoomImageResponseDTO> getRoomImages(
            @Parameter(description = "숙소 ID", example = "1")
            @PathVariable Long roomId) {
        Long ownerId = requireUserId();
        return adminRoomImagesService.getImages(ownerId, roomId);
    }

    @Operation(
            summary = "숙소 사진 업로드 (multipart)",
            description = """
                    `multipart/form-data` 로 이미지 파일을 여러 장 한 번에 업로드합니다.

                    - 폼 필드명은 반드시 **`images`** 입니다. 여러 장이면 같은 이름으로 반복해서 담습니다.
                    - 이미지 파일만 허용되며, 그 외 형식은 400 입니다.
                    - 용량 제한: **파일당 10MB / 요청 전체 50MB**. 초과 시 413 `FILE_TOO_LARGE`.
                    - 첫 번째 사진이 대표 사진(`thumbnail: true`)이 되어 공개 목록의 `thumbnailUrl` 로 노출됩니다.
                    - 응답의 `url` 은 `/uploads/...` 경로이며 그대로 이미지 URL 로 사용할 수 있습니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공, 등록된 사진 목록 반환",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AdminRoomImageResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "이미지가 아닌 파일이거나 파일이 비어 있음", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 숙소", content = @Content)
    })
    @PostMapping("/{roomId}/images")
    public List<AdminRoomImageResponseDTO> addRoomImages(
            @Parameter(description = "사진을 추가할 숙소 ID", example = "1")
            @PathVariable Long roomId,
            @Parameter(description = "업로드할 이미지 파일들 (폼 필드명: images)")
            @RequestParam("images") List<MultipartFile> images) {
        Long ownerId = requireUserId();
        return adminRoomImagesService.addImages(ownerId, roomId, images);
    }

    @Operation(
            summary = "숙소 사진 삭제",
            description = "사진 한 장을 삭제합니다. 이미 삭제된 사진을 다시 삭제하면 404 입니다. 응답 본문은 없습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 완료", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 숙소 또는 사진", content = @Content)
    })
    @DeleteMapping("/{roomId}/images/{imageId}")
    public void deleteRoomImage(
            @Parameter(description = "숙소 ID", example = "1")
            @PathVariable Long roomId,
            @Parameter(description = "삭제할 사진 ID", example = "10")
            @PathVariable Long imageId) {
        Long ownerId = requireUserId();
        adminRoomImagesService.deleteImage(ownerId, roomId, imageId);
    }

    /** 드래그로 바꾼 사진 순서를 저장합니다. body: 새 순서대로 나열한 imageId 배열 */
    @PatchMapping("/{roomId}/images/order")
    public List<AdminRoomImageResponseDTO> reorderRoomImages(@PathVariable Long roomId,
                                                               @RequestBody List<Long> orderedImageIds) {
        Long ownerId = requireUserId();
        return adminRoomImagesService.reorderImages(ownerId, roomId, orderedImageIds);
    }

    /** imageId를 대표 사진으로 지정합니다. */
    @PatchMapping("/{roomId}/images/{imageId}/thumbnail")
    public List<AdminRoomImageResponseDTO> setRoomImageThumbnail(@PathVariable Long roomId, @PathVariable Long imageId) {
        Long ownerId = requireUserId();
        return adminRoomImagesService.setThumbnail(ownerId, roomId, imageId);
    }

    private Long getCurrentUserId() {
        Object userId = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        return userId instanceof Long ? (Long) userId : null;
    }

    private Long requireUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new UnauthenticatedException();
        }
        return userId;
    }
}
