import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMypage } from '../api/mypage';
import { useAuth } from '../context/AuthContext';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';
import Badge from '../components/Badge';
import EmptyState from '../components/EmptyState';
import StarRating from '../components/StarRating';
import { bookingStatus, bookingTotal, formatCurrency, formatShortDate } from '../utils/format';

export default function MyPage() {
  const { profile } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getMypage()
      .then(setData)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner label="기록을 펼치는 중" />;

  const me = data?.profile || profile;
  const bookings = data?.bookings || [];
  const bookmarks = data?.bookmarks || [];
  const reviews = data?.reviews || [];

  return (
    <div className="container page">
      <Alert>{error}</Alert>

      <div className="profile-head">
        <div className="avatar" aria-hidden="true">
          {me?.name ? me.name[0] : '?'}
        </div>
        <div style={{ flex: 1 }}>
          <p className="eyebrow" style={{ marginBottom: 6 }}>
            User
          </p>
          <h1 style={{ fontSize: 26 }}>{me?.name || '이름 없음'}</h1>
          <p className="tiny dim mono mt-8">{me?.email}</p>
        </div>
        <Link to="/mypage/edit" className="btn btn-outline btn-sm">
          기록 수정
        </Link>
      </div>

      <div className="grid-3 mb-24">
        <Link to="/mypage/bookings" className="tile">
          <p className="tile-num">{bookings.length}</p>
          <p className="tile-label">예약 내역</p>
        </Link>
        <Link to="/mypage/bookmarks" className="tile">
          <p className="tile-num">{bookmarks.length}</p>
          <p className="tile-label">저장된 묘소</p>
        </Link>
        <div className="tile">
          <p className="tile-num">{reviews.length}</p>
          <p className="tile-label">내 리뷰</p>
        </div>
      </div>

      <div className="section-head">
        <div>
          <p className="eyebrow eyebrow-ash">Recent</p>
          <h2>최근 예약</h2>
        </div>
        <Link to="/mypage/bookings" className="link tiny">
          전체 보기 →
        </Link>
      </div>

      {bookings.length === 0 ? (
        <EmptyState
          mark="†"
          title="아직 든 적이 없습니다"
          description="묘역의 묘소를 둘러보십시오."
          action={
            <Link to="/rooms" className="btn btn-primary">
              SEARCH
            </Link>
          }
        />
      ) : (
        bookings.slice(0, 5).map((booking) => {
          const status = bookingStatus(booking.status);
          return (
            <Link to={`/mypage/bookings/${booking.id}`} className="list-row" key={booking.id}>
              <div>
                <p className="list-row-title">{booking.roomName}</p>
                <p className="list-row-sub">
                  {formatShortDate(booking.checkInDate)} → {formatShortDate(booking.checkOutDate)}
                  {bookingTotal(booking) > 0 && ` · ${formatCurrency(bookingTotal(booking))}`}
                </p>
              </div>
              <Badge tone={status.tone}>{status.label}</Badge>
            </Link>
          );
        })
      )}

      {reviews.length > 0 && (
        <>
          <div className="section-head">
            <div>
              <p className="eyebrow eyebrow-ash">Testimony</p>
              <h2>내가 남긴 리뷰</h2>
            </div>
          </div>
          {reviews.map((review) => (
            <div className="list-row" key={review.id}>
              <div>
                <StarRating value={review.rating} size={13} />
                <p className="list-row-sub mt-8" style={{ whiteSpace: 'pre-wrap' }}>
                  {review.content}
                </p>
              </div>
            </div>
          ))}
        </>
      )}
    </div>
  );
}
