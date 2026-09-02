/* ------------------------------------------------------------------
 * 표시용 포맷터 + 요금 계산.
 * 요금 규칙은 백엔드 BookingsService.calculateTotalPrice() 와 동일하게
 * "금요일/토요일 숙박 = 주말 요금"으로 맞춰두었습니다.
 * (화면에 보여주는 건 어디까지나 예상 금액이고, 확정 금액은 예약 생성 응답 기준)
 * ------------------------------------------------------------------ */

export function formatCurrency(amount) {
  return `₩${Number(amount || 0).toLocaleString('ko-KR')}`;
}

export function formatDate(dateLike) {
  if (!dateLike) return '-';
  const d = new Date(dateLike);
  if (Number.isNaN(d.getTime())) return String(dateLike);
  return d.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
}

export function formatShortDate(dateLike) {
  if (!dateLike) return '-';
  const d = new Date(dateLike);
  if (Number.isNaN(d.getTime())) return String(dateLike);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
}

export function formatDateTime(dateLike) {
  if (!dateLike) return '-';
  const d = new Date(dateLike);
  if (Number.isNaN(d.getTime())) return String(dateLike);
  return d.toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/** LocalTime("15:00:00" 또는 "15:00")을 "15:00"으로 */
export function formatTime(timeLike) {
  if (!timeLike) return '-';
  return String(timeLike).slice(0, 5);
}

/** <input type="date"> 용 "YYYY-MM-DD" */
export function toDateInputValue(dateLike) {
  const d = dateLike ? new Date(dateLike) : new Date();
  if (Number.isNaN(d.getTime())) return '';
  const offset = d.getTimezoneOffset() * 60000;
  return new Date(d.getTime() - offset).toISOString().slice(0, 10);
}

export function addDays(dateStr, days) {
  const d = new Date(`${dateStr}T00:00:00`);
  d.setDate(d.getDate() + days);
  return toDateInputValue(d);
}

/**
 * LocalTime.toString()은 초가 0이면 "15:00"처럼 초를 생략합니다.
 * 서버가 기대하는 LocalDateTime 문자열("YYYY-MM-DDTHH:mm:ss")로 채워줍니다.
 */
export function toLocalDateTimeString(dateStr, timeStr) {
  const [hh = '00', mm = '00', ss = '00'] = String(timeStr || '00:00').split(':');
  return `${dateStr}T${hh.padStart(2, '0')}:${mm.padStart(2, '0')}:${ss.padStart(2, '0')}`;
}

export function calculateNights(checkinStr, checkoutStr) {
  if (!checkinStr || !checkoutStr) return 0;
  const start = new Date(`${checkinStr}T00:00:00`);
  const end = new Date(`${checkoutStr}T00:00:00`);
  const diff = Math.round((end - start) / 86400000);
  return diff > 0 ? diff : 0;
}

export function calculateEstimatedTotal(checkinStr, checkoutStr, weekdayPrice, weekendPrice) {
  const nights = calculateNights(checkinStr, checkoutStr);
  if (!nights) return 0;

  let total = 0;
  const cursor = new Date(`${checkinStr}T00:00:00`);
  for (let i = 0; i < nights; i += 1) {
    const day = cursor.getDay(); // 0=일 ... 5=금, 6=토
    total += Number((day === 5 || day === 6 ? weekendPrice : weekdayPrice) || 0);
    cursor.setDate(cursor.getDate() + 1);
  }
  return total;
}

/* ---------------- 상태 라벨 ---------------- */

const BOOKING_STATUS = {
  CONFIRMED: { label: '봉인 완료', tone: 'confirmed' },
  COMPLETED: { label: '하산 완료', tone: 'completed' },
  CANCELLED: { label: '파기됨', tone: 'cancelled' },
};

export function bookingStatus(status) {
  const key = String(status || '').toUpperCase();
  return BOOKING_STATUS[key] || { label: key || '알 수 없음', tone: 'muted' };
}

const ROOM_STATUS = {
  ACTIVE: { label: '개방중', tone: 'confirmed' },
  INACTIVE: { label: '폐쇄됨', tone: 'cancelled' },
};

export function roomStatus(status) {
  const key = String(status || '').toUpperCase();
  return ROOM_STATUS[key] || { label: key || '-', tone: 'muted' };
}

export function isRoomActive(status) {
  return String(status || '').toUpperCase() === 'ACTIVE';
}

/** 백엔드가 "/uploads/xxx.png" 같은 상대경로를 주므로 그대로 쓰되 빈 값만 걸러냅니다. */
export function imageUrl(url) {
  if (!url) return null;
  return url;
}
