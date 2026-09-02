import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { getRoomDetail } from '../api/rooms';
import { createBooking } from '../api/bookings';
import { useAuth } from '../context/AuthContext';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';
import {
  calculateEstimatedTotal,
  calculateNights,
  formatCurrency,
  formatDate,
  formatTime,
  toLocalDateTimeString,
} from '../utils/format';

export default function ReservationPage() {
  const { roomId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { profile } = useAuth();

  const checkin = searchParams.get('checkin') || '';
  const checkout = searchParams.get('checkout') || '';
  const guests = Number(searchParams.get('guests') || 1);

  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [agreed, setAgreed] = useState(false);

  useEffect(() => {
    getRoomDetail(roomId)
      .then(setRoom)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [roomId]);

  const nights = calculateNights(checkin, checkout);
  const estimate = useMemo(
    () => (room ? calculateEstimatedTotal(checkin, checkout, room.weekdayPrice, room.weekendPrice) : 0),
    [room, checkin, checkout]
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (nights <= 0) {
      setError('날짜가 올바르지 않습니다. 산장 상세 화면에서 다시 선택해 주십시오.');
      return;
    }

    setSubmitting(true);
    try {
      // 서버는 LocalDateTime을 기대하므로 숙소의 체크인/체크아웃 시각을 붙여 보냅니다.
      const booking = await createBooking({
        roomId: Number(roomId),
        checkInDate: toLocalDateTimeString(checkin, room.checkinTime || '15:00:00'),
        checkOutDate: toLocalDateTimeString(checkout, room.checkoutTime || '11:00:00'),
        guestCount: guests,
      });
      navigate('/booking/result', { replace: true, state: { booking, room } });
    } catch (err) {
      setError(err.message || '예약에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Spinner label="확인하는 중" />;
  if (!room) {
    return (
      <div className="container page">
        <Alert>{error || '산장을 찾을 수 없습니다.'}</Alert>
      </div>
    );
  }

  return (
    <div className="container page" style={{ maxWidth: 880 }}>
      <p className="eyebrow">Final Confirmation</p>
      <h1>입산 서약</h1>
      <div className="rule mb-24" />

      <Alert>{error}</Alert>

      <div className="panel mb-24">
        <div className="row-between mb-16">
          <div>
            <p className="tiny dim">{[room.country, room.city].filter(Boolean).join(' · ')}</p>
            <h2 className="serif">{room.name}</h2>
          </div>
        </div>

        <div className="spec-list">
          <div className="spec">
            <p className="spec-label">입산</p>
            <p className="spec-value">{formatDate(checkin)}</p>
            <p className="tiny dim mt-8">{formatTime(room.checkinTime)} 이후</p>
          </div>
          <div className="spec">
            <p className="spec-label">하산</p>
            <p className="spec-value">{formatDate(checkout)}</p>
            <p className="tiny dim mt-8">{formatTime(room.checkoutTime)} 까지</p>
          </div>
          <div className="spec">
            <p className="spec-label">머무는 밤</p>
            <p className="spec-value">{nights}박</p>
          </div>
          <div className="spec">
            <p className="spec-label">인원</p>
            <p className="spec-value">{guests}명</p>
          </div>
        </div>
      </div>

      <div className="grid-2" style={{ alignItems: 'start' }}>
        <div className="panel">
          <p className="eyebrow eyebrow-ash">예약자</p>
          <div className="price-line">
            <span>이름</span>
            <span className="nowrap">{profile?.name || '-'}</span>
          </div>
          <div className="price-line">
            <span>이메일</span>
            <span className="nowrap mono">{profile?.email || '-'}</span>
          </div>
          <div className="price-line" style={{ borderBottom: 0 }}>
            <span>연락처</span>
            <span className="nowrap">{profile?.phone || '-'}</span>
          </div>
        </div>

        <div className="panel">
          <p className="eyebrow eyebrow-ash">요금</p>
          <div className="price-line">
            <span>
              {nights}박 · 평일 {formatCurrency(room.weekdayPrice)} / 주말 {formatCurrency(room.weekendPrice)}
            </span>
          </div>
          <div className="price-line total">
            <span>예상 합계</span>
            <span>{formatCurrency(estimate)}</span>
          </div>
          <p className="tiny dim mt-8">금·토 숙박에는 주말 요금이 적용됩니다.</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="mt-24">
        <label className="row gap-8" style={{ alignItems: 'flex-start', cursor: 'pointer', marginBottom: 20 }}>
          <input
            type="checkbox"
            checked={agreed}
            onChange={(e) => setAgreed(e.target.checked)}
            style={{ width: 16, height: 16, marginTop: 4, accentColor: 'var(--blood-bright)' }}
          />
          <span className="tiny muted">
            해가 진 뒤에는 산장 밖으로 나가지 않겠습니다. 방 안에서 들리는 소리에 대답하지 않겠습니다.
            이 예약의 모든 기록이 산장에 남는 것에 동의합니다.
          </span>
        </label>

        <div className="row gap-8">
          <button type="submit" className="btn btn-primary btn-lg" disabled={submitting || !agreed}>
            {submitting ? '봉인하는 중…' : '예약 확정하기'}
          </button>
          <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>
            돌아가기
          </button>
        </div>
      </form>
    </div>
  );
}
