import { http } from './client';

/**
 * POST /api/mypage
 * 프로필 + 최근 예약 + 북마크 + 내가 쓴 리뷰를 한 번에 내려줍니다.
 * 응답: MypageResponseDTO { profile, bookings, bookmarks, reviews }
 */
export function getMypage() {
  return http.post('/api/mypage', undefined);
}
