package com.nubi.domain.mypage.dto;

import com.nubi.domain.account.dto.AccountResponseDTO;
import com.nubi.domain.bookings.dto.ReviewResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "마이페이지 통합 응답. 프로필·예약·북마크·리뷰를 한 번에 담습니다")
@Getter
@NoArgsConstructor
public class MypageResponseDTO {

    @Schema(description = "내 계정 정보")
    private AccountResponseDTO profile;

    @Schema(description = "내 예약 내역 요약 목록")
    private List<BookingsSummaryDTO> bookings;

    @Schema(description = "내가 찜한 숙소 목록")
    private List<BookmarkSummaryDTO> bookmarks;

    @Schema(description = "내가 작성한 리뷰 목록")
    private List<ReviewResponseDTO> reviews;

    @Builder
    public MypageResponseDTO(AccountResponseDTO profile, List<BookingsSummaryDTO> bookings,
                             List<BookmarkSummaryDTO> bookmarks, List<ReviewResponseDTO> reviews) {
        this.profile = profile;
        this.bookings = bookings;
        this.bookmarks = bookmarks;
        this.reviews = reviews;
    }
}
