import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { cancelBooking, createReview, getBookingDetail } from '../api/bookings';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';
import Badge from '../components/Badge';
import StarRating from '../components/StarRating';
import { bookingStatus, bookingTotal, formatCurrency, formatDate, formatDateTime } from '../utils/format';

export default function BookingDetailPage() {
  const { bookingId } = useParams();

  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [busy, setBusy] = useState(false);

  // 취소
  const [cancelOpen, setCancelOpen] = useState(false);
  const [reason, setReason] = useState('');

  // 후기
  const [rating, setRating] = useState(5);
  const [content, setContent] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    getBookingDetail(bookingId)
      .then(setBooking)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [bookingId]);

  useEffect(load, [load]);

  // 안내 메시지는 계속 떠 있으면 거슬리니 잠깐 보여주고 스스로 사라집니다.
  useEffect(() => {
    if (!notice) return;
    const timer = setTimeout(() => setNotice(''), 2600);
    return () => clearTimeout(timer);
  }, [notice]);

  const handleCancel = async () => {
    setError('');
    setBusy(true);
    try {
      await cancelBooking(bookingId, reason);
      setNotice('예약을 취소했습니다. 기록은 남습니다.');
      setCancelOpen(false);
      setReason('');
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const handleReview = async (e) => {
    e.preventDefault();
    setError('');
    if (!content.trim()) {
      setError('리뷰 내용을 적어주십시오.');
      return;
    }
    setBusy(true);
    try {
      await createReview(bookingId, { rating, content: content.trim() });
      setNotice('리뷰를 남겼습니다.');
      setContent('');
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
      <div className="container page">
        <Alert>{error || '예약을 찾을 수 없습니다.'}</Alert>
      </div>
    );
  }

  const status = bookingStatus(booking.status);
  const upper = String(booking.status || '').toUpperCase();
  const canCancel = upper === 'CONFIRMED';
  const canReview = upper === 'COMPLETED' && !booking.review;

  return (
    <div className="container-narrow page">
      <Link to="/mypage/bookings" className="link tiny">
        ← 예약 내역
      </Link>

      <div className="row-between mt-16 mb-24">
        <div>
          <p className="eyebrow">#{String(booking.id).padStart(6, '0')}</p>
          <h1 style={{ fontSize: 27 }}>{booking.roomName || booking.room?.name}</h1>
        </div>
        <Badge tone={status.tone}>{status.label}</Badge>
      </div>

      <Alert>{error}</Alert>
      <Alert tone="success">{notice}</Alert>

      <div className="panel mb-24">
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
            <span>예약일</span>
            <span>{formatDateTime(booking.createdAt)}</span>
          </div>
        )}
        <div className="price-line total">
          <span>결제 금액</span>
          <span>{formatCurrency(bookingTotal(booking))}</span>
        </div>
      </div>

      {/* ---------- 취소 ---------- */}
      {canCancel && (
        <div className="panel mb-24" style={{ borderColor: 'var(--hair-blood)' }}>
          <p className="eyebrow">예약 취소</p>
          {cancelOpen ? (
            <>
              <div className="field mt-16">
                <label htmlFor="reason">취소 사유 (선택)</label>
                <textarea
                  id="reason"
                  rows={3}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="왜 입주하지 못하게 되었습니까"
                />
              </div>
              <div className="row gap-8">
                <button type="button" className="btn btn-danger" onClick={handleCancel} disabled={busy}>
                  {busy ? '취소하는 중…' : '정말 취소합니다'}
                </button>
                <button type="button" className="btn btn-ghost" onClick={() => setCancelOpen(false)}>
                  그만두기
                </button>
              </div>
            </>
          ) : (
            <>
              <p className="tiny muted mb-16">취소해도 예약 기록 자체는 묘소에 남습니다.</p>
              <button type="button" className="btn btn-danger" onClick={() => setCancelOpen(true)}>
                예약 취소하기
              </button>
            </>
          )}
        </div>
      )}

      {/* ---------- 후기 ---------- */}
      {canReview && (
        <div className="panel">
          <p className="eyebrow">리뷰 남기기</p>
          <p className="tiny muted mb-16">무사히 내려오셨습니까. 그곳에서 본 것을 적어주십시오.</p>
          <form onSubmit={handleReview}>
            <div className="field">
              <label>평점</label>
              <StarRating value={rating} onChange={setRating} readOnly={false} />
            </div>
            <div className="field">
              <label htmlFor="rc">리뷰</label>
              <textarea
                id="rc"
                rows={5}
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="그 밤에 무슨 일이 있었습니까"
              />
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
              {busy ? '남기는 중…' : '리뷰 남기기'}
            </button>
          </form>
        </div>
      )}

      {/* ---------- 내가 남긴 리뷰 ---------- */}
      {upper === 'COMPLETED' && booking.review && (
        <div className="panel">
          <p className="eyebrow">내가 남긴 리뷰</p>
          <StarRating value={booking.review.rating} size={15} />
          <p className="muted mt-16" style={{ whiteSpace: 'pre-wrap', lineHeight: 1.8 }}>
            {booking.review.content}
          </p>
          {booking.review.createdAt && (
            <p className="tiny dim mt-16">{formatDate(booking.review.createdAt)}에 남김</p>
          )}
        </div>
      )}
    </div>
  );
}
