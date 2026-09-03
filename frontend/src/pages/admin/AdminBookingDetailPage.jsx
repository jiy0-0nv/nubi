import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { cancelAdminBooking, getAdminBookingDetail } from '../../api/admin';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';
import Badge from '../../components/Badge';
import { bookingStatus, bookingTotal, formatCurrency, formatDateTime } from '../../utils/format';

export default function AdminBookingDetailPage() {
  const { bookingId } = useParams();

  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    getAdminBookingDetail(bookingId)
      .then(setBooking)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [bookingId]);

  useEffect(load, [load]);

  const cancel = async () => {
    if (!window.confirm('이 예약을 파기할까요? 기록은 남습니다.')) return;
    setBusy(true);
    setError('');
    try {
      await cancelAdminBooking(bookingId);
      setNotice('예약을 파기했습니다.');
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  if (loading) return <Spinner />;
  if (!booking) {
    return (
      <>
        <Alert>{error || '예약을 찾을 수 없습니다.'}</Alert>
        <Link to="/admin/bookings" className="btn btn-ghost">
          ← 예약 관리
        </Link>
      </>
    );
  }

  const status = bookingStatus(booking.status);
  const canCancel = String(booking.status).toUpperCase() === 'CONFIRMED';

  return (
    <>
      <div className="admin-topbar">
        <div>
          <p className="eyebrow">Booking #{String(booking.id).padStart(6, '0')}</p>
          <h1>{booking.room?.name || booking.roomName || '예약 상세'}</h1>
          <div className="mt-8">
            <Badge tone={status.tone}>{status.label}</Badge>
          </div>
        </div>
        <div className="row gap-8">
          <Link to="/admin/bookings" className="btn btn-ghost">
            ← 목록으로
          </Link>
          {canCancel && (
            <button type="button" className="btn btn-danger" onClick={cancel} disabled={busy}>
              {busy ? '파기하는 중…' : '예약 파기'}
            </button>
          )}
        </div>
      </div>

      <Alert>{error}</Alert>
      <Alert tone="success">{notice}</Alert>

      <div className="grid-2" style={{ alignItems: 'start' }}>
        <div className="panel">
          <p className="eyebrow eyebrow-ash">일정</p>
          <div className="price-line">
            <span>입실</span>
            <span>{formatDateTime(booking.checkInDate)}</span>
          </div>
          <div className="price-line">
            <span>퇴실</span>
            <span>{formatDateTime(booking.checkOutDate)}</span>
          </div>
          <div className="price-line">
            <span>인원</span>
            <span>{booking.guestCount}명</span>
          </div>
          {booking.createdAt && (
            <div className="price-line">
              <span>예약 접수</span>
              <span>{formatDateTime(booking.createdAt)}</span>
            </div>
          )}
          <div className="price-line total">
            <span>결제 금액</span>
            <span>{formatCurrency(bookingTotal(booking))}</span>
          </div>
        </div>

        <div className="panel">
          <p className="eyebrow eyebrow-ash">예약자</p>
          <div className="price-line">
            <span>이름</span>
            <span>{booking.guest?.name || booking.guestName || '-'}</span>
          </div>
          <div className="price-line">
            <span>이메일</span>
            <span className="mono">{booking.guest?.email || '-'}</span>
          </div>
          <div className="price-line" style={{ borderBottom: 0 }}>
            <span>연락처</span>
            <span>{booking.guest?.phone || '-'}</span>
          </div>

          {booking.room?.id && (
            <>
              <div className="divider" style={{ margin: '20px 0' }} />
              <p className="eyebrow eyebrow-ash">무덤</p>
              <Link to={`/admin/rooms/${booking.room.id}`} className="link">
                {booking.room.name} 상세로 →
              </Link>
            </>
          )}
        </div>
      </div>

      {booking.cancelReason && (
        <div className="panel mt-24" style={{ borderColor: 'var(--hair-blood)' }}>
          <p className="eyebrow">파기 사유</p>
          <p className="muted" style={{ whiteSpace: 'pre-wrap' }}>
            {booking.cancelReason}
          </p>
        </div>
      )}
    </>
  );
}
