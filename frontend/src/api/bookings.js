import { http, toQuery } from './client';

/** GET /api/bookings - 내 예약 목록 (로그인 필요) */
export function getBookings({ status, page = 0, size = 10 } = {}) {
  return http.get(`/api/bookings${toQuery({ status, page, size })}`);
}

/** GET /api/bookings/{bookingId} - 내 예약 상세 */
export function getBookingDetail(bookingId) {
  return http.get(`/api/bookings/${bookingId}`);
}

/**
 * POST /api/bookings - 예약 생성
 * checkInDate / checkOutDate 는 LocalDateTime 문자열("YYYY-MM-DDTHH:mm:ss")
 */
export function createBooking({ roomId, checkInDate, checkOutDate, guestCount }) {
  return http.post('/api/bookings', { roomId, checkInDate, checkOutDate, guestCount });
}

/** PATCH /api/bookings/{bookingId}/cancel - 예약 취소 (행은 남고 status만 CANCELLED) */
export function cancelBooking(bookingId, reason) {
  return http.patch(`/api/bookings/${bookingId}/cancel`, { reason: reason || '' });
}

/** POST /api/bookings/{bookingId}/review - 이용 완료된 예약에 후기 작성 */
export function createReview(bookingId, { rating, content }) {
  return http.post(`/api/bookings/${bookingId}/review`, { rating, content });
}
