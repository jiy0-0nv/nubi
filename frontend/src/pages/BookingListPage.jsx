import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getBookings } from '../api/bookings';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';
import Badge from '../components/Badge';
import EmptyState from '../components/EmptyState';
import Pagination from '../components/Pagination';
import { bookingStatus, bookingTotal, formatCurrency, formatShortDate } from '../utils/format';

const TABS = [
  { key: '', label: '전체' },
  { key: 'CONFIRMED', label: '봉인 완료' },
  { key: 'COMPLETED', label: '퇴실 완료' },
  { key: 'CANCELLED', label: '파기됨' },
];

export default function BookingListPage() {
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    setError('');
    getBookings({ status: status || undefined, page })
      .then(setData)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [status, page]);

  useEffect(load, [load]);

  const bookings = data?.content || [];

  return (
    <div className="container page">
      <p className="eyebrow">Entry Log</p>
      <h1>예약 내역</h1>
      <div className="rule mb-24" />

      <div className="tabs" style={{ marginTop: 28 }}>
        {TABS.map((tab) => (
          <button
            key={tab.key}
            type="button"
            className={`tab${status === tab.key ? ' active' : ''}`}
            onClick={() => {
              setStatus(tab.key);
              setPage(0);
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <Alert>{error}</Alert>

      {loading ? (
        <Spinner />
      ) : bookings.length === 0 ? (
        <EmptyState
          mark="†"
          title="해당하는 기록이 없습니다"
          description="다른 상태를 골라보거나 새로 예약해 보십시오."
          action={
            <Link to="/rooms" className="btn btn-primary">
              SEARCH
            </Link>
          }
        />
      ) : (
        <>
          {bookings.map((booking) => {
            const s = bookingStatus(booking.status);
            return (
              <Link to={`/mypage/bookings/${booking.id}`} className="list-row" key={booking.id}>
                <div>
                  <p className="list-row-title">{booking.roomName || booking.room?.name}</p>
                  <p className="list-row-sub">
                    {formatShortDate(booking.checkInDate)} → {formatShortDate(booking.checkOutDate)} ·{' '}
                    {booking.guestCount}명 · {formatCurrency(bookingTotal(booking))}
                  </p>
                </div>
                <Badge tone={s.tone}>{s.label}</Badge>
              </Link>
            );
          })}
          <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}
