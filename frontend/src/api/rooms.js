import { http, toQuery } from './client';

/**
 * GET /api/rooms - 공개 숙소 목록 (인증 불필요)
 * 응답: Page<RoomsDTO.ListItem>
 *   { content: [{ id, name, city, country, weekdayPrice, weekendPrice,
 *                 maxGuests, status, thumbnailUrl, ratingAverage, reviewCount }],
 *     totalPages, totalElements, number, size }
 *
 * keyword(이름/설명/도시/국가 부분일치), guests(정원 이상), checkin+checkout(그 기간에
 * 겹치는 확정 예약이 없는 방만) 전부 서버에서 실제로 필터링합니다. checkin/checkout은
 * 둘 다 있어야 적용되고, 형식이 yyyy-MM-dd가 아니거나 checkout <= checkin이면 400.
 * keyword는 콤마로 여러 개 넘기면 OR로 매치됩니다 (예: "왕릉,능묘" → 둘 중 하나만 있어도 검색됨).
 * 카테고리 동의어 필터링에 사용 (실제 category 컬럼은 없음).
 */
export function getRooms({ keyword, checkin, checkout, guests, page = 0, size = 12 } = {}) {
  return http.get(`/api/rooms${toQuery({ keyword, checkin, checkout, guests, page, size })}`, { auth: false });
}

/** GET /api/rooms/{roomId} - 공개 숙소 상세 */
export function getRoomDetail(roomId) {
  return http.get(`/api/rooms/${roomId}`, { auth: false });
}

/** GET /api/rooms/{roomId}/reviews - 공개 후기 목록 (Page<ReviewResponseDTO>) */
export function getRoomReviews(roomId, { page = 0, size = 10 } = {}) {
  return http.get(`/api/rooms/${roomId}/reviews${toQuery({ page, size })}`, { auth: false });
}
