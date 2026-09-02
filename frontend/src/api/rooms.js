import { http, toQuery } from './client';

/**
 * GET /api/rooms - 공개 숙소 목록 (인증 불필요)
 * 응답: Page<RoomsDTO.ListItem>
 *   { content: [{ id, name, city, country, weekdayPrice, weekendPrice,
 *                 maxGuests, status, thumbnailUrl, ratingAverage, reviewCount }],
 *     totalPages, totalElements, number, size }
 *
 * NOTE. 현재 백엔드 RoomsService는 keyword/checkin/checkout/guests 를 받기만 하고
 *       실제 필터링은 아직 안 합니다(페이지네이션만 동작). 서버에 필터가 붙으면
 *       바로 동작하도록 파라미터는 그대로 전달하고, 화면에서도 클라이언트측
 *       보조 필터를 함께 적용합니다.
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
