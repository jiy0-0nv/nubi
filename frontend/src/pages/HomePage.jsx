import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getRooms } from '../api/rooms';
import RoomCard from '../components/RoomCard';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';
import EmptyState from '../components/EmptyState';
import GuestStepper from '../components/GuestStepper';
import { toDateInputValue, addDays } from '../utils/format';

const CATEGORIES = [
  { key: 'pyramid', label: '피라미드', color: 'var(--blood-bright)', shape: 'triangle', keywords: ['피라미드'] },
  { key: 'tomb', label: '왕릉·능묘', color: 'var(--yellow)', shape: 'dome', keywords: ['왕릉', '능묘'] },
  { key: 'dolmen', label: '고인돌·거석', color: 'var(--green)', shape: 'bar', keywords: ['고인돌', '거석'] },
  { key: 'catacomb', label: '카타콤·지하묘', color: 'var(--pink)', shape: 'arch', keywords: ['카타콤', '지하묘'] },
  { key: 'shrine', label: '사당·묘당', color: 'var(--violet)', shape: 'diamond', keywords: ['사당', '묘당'] },
  { key: 'fossil', label: '화석층', color: 'var(--bone-2)', shape: 'pill', keywords: ['화석'] },
  { key: 'virtual', label: '가상 묘소', color: 'var(--blood-bright)', shape: 'ring', keywords: ['가상'] },
];

const PROMOS = [
  {
    title: '시대별로 둘러보기',
    desc: '신석기 고인돌부터 20세기 납골당까지, 연식으로 골라보세요.',
    color: 'var(--blood-bright)',
    from: 'rgba(94,224,238,.12)',
  },
  {
    title: '세계문화유산 컬렉션',
    desc: '유네스코 등재 묘소 214곳. 보존 규정 안내 포함.',
    color: 'var(--yellow)',
    from: 'rgba(255,210,63,.12)',
  },
  {
    title: '소설 속 가상 묘소',
    desc: '카르파티아의 관, 아발론의 왕묘. 환불 규정이 다를 수 있습니다.',
    color: 'var(--violet)',
    from: 'rgba(160,110,232,.12)',
  },
];

const RULES = [
  ['첫째', '해가 진 뒤에는 묘소 밖으로 나오지 마십시오.', '체크인은 15시, 체크아웃은 11시입니다.'],
  ['둘째', '방 안에서 들리는 소리에 대답하지 마십시오.', '문의는 호스트(관리자)에게만 남겨주십시오.'],
  ['셋째', '예약은 취소할 수 있습니다. 기록은 남습니다.', '취소된 예약도 내역에서 계속 확인됩니다.'],
];

function CategoryIcon({ shape, color }) {
  const base = { width: 32, height: 28, background: color, flexShrink: 0 };
  if (shape === 'triangle') {
    return (
      <div
        aria-hidden="true"
        style={{
          width: 0,
          height: 0,
          borderLeft: '17px solid transparent',
          borderRight: '17px solid transparent',
          borderBottom: `27px solid ${color}`,
        }}
      />
    );
  }
  if (shape === 'dome') return <div aria-hidden="true" style={{ ...base, borderRadius: '16px 16px 3px 3px' }} />;
  if (shape === 'bar') return <div aria-hidden="true" style={{ ...base, height: 13, borderRadius: 4, margin: '7px 0' }} />;
  if (shape === 'arch') return <div aria-hidden="true" style={{ ...base, borderRadius: '3px 3px 16px 16px' }} />;
  if (shape === 'diamond') return <div aria-hidden="true" style={{ ...base, width: 26, height: 26, transform: 'rotate(45deg)', borderRadius: 4 }} />;
  if (shape === 'pill') return <div aria-hidden="true" style={{ ...base, borderRadius: '999px 999px 6px 999px' }} />;
  if (shape === 'ring') {
    return <div aria-hidden="true" style={{ width: 28, height: 28, borderRadius: 999, border: `4px solid ${color}` }} />;
  }
  return null;
}

/** 히어로 배경의 달·피라미드 실루엣 (순수 CSS 장식) */
function HeroDecor() {
  return (
    <>
      <div
        aria-hidden="true"
        style={{ position: 'absolute', top: 40, right: 110, width: 108, height: 108, borderRadius: 999, background: 'var(--yellow)' }}
      />
      <div
        aria-hidden="true"
        style={{ position: 'absolute', top: 40, right: 76, width: 108, height: 108, borderRadius: 999, background: 'var(--void)' }}
      />
      <div
        aria-hidden="true"
        style={{
          position: 'absolute', bottom: 0, right: 170,
          width: 0, height: 0,
          borderLeft: '92px solid transparent', borderRight: '92px solid transparent', borderBottom: '116px solid var(--stone-2)',
        }}
      />
      <div
        aria-hidden="true"
        style={{
          position: 'absolute', bottom: 0, right: 330,
          width: 0, height: 0,
          borderLeft: '60px solid transparent', borderRight: '60px solid transparent', borderBottom: '80px solid var(--soil)',
        }}
      />
      <div
        aria-hidden="true"
        style={{
          position: 'absolute', bottom: 0, right: 90,
          width: 0, height: 0,
          borderLeft: '46px solid transparent', borderRight: '46px solid transparent', borderBottom: '60px solid var(--soil)',
        }}
      />
    </>
  );
}

function PromoCard({ title, desc, color, from }) {
  return (
    <div className="promo-card" style={{ background: `linear-gradient(150deg, ${from}, var(--stone) 70%)`, borderColor: 'var(--hair)' }}>
      <div className="promo-card-icon" style={{ width: 28, height: 22, borderRadius: '14px 14px 3px 3px', background: color }} />
      <p className="promo-card-title">{title}</p>
      <p className="promo-card-desc">{desc}</p>
    </div>
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
      {/* ---------- 상단 안내 바 ---------- */}
      {/* <div className="promo-bar">
        <strong>만성절 특가 — 오래된 묘소 20% 할인</strong>
        <span className="ticker">✦ NUBI 묘원 ✦ REST IN STAY ✦ NUBI 묘원 ✦ REST IN STAY ✦</span>
      </div> */}

      {/* ---------- 히어로 ---------- */}
      <section className="landing-hero">
        <div className="landing-hero-decor">
          <HeroDecor />
        </div>
        <div className="container">
          <div className="landing-hero-inner">
            {/* <div className="landing-kicker">
              <span className="landing-kicker-dot" />
              <span>등록 묘 1,204기 · 최고 연식 6,600만 년</span>
            </div> */}
            <h1 className="landing-title">
              오늘 밤은 <span className="accent">4,600년</span> 된
              <br />
              묘소에서 자보세요
            </h1>
            <p className="landing-desc">
              피라미드부터 종묘 회랑, 고비 사막 화석층까지. 세계의 유명 묘소를 하룻밤 빌려드립니다.
            </p>

            <form className="search-pill" onSubmit={handleSearch}>
              <div className="search-pill-field">
                <span>어디로</span>
                <input
                  type="text"
                  placeholder="어디로든"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                />
              </div>
              <div className="search-pill-divider" />
              <div className="search-pill-field">
                <span>입실일</span>
                <input
                  type="date"
                  value={checkin}
                  min={today}
                  onChange={(e) => {
                    setCheckin(e.target.value);
                    if (e.target.value >= checkout) setCheckout(addDays(e.target.value, 1));
                  }}
                />
              </div>
              <div className="search-pill-divider" />
              <div className="search-pill-field">
                <span>퇴실일</span>
                <input
                  type="date"
                  value={checkout}
                  min={addDays(checkin, 1)}
                  onChange={(e) => setCheckout(e.target.value)}
                />
              </div>
              <div className="search-pill-divider" />
              <div className="search-pill-field" style={{ flex: '0.85 1 0' }}>
                <span>인원</span>
                <GuestStepper value={guests} onChange={setGuests} min={1} max={8} />
              </div>
              <button type="submit" className="search-pill-btn">
                검색
              </button>
            </form>

            {/* <div className="landing-tags">
              <span className="landing-tag">석실 난방 있음</span>
              <span className="landing-tag">세계문화유산</span>
              <span className="landing-tag">발굴 진행 중</span>
            </div> */}
          </div>
        </div>
      </section>

      {/* ---------- 카테고리 ---------- */}
      <div className="container">
        <div className="category-row" style={{ marginTop: 26 }}>
          {CATEGORIES.map((c) => (
            <Link className="category-tile" key={c.key} to={`/rooms?keyword=${encodeURIComponent(c.keywords.join(','))}`}>
              <CategoryIcon shape={c.shape} color={c.color} />
              <span>{c.label}</span>
            </Link>
          ))}
        </div>
      </div>

      {/* ---------- 추천 묘소 ---------- */}
      <div className="container page">
        <div className="section-head">
          <div>
            <h2>평이 좋은 묘소</h2>
          </div>
          <Link to="/rooms" className="link tiny">
            전체 보기 →
          </Link>
        </div>

        <Alert>{error}</Alert>

        {loading ? (
          <Spinner label="묘소를 찾는 중" />
        ) : rooms.length === 0 ? (
          <EmptyState
            mark="†"
            title="등록된 묘소가 없습니다"
            description="아직 이 지역에는 아무것도 없습니다."
          />
        ) : (
          <div className="grid-4">
            {rooms.map((room) => (
              <RoomCard key={room.id} room={room} />
            ))}
          </div>
        )}

        {/* ---------- 프로모 카드 ---------- */}
        <div className="grid-3" style={{ marginTop: 48 }}>
          {PROMOS.map((p) => (
            <PromoCard key={p.title} {...p} />
          ))}
        </div>

        {/* ---------- 호스트 CTA ---------- */}
        <div className="host-cta">
          <div style={{ flex: '1.2 1 320px' }}>
            <h2 style={{ fontSize: 'clamp(22px, 3vw, 30px)', marginBottom: 14 }}>
              관리하는 묘소가 있다면, NUBI에 올려주세요
            </h2>
            <p className="muted" style={{ marginBottom: 24, maxWidth: 480, lineHeight: 1.75 }}>
              등록 7분. 문화재 심의 대행, 관리인 인증, 보존 가이드까지 안내합니다.
            </p>
            <Link to="/signup" className="btn btn-primary btn-lg">
              호스팅 시작하기
            </Link>
          </div>
          <div className="grid-2" style={{ flex: '1 1 320px', gap: 14 }}>
            <div className="stat-mini">
              <div className="stat-mini-num" style={{ color: 'var(--blood-bright)' }}>
                1.2K
              </div>
              <div className="stat-mini-label">등록 묘소</div>
            </div>
            <div className="stat-mini">
              <div className="stat-mini-num" style={{ color: 'var(--yellow)' }}>
                98%
              </div>
              <div className="stat-mini-label">무사 체크아웃률</div>
            </div>
            <div className="stat-mini">
              <div className="stat-mini-num" style={{ color: 'var(--pink)' }}>
                24H
              </div>
              <div className="stat-mini-label">관리인 콜센터</div>
            </div>
            <div className="stat-mini">
              <div className="stat-mini-num" style={{ color: 'var(--green)' }}>
                41
              </div>
              <div className="stat-mini-label">등재 국가</div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
