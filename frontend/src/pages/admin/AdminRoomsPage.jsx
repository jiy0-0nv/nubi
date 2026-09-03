import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { deleteAdminRoom, getAdminRooms, updateAdminRoom } from '../../api/admin';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';
import Badge from '../../components/Badge';
import EmptyState from '../../components/EmptyState';
import Pagination from '../../components/Pagination';
import { addDays, formatCurrency, formatTime, isRoomActive, toDateInputValue } from '../../utils/format';

const EMPTY_FILTERS = { keyword: '', checkin: '', checkout: '', guests: '' };

export default function AdminRoomsPage() {
  const navigate = useNavigate();
  const today = toDateInputValue(new Date());
  const [page, setPage] = useState(0);
  const [form, setForm] = useState(EMPTY_FILTERS);
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [busyId, setBusyId] = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    setError('');
    getAdminRooms({ ...filters, page, size: 20 })
      .then(setData)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [filters, page]);

  useEffect(load, [load]);

  const submitSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setFilters(form);
  };

  const resetSearch = () => {
    setForm(EMPTY_FILTERS);
    setFilters(EMPTY_FILTERS);
    setPage(0);
  };

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
  const hasActiveFilters = Boolean(filters.keyword || filters.checkin || filters.checkout || filters.guests);

  return (
    <>
      <div className="admin-topbar">
        <div>
          <p className="eyebrow">Properties</p>
          <h1>묘소 관리</h1>
          <p className="tiny dim mt-8">현재 운영중인 묘소들입니다.</p>
        </div>
        <Link to="/admin/rooms/new" className="btn btn-primary">
          + 새 묘소 등록
        </Link>
      </div>

      <form className="search-bar mb-24" onSubmit={submitSearch}>
        <div className="field">
          <label htmlFor="af-keyword">지역 · 묘소 이름</label>
          <input
            id="af-keyword"
            type="text"
            placeholder="어디로든"
            value={form.keyword}
            onChange={(e) => setForm((f) => ({ ...f, keyword: e.target.value }))}
          />
        </div>
        <div className="field">
          <label htmlFor="af-checkin">입실일</label>
          <input
            id="af-checkin"
            type="date"
            min={today}
            value={form.checkin}
            onChange={(e) => {
              const v = e.target.value;
              setForm((f) => ({
                ...f,
                checkin: v,
                checkout: f.checkout && f.checkout > v ? f.checkout : addDays(v, 1),
              }));
            }}
          />
        </div>
        <div className="field">
          <label htmlFor="af-checkout">퇴실일</label>
          <input
            id="af-checkout"
            type="date"
            min={form.checkin ? addDays(form.checkin, 1) : today}
            value={form.checkout}
            onChange={(e) => setForm((f) => ({ ...f, checkout: e.target.value }))}
          />
        </div>
        <div className="field">
          <label htmlFor="af-guests">인원</label>
          <select
            id="af-guests"
            value={form.guests}
            onChange={(e) => setForm((f) => ({ ...f, guests: e.target.value }))}
          >
            <option value="">인원 무관</option>
            {[1, 2, 3, 4, 5, 6, 7, 8].map((n) => (
              <option key={n} value={n}>
                {n}명 이상
              </option>
            ))}
          </select>
        </div>
        <div className="row gap-8">
          <button type="submit" className="btn btn-primary" style={{ height: 44 }}>
            찾기
          </button>
          <button type="button" className="btn btn-ghost" style={{ height: 44 }} onClick={resetSearch}>
            초기화
          </button>
        </div>
      </form>

      <Alert>{error}</Alert>
      <Alert tone="success">{notice}</Alert>

      {loading ? (
        <Spinner />
      ) : rooms.length === 0 ? (
        <EmptyState
          mark="▣"
          title={hasActiveFilters ? '조건에 맞는 방이 없습니다' : '호스팅 중인 방이 없습니다'}
          description={hasActiveFilters ? '검색 조건을 바꾸어 다시 찾아보세요.' : '첫 호스팅를 시작해 보세요.'}
          action={
            hasActiveFilters ? (
              <button type="button" className="btn btn-outline" onClick={resetSearch}>
                검색 초기화
              </button>
            ) : (
              <Link to="/admin/rooms/new" className="btn btn-primary">
                새 묘소 등록
              </Link>
            )
          }
        />
      ) : (
        <>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>묘소</th>
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
                        {isRoomActive(room.status) ? '운영중' : '폐쇄됨'}
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
                          {isRoomActive(room.status) ? '폐쇄' : '운영'}
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
