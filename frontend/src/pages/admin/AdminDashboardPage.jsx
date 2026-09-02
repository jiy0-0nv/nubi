import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAdminBookings, getAdminRooms } from '../../api/admin';
import { useAuth } from '../../context/AuthContext';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';
import Badge from '../../components/Badge';
import EmptyState from '../../components/EmptyState';
import { bookingStatus, bookingTotal, formatCurrency, formatShortDate, isRoomActive } from '../../utils/format';

export default function AdminDashboardPage() {
  const { profile } = useAuth();
  const [rooms, setRooms] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([getAdminRooms({ size: 100 }), getAdminBookings({ size: 100 })])
      .then(([roomPage, bookingPage]) => {
        setRooms(roomPage?.content || []);
        setBookings(bookingPage?.content || []);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner label="장부를 펼치는 중" />;

  const activeRooms = rooms.filter((r) => isRoomActive(r.status)).length;
  const confirmed = bookings.filter((b) => String(b.status).toUpperCase() === 'CONFIRMED');
  const revenue = bookings
    .filter((b) => ['CONFIRMED', 'COMPLETED'].includes(String(b.status).toUpperCase()))
    .reduce((sum, b) => sum + bookingTotal(b), 0);

  return (
    <>
      <div className="admin-topbar">
        <div>
          <p className="eyebrow">Keeper's Ledger</p>
          <h1>산장지기 장부</h1>
          <p className="tiny dim mt-8">{profile?.name || '산장지기'}님이 관리하는 산장만 표시됩니다.</p>
        </div>
        <Link to="/admin/rooms/new" className="btn btn-primary">
          + 새 산장 세우기
        </Link>
      </div>

      <Alert>{error}</Alert>

      <div className="grid-4 mb-24">
        <div className="stat">
          <p className="stat-label">보유 산장</p>
          <p className="stat-value">{rooms.length}</p>
          <p className="tiny dim mt-8">개방중 {activeRooms}채</p>
        </div>
        <div className="stat">
          <p className="stat-label">진행중 예약</p>
          <p className="stat-value blood">{confirmed.length}</p>
          <p className="tiny dim mt-8">전체 {bookings.length}건</p>
        </div>
        <div className="stat">
          <p className="stat-label">누적 매출</p>
          <p className="stat-value" style={{ fontSize: 24 }}>
            {formatCurrency(revenue)}
          </p>
          <p className="tiny dim mt-8">확정 + 완료 기준</p>
        </div>
        <div className="stat">
          <p className="stat-label">파기된 예약</p>
          <p className="stat-value">
            {bookings.filter((b) => String(b.status).toUpperCase() === 'CANCELLED').length}
          </p>
          <p className="tiny dim mt-8">기록은 남습니다</p>
        </div>
      </div>

      <div className="section-head" style={{ marginTop: 40 }}>
        <div>
          <p className="eyebrow eyebrow-ash">Latest</p>
          <h2>최근 들어온 예약</h2>
        </div>
        <Link to="/admin/bookings" className="link tiny">
          전체 보기 →
        </Link>
      </div>

      {bookings.length === 0 ? (
        <EmptyState mark="❑" title="아직 예약이 없습니다" description="산장을 등록하면 이곳에 예약이 쌓입니다." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>산장</th>
                <th>예약자</th>
                <th>일정</th>
                <th>금액</th>
                <th>상태</th>
              </tr>
            </thead>
            <tbody>
              {bookings.slice(0, 8).map((b) => {
                const s = bookingStatus(b.status);
                return (
                  <tr key={b.id}>
                    <td className="cell-strong">{b.room?.name || b.roomName || '-'}</td>
                    <td>
                      {b.guest?.name || b.guestName || '-'}
                      <br />
                      <span className="tiny dim mono">{b.guest?.email || ''}</span>
                    </td>
                    <td className="tiny">
                      {formatShortDate(b.checkInDate)} → {formatShortDate(b.checkOutDate)}
                    </td>
                    <td>{formatCurrency(bookingTotal(b))}</td>
                    <td>
                      <Badge tone={s.tone}>{s.label}</Badge>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      <div className="section-head" style={{ marginTop: 40 }}>
        <div>
          <p className="eyebrow eyebrow-ash">Properties</p>
          <h2>내 산장</h2>
        </div>
        <Link to="/admin/rooms" className="link tiny">
          전체 보기 →
        </Link>
      </div>

      {rooms.length === 0 ? (
        <EmptyState
          mark="▣"
          title="세워둔 산장이 없습니다"
          description="첫 산장을 등록해 능선 위에 올려보십시오."
          action={
            <Link to="/admin/rooms/new" className="btn btn-primary">
              새 산장 세우기
            </Link>
          }
        />
      ) : (
        <div className="grid-3">
          {rooms.slice(0, 6).map((room) => (
            <Link to={`/admin/rooms/${room.id}`} className="panel panel-tight card-hoverable" key={room.id}>
              <div className="row-between mb-8">
                <span className="tiny dim">{[room.country, room.city].filter(Boolean).join(' · ')}</span>
                <Badge tone={isRoomActive(room.status) ? 'confirmed' : 'cancelled'}>
                  {isRoomActive(room.status) ? '개방중' : '폐쇄됨'}
                </Badge>
              </div>
              <p className="serif" style={{ fontSize: 17, fontWeight: 700 }}>
                {room.name}
              </p>
              <p className="tiny dim mt-8">
                {formatCurrency(room.weekdayPrice)} / 1박 · 최대 {room.maxGuests}인
              </p>
            </Link>
          ))}
        </div>
      )}
    </>
  );
}
