import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getRooms } from '../api/rooms';
import RoomCard from '../components/RoomCard';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';
import EmptyState from '../components/EmptyState';
import { toDateInputValue, addDays } from '../utils/format';

/** 히어로 아래에서 자라 오르는 뿌리 실루엣 (포스터 모티프) */
function Roots() {
  return (
    <svg className="hero-roots" viewBox="0 0 1200 260" fill="none" aria-hidden="true">
      <g stroke="#071211" strokeWidth="2.4" strokeLinecap="round" opacity="0.95">
        <path d="M600 260V120M600 150l-42-38M600 178l46-42M558 112l-30-34M558 112l-46-16M646 136l40-30M646 136l16-44M528 78l-26-30M512 96l-52-8M686 106l44-16M662 92l10-40" />
        <path d="M600 200l-70-30M600 214l84-36M530 170l-64-14M684 178l70-24M466 156l-58 8M754 154l62 12" />
        <path d="M408 164l-52 22M816 166l56 26M356 186l-58-6M872 192l64-8M298 180l-64 26M936 184l70 20" />
        <path d="M234 206l-70-8M1006 204l72-6M164 198l-72 22M1078 198l76 18" />
      </g>
      <path d="M0 260h1200V236c-120-16-210 8-320-6s-180-30-280-30-190 22-300 34S110 224 0 238z" fill="#071211" />
    </svg>
  );
}

export default function HomePage() {
  const navigate = useNavigate();
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const today = toDateInputValue(new Date());
  const [checkin, setCheckin] = useState(today);
  const [checkout, setCheckout] = useState(addDays(today, 1));
  const [keyword, setKeyword] = useState('');
  const [guests, setGuests] = useState(2);

  useEffect(() => {
    let ignore = false;
    getRooms({ size: 8 })
      .then((page) => {
        if (!ignore) setRooms(page?.content || []);
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
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    const params = new URLSearchParams();
    if (keyword) params.set('keyword', keyword);
    if (checkin) params.set('checkin', checkin);
    if (checkout) params.set('checkout', checkout);
    if (guests) params.set('guests', String(guests));
    navigate(`/rooms?${params.toString()}`);
  };

  return (
    <>
      {/* ---------- 포스터형 히어로 ---------- */}
      <section className="hero">
        <Roots />
        <div className="hero-inner">
          <p className="hero-credit">누비 묘원 · 제 1 구역 개방</p>
          <h1 className="hero-title">영면</h1>
          <p className="hero-tagline">함부로 깨우지 말 것</p>
          <p className="hero-desc">
            1987년 이후 열두 기의 무덤이 이 묘역에 들어섰습니다.
            <br />
            그 아래에 무엇이 묻혀 있는지는 아무도 묻지 않았습니다.
            <br />
            지금 남은 방을 확인하십시오.
          </p>
          <div className="hero-actions">
            <Link to="/rooms" className="btn btn-primary btn-lg">
              남은 방 확인하기
            </Link>
            <Link to="/signup" className="btn btn-outline btn-lg">
              입주 신청
            </Link>
          </div>
        </div>
      </section>

      {/* ---------- 검색 ---------- */}
      <div className="container">
        <form className="search-bar" onSubmit={handleSearch}>
          <div className="field">
            <label htmlFor="q">지역 · 무덤 이름</label>
            <input
              id="q"
              type="text"
              placeholder="어느 묘역을 찾으십니까"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
          </div>
          <div className="field">
            <label htmlFor="ci">입실일</label>
            <input
              id="ci"
              type="date"
              value={checkin}
              min={today}
              onChange={(e) => {
                setCheckin(e.target.value);
                if (e.target.value >= checkout) setCheckout(addDays(e.target.value, 1));
              }}
            />
          </div>
          <div className="field">
            <label htmlFor="co">퇴실일</label>
            <input
              id="co"
              type="date"
              value={checkout}
              min={addDays(checkin, 1)}
              onChange={(e) => setCheckout(e.target.value)}
            />
          </div>
          <div className="field">
            <label htmlFor="g">인원</label>
            <select id="g" value={guests} onChange={(e) => setGuests(Number(e.target.value))}>
              {[1, 2, 3, 4, 5, 6, 7, 8].map((n) => (
                <option key={n} value={n}>
                  {n}명
                </option>
              ))}
            </select>
          </div>
          <button type="submit" className="btn btn-primary" style={{ height: 44 }}>
            찾기
          </button>
        </form>
      </div>

      {/* ---------- 추천 무덤 ---------- */}
      <div className="container page">
        <div className="section-head">
          <div>
            <p className="eyebrow">아직 비어 있는 방</p>
            <h2>묘역의 무덤들</h2>
          </div>
          <Link to="/rooms" className="link tiny">
            전체 보기 →
          </Link>
        </div>

        <Alert>{error}</Alert>

        {loading ? (
          <Spinner label="무덤을 여는 중" />
        ) : rooms.length === 0 ? (
          <EmptyState
            mark="†"
            title="등록된 무덤이 없습니다"
            description="아직 이 묘역에는 아무것도 세워지지 않았습니다."
          />
        ) : (
          <div className="grid-4">
            {rooms.map((room) => (
              <RoomCard key={room.id} room={room} />
            ))}
          </div>
        )}

        {/* ---------- 컨셉 안내 ---------- */}
        <div className="section-head">
          <div>
            <p className="eyebrow eyebrow-ash">입주 전 반드시 읽을 것</p>
            <h2>세 가지 규칙</h2>
          </div>
        </div>
        <div className="grid-3">
          {[
            ['첫째', '해가 진 뒤에는 무덤 밖으로 나오지 마십시오.', '체크인은 15시, 체크아웃은 11시입니다.'],
            ['둘째', '방 안에서 들리는 소리에 대답하지 마십시오.', '문의는 묘지기(관리자)에게만 남겨주십시오.'],
            ['셋째', '예약은 취소할 수 있습니다. 기록은 남습니다.', '취소된 예약도 내역에서 계속 확인됩니다.'],
          ].map(([no, title, desc]) => (
            <div className="panel" key={no}>
              <p className="eyebrow">{no}</p>
              <p className="serif" style={{ fontSize: 17, lineHeight: 1.6, marginBottom: 10 }}>
                {title}
              </p>
              <p className="tiny dim">{desc}</p>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}
