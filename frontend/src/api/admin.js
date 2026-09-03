import { http, toQuery } from './client';

/* ------------------------------------------------------------------
 * 관리자 API (/api/admin/**)
 *
 * 서버 동작 요약:
 *  - JwtAuthenticationFilter로 "로그인 여부"만 검사합니다.
 *  - 컨트롤러/서비스는 요청자가 소유(owner_id)한 숙소·예약만 필터링해서 내려줍니다.
 *    즉 다른 관리자의 데이터는 서버가 알아서 차단합니다(403/404).
 *  - 서버는 role(USER/ADMIN)까지는 보지 않으므로, "관리자 화면 진입" 자체는
 *    프론트의 AdminRoute가 AccountResponseDTO.role === 'ADMIN' 으로 막습니다.
 * ------------------------------------------------------------------ */

/* ---------------- 숙소 ---------------- */

/**
 * GET /api/admin/rooms - 내가 소유한 숙소 목록 (Page<AdminRoomResponseDTO>)
 * 필터: keyword(이름·도시·국가), checkin/checkout(yyyy-MM-dd, 둘 다 있어야 적용), guests(최대 인원 이상)
 */
export function getAdminRooms({ keyword, checkin, checkout, guests, page = 0, size = 20 } = {}) {
  return http.get(`/api/admin/rooms${toQuery({ keyword, checkin, checkout, guests, page, size })}`);
}

/** GET /api/admin/rooms/{roomId} */
export function getAdminRoomDetail(roomId) {
  return http.get(`/api/admin/rooms/${roomId}`);
}

/** POST /api/admin/rooms - 숙소 등록 */
export function createAdminRoom(payload) {
  return http.post('/api/admin/rooms', payload);
}

/** PATCH /api/admin/rooms/{roomId} - 부분 수정 (보낸 필드만 반영) */
export function updateAdminRoom(roomId, payload) {
  return http.patch(`/api/admin/rooms/${roomId}`, payload);
}

/** DELETE /api/admin/rooms/{roomId} - 숙소 삭제 (사진도 함께 정리) */
export function deleteAdminRoom(roomId) {
  return http.delete(`/api/admin/rooms/${roomId}`);
}

/* ---------------- 숙소 사진 ---------------- */

/** GET /api/admin/rooms/{roomId}/images - [{ id, url, ... }] */
export function getRoomImages(roomId) {
  return http.get(`/api/admin/rooms/${roomId}/images`);
}

/**
 * POST /api/admin/rooms/{roomId}/images
 * multipart/form-data, 필드명은 "images" (여러 장 동시 업로드 가능)
 */
export function uploadRoomImages(roomId, files) {
  const formData = new FormData();
  Array.from(files).forEach((file) => formData.append('images', file));
  return http.upload(`/api/admin/rooms/${roomId}/images`, formData);
}

/** DELETE /api/admin/rooms/{roomId}/images/{imageId} */
export function deleteRoomImage(roomId, imageId) {
  return http.delete(`/api/admin/rooms/${roomId}/images/${imageId}`);
}

/* ---------------- 예약 ---------------- */

/**
 * GET /api/admin/bookings - 내 숙소에 들어온 예약 목록
 * 필터: status(CONFIRMED|COMPLETED|CANCELLED), room_id
 */
export function getAdminBookings({ status, roomId, page = 0, size = 20 } = {}) {
  return http.get(`/api/admin/bookings${toQuery({ status, room_id: roomId, page, size })}`);
}

/** GET /api/admin/bookings/{bookingId} */
export function getAdminBookingDetail(bookingId) {
  return http.get(`/api/admin/bookings/${bookingId}`);
}

/** DELETE /api/admin/bookings/{bookingId} - soft cancel (status -> CANCELLED) */
export function cancelAdminBooking(bookingId) {
  return http.delete(`/api/admin/bookings/${bookingId}`);
}
