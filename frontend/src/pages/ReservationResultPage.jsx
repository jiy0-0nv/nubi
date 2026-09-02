import { Link, Navigate, useLocation } from 'react-router-dom';
import { bookingTotal, formatCurrency, formatDateTime } from '../utils/format';

export default function ReservationResultPage() {
  const location = useLocation();
  const booking = location.state?.booking;
  const room = location.state?.room;

  // 새로고침 등으로 state가 사라졌다면 예약 목록으로 보냅니다.
  if (!booking) return <Navigate to="/mypage/bookings" replace />;

  return (
    <div className="container-narrow page text-center">
      <div className="result-seal" aria-hidden="true">
        †
      </div>
      <p className="eyebrow">Sealed</p>
      <h1>예약이 봉인되었습니다</h1>
      <p className="muted mt-16">
        {room?.name || booking.roomName}에 당신의 이름이 새겨졌습니다.
        <br />
        정해진 날, 정해진 시각에 오르십시오.
      </p>

      <div className="panel mt-40 text-left">
        <div className="price-line">
          <span>예약 번호</span>
          <span className="mono">#{String(booking.id).padStart(6, '0')}</span>
        </div>
        <div className="price-line">
          <span>산장</span>
          <span>{room?.name || booking.roomName || '-'}</span>
        </div>
        <div className="price-line">
          <span>입산</span>
          <span>{formatDateTime(booking.checkInDate)}</span>
        </div>
        <div className="price-line">
          <span>하산</span>
          <span>{formatDateTime(booking.checkOutDate)}</span>
        </div>
        <div className="price-line">
          <span>인원</span>
          <span>{booking.guestCount}명</span>
        </div>
        <div className="price-line total">
          <span>확정 금액</span>
          <span>{formatCurrency(bookingTotal(booking))}</span>
        </div>
      </div>

      <div className="row gap-8 mt-24" style={{ justifyContent: 'center' }}>
        <Link to={`/mypage/bookings/${booking.id}`} className="btn btn-primary">
          예약 상세 보기
        </Link>
        <Link to="/rooms" className="btn btn-outline">
          다른 산장 보기
        </Link>
      </div>
    </div>
  );
}
