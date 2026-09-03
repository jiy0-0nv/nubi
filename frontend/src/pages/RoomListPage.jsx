import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getRooms } from '../api/rooms';
import RoomCard from '../components/RoomCard';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';
import EmptyState from '../components/EmptyState';
import Pagination from '../components/Pagination';
import GuestStepper from '../components/GuestStepper';
import { addDays, toDateInputValue } from '../utils/format';

const PAGE_SIZE = 12;

export default function RoomListPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const keyword = searchParams.get('keyword') || '';
  const checkin = searchParams.get('checkin') || '';
  const checkout = searchParams.get('checkout') || '';
  const guests = Number(searchParams.get('guests') || 0);
  const page = Number(searchParams.get('page') || 0);

  const [form, setForm] = useState({ keyword, checkin, checkout, guests: guests || 2 });
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setForm({ keyword, checkin, checkout, guests: guests || 2 });
  }, [keyword, checkin, checkout, guests]);

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    setError('');
    getRooms({ keyword, checkin, checkout, guests: guests || undefined, page, size: PAGE_SIZE })
      .then((res) => {
        if (!ignore) setData(res);
      })
      .catch((err) => {
        if (!ignore) setError(err.message);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });
    return () => {
      ignore = true;
    };
  }, [keyword, checkin, checkout, guests, page]);

  const rooms = data?.content || [];

  const submit = (e) => {
    e.preventDefault();
    const next = {};
    if (form.keyword) next.keyword = form.keyword;
    if (form.checkin) next.checkin = form.checkin;
    if (form.checkout) next.checkout = form.checkout;
    if (form.guests) next.guests = String(form.guests);
    next.page = '0';
    setSearchParams(next);
  };

  const goPage = (nextPage) => {
    const next = Object.fromEntries(searchParams.entries());
    next.page = String(nextPage);
    setSearchParams(next);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const today = toDateInputValue(new Date());

  return (
    <div className="container page">
      <p className="eyebrow">Grave Directory</p>
      <h1>묘역의 무덤들</h1>
      <div className="rule" />

      <form className="search-bar mb-24" style={{ marginTop: 32 }} onSubmit={submit}>
        <div className="field">
          <label>지역 · 무덤 이름</label>
          <input
            type="text"
            placeholder="어디로든"
            value={form.keyword}
            onChange={(e) => setForm((f) => ({ ...f, keyword: e.target.value }))}
          />
        </div>
        <div className="field">
          <label>입실일</label>
          <input
            type="date"
            min={today}
            value={form.checkin}
            onChange={(e) => {
              const v = e.target.value;
              setForm((f) => ({ ...f, checkin: v, checkout: f.checkout && f.checkout > v ? f.checkout : addDays(v, 1) }));
            }}
          />
        </div>
        <div className="field">
          <label>퇴실일</label>
          <input
            type="date"
            min={form.checkin ? addDays(form.checkin, 1) : today}
            value={form.checkout}
            onChange={(e) => setForm((f) => ({ ...f, checkout: e.target.value }))}
          />
        </div>
        <div className="field">
          <label>인원</label>
          <GuestStepper value={form.guests} onChange={(v) => setForm((f) => ({ ...f, guests: v }))} min={1} max={8} />
        </div>
        <button type="submit" className="btn btn-primary" style={{ height: 44 }}>
          찾기
        </button>
      </form>

      <Alert>{error}</Alert>

      {loading ? (
        <Spinner label="무덤을 여는 중" />
      ) : rooms.length === 0 ? (
        <EmptyState
          mark="†"
          title="조건에 맞는 무덤이 없습니다"
          description="날짜나 인원을 바꾸어 다시 찾아보십시오."
        />
      ) : (
        <>
          <p className="tiny dim mb-16">{rooms.length}기의 무덤이 문을 열고 있습니다.</p>
          <div className="grid-4">
            {rooms.map((room) => (
              <RoomCard key={room.id} room={room} />
            ))}
          </div>
          <Pagination page={page} totalPages={data?.totalPages} onChange={goPage} />
        </>
      )}
    </div>
  );
}
