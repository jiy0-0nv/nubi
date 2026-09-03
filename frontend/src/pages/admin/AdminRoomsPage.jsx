import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { deleteAdminRoom, getAdminRooms, updateAdminRoom } from '../../api/admin';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';
import Badge from '../../components/Badge';
import EmptyState from '../../components/EmptyState';
import Pagination from '../../components/Pagination';
import { formatCurrency, formatTime, isRoomActive } from '../../utils/format';

export default function AdminRoomsPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [busyId, setBusyId] = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    setError('');
    getAdminRooms({ page, size: 20 })
      .then(setData)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [page]);

  useEffect(load, [load]);

  /** 상태 토글 — PATCH는 보낸 필드만 반영되므로 status만 담아 보냅니다. */
  const toggleStatus = async (room) => {
    setBusyId(room.id);
    setError('');
    try {
      await updateAdminRoom(room.id, { status: isRoomActive(room.status) ? 'inactive' : 'active' });
      setNotice(`"${room.name}"의 상태를 바꾸었습니다.`);
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyId(null);
    }
  };

  const remove = async (room) => {
    if (!window.confirm(`"${room.name}"을(를) 완전히 허물까요? 등록된 사진도 함께 사라집니다.`)) return;
    setBusyId(room.id);
    setError('');
    try {
      await deleteAdminRoom(room.id);
      setNotice(`"${room.name}"을(를) 허물었습니다.`);
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyId(null);
    }
  };

  const rooms = data?.content || [];

  return (
    <>
      <div className="admin-topbar">
        <div>
          <p className="eyebrow">Properties</p>
          <h1>무덤 관리</h1>
          <p className="tiny dim mt-8">내가 소유한 무덤만 보입니다. 다른 묘지기의 것은 서버가 차단합니다.</p>
        </div>
        <Link to="/admin/rooms/new" className="btn btn-primary">
          + 새 무덤 세우기
        </Link>
      </div>

      <Alert>{error}</Alert>
      <Alert tone="success">{notice}</Alert>

      {loading ? (
        <Spinner />
      ) : rooms.length === 0 ? (
        <EmptyState
          mark="▣"
          title="세워둔 무덤이 없습니다"
          description="첫 무덤을 등록해 묘역에 올려보십시오."
          action={
            <Link to="/admin/rooms/new" className="btn btn-primary">
              새 무덤 세우기
            </Link>
          }
        />
      ) : (
        <>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>무덤</th>
                  <th>위치</th>
                  <th>요금 (평일/주말)</th>
                  <th>정원</th>
                  <th>운영 시간</th>
                  <th>상태</th>
                  <th style={{ textAlign: 'right' }}>관리</th>
                </tr>
              </thead>
              <tbody>
                {rooms.map((room) => (
                  <tr key={room.id}>
                    <td className="cell-strong">
                      <Link to={`/admin/rooms/${room.id}`} className="link" style={{ color: 'var(--bone)' }}>
                        {room.name}
                      </Link>
                    </td>
                    <td className="tiny">{[room.country, room.city].filter(Boolean).join(' · ') || '-'}</td>
                    <td className="tiny">
                      {formatCurrency(room.weekdayPrice)} / {formatCurrency(room.weekendPrice)}
                    </td>
                    <td className="tiny">{room.maxGuests}인</td>
                    <td className="tiny">
                      {formatTime(room.checkinTime)} ~ {formatTime(room.checkoutTime)}
                    </td>
                    <td>
                      <Badge tone={isRoomActive(room.status) ? 'confirmed' : 'cancelled'}>
                        {isRoomActive(room.status) ? '개방중' : '폐쇄됨'}
                      </Badge>
                    </td>
                    <td>
                      <div className="cell-actions">
                        <button
                          type="button"
                          className="btn btn-outline btn-sm"
                          disabled={busyId === room.id}
                          onClick={() => toggleStatus(room)}
                        >
                          {isRoomActive(room.status) ? '폐쇄' : '개방'}
                        </button>
                        <button
                          type="button"
                          className="btn btn-outline btn-sm"
                          onClick={() => navigate(`/admin/rooms/${room.id}/edit`)}
                        >
                          수정
                        </button>
                        <button
                          type="button"
                          className="btn btn-outline btn-sm"
                          onClick={() => navigate(`/admin/rooms/${room.id}/images`)}
                        >
                          사진
                        </button>
                        <button
                          type="button"
                          className="btn btn-danger btn-sm"
                          disabled={busyId === room.id}
                          onClick={() => remove(room)}
                        >
                          허물기
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />
        </>
      )}
    </>
  );
}
