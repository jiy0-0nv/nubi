import { useCallback, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { cancelAdminBooking, getAdminBookings, getAdminRooms } from '../../api/admin';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';
import Badge from '../../components/Badge';
import EmptyState from '../../components/EmptyState';
import Pagination from '../../components/Pagination';
import { bookingStatus, formatCurrency, formatShortDate } from '../../utils/format';

const TABS = [
  { key: '', label: '전체' },
  { key: 'CONFIRMED', label: '봉인 완료' },
  { key: 'COMPLETED', label: '하산 완료' },
  { key: 'CANCELLED', label: '파기됨' },
];

export default function AdminBookingsPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const status = searchParams.get('status') || '';
  const roomId = searchParams.get('room_id') || '';
  const page = Number(searchParams.get('page') || 0);

  const [rooms, setRooms] = useState([]);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [busyId, setBusyId] = useState(null);

  // 필터 드롭다운에 쓸 내 숙소 목록
  useEffect(() => {
    getAdminRooms({ size: 100 })
      .then((res) => setRooms(res?.content || []))
      .catch(() => setRooms([]));
  }, []);

  const load = useCallback(() => {
    setLoading(true);
    setError('');
    getAdminBookings({ status: status || undefined, roomId: roomId || undefined, page, size: 20 })
      .then(setData)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [status, roomId, page]);

  useEffect(load, [load]);

  const setParam = (patch) => {
    const next = Object.fromEntries(searchParams.entries());
    Object.entries(patch).forEach(([k, v]) => {
      if (v) next[k] = String(v);
      else delete next[k];
    });
    if (!('page' in patch)) next.page = '0';
    setSearchParams(next);
  };

  const cancel = async (booking) => {
    const who = booking.guest?.name || booking.guestName || '이 손님';
    if (!window.confirm(`${who}의 예약을 파기할까요? 기록은 남습니다.`)) return;
    setBusyId(booking.id);
    setError('');
    try {
      await cancelAdminBooking(booking.id);
      setNotice('예약을 파기했습니다.');
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyId(null);
    }
  };

  const bookings = data?.content || [];

  return (
    <>
      <div className="admin-topbar">
        <div>
          <p className="eyebrow">Bookings</p>
          <h1>예약 관리</h1>
          <p className="tiny dim mt-8">내 산장에 들어온 예약만 보입니다.</p>
        </div>
        <div className="field" style={{ marginBottom: 0, minWidth: 220 }}>
          <label htmlFor="roomFilter">산장별 보기</label>
          <select id="roomFilter" value={roomId} onChange={(e) => setParam({ room_id: e.target.value })}>
            <option value="">전체 산장</option>
            {rooms.map((room) => (
              <option key={room.id} value={room.id}>
                {room.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="tabs">
        {TABS.map((tab) => (
          <button
            key={tab.key}
            type="button"
            className={`tab${status === tab.key ? ' active' : ''}`}
            onClick={() => setParam({ status: tab.key })}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <Alert>{error}</Alert>
      <Alert tone="success">{notice}</Alert>

      {loading ? (
        <Spinner />
      ) : bookings.length === 0 ? (
        <EmptyState mark="❑" title="해당하는 예약이 없습니다" description="필터를 바꾸어 다시 확인해 보십시오." />
      ) : (
        <>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>산장</th>
                  <th>예약자</th>
                  <th>일정</th>
                  <th>인원</th>
                  <th>금액</th>
                  <th>상태</th>
                  <th style={{ textAlign: 'right' }}>관리</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map((b) => {
                  const s = bookingStatus(b.status);
                  const canCancel = String(b.status).toUpperCase() === 'CONFIRMED';
                  return (
                    <tr key={b.id}>
                      <td className="mono dim">{String(b.id).padStart(4, '0')}</td>
                      <td className="cell-strong">{b.room?.name || b.roomName || '-'}</td>
                      <td>
                        {b.guest?.name || b.guestName || '-'}
                        <br />
                        <span className="tiny dim mono">{b.guest?.email || b.guest?.phone || ''}</span>
                      </td>
                      <td className="tiny">
                        {formatShortDate(b.checkInDate)} → {formatShortDate(b.checkOutDate)}
                      </td>
                      <td className="tiny">{b.guestCount}명</td>
                      <td>{formatCurrency(b.totalPrice)}</td>
                      <td>
                        <Badge tone={s.tone}>{s.label}</Badge>
                      </td>
                      <td>
                        <div className="cell-actions">
                          <Link to={`/admin/bookings/${b.id}`} className="btn btn-outline btn-sm">
                            상세
                          </Link>
                          {canCancel && (
                            <button
                              type="button"
                              className="btn btn-danger btn-sm"
                              disabled={busyId === b.id}
                              onClick={() => cancel(b)}
                            >
                              파기
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={data?.totalPages} onChange={(p) => setParam({ page: String(p) })} />
        </>
      )}
    </>
  );
}
