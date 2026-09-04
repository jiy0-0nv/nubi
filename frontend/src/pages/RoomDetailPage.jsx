import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getRoomDetail, getRoomReviews } from '../api/rooms';
import { getBookmarkStatus } from '../api/bookmarks';
import { useAuth } from '../context/AuthContext';
import { useBookmarks } from '../context/BookmarksContext';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';
import Modal from '../components/Modal';
import StarRating from '../components/StarRating';
import GuestStepper from '../components/GuestStepper';
import CountryShape from '../components/CountryShape';
import {
  addDays,
  calculateNights,
  calculateStayBreakdown,
  formatCurrency,
  formatDate,
  formatTime,
  isRoomActive,
  toDateInputValue,
} from '../utils/format';

export default function RoomDetailPage() {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { has, toggle, reload: reloadBookmarks } = useBookmarks();

  const [room, setRoom] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [formError, setFormError] = useState('');
  const [activeImage, setActiveImage] = useState(0);

  // 설명이 10줄을 넘으면 [더보기] 버튼을 보여주고, 눌렀을 때만 전체를 팝업으로 띄웁니다.
  const descriptionRef = useRef(null);
  const [descriptionOverflows, setDescriptionOverflows] = useState(false);
  const [showFullDescription, setShowFullDescription] = useState(false);

  const today = toDateInputValue(new Date());
  const [checkin, setCheckin] = useState(today);
  const [checkout, setCheckout] = useState(addDays(today, 1));
  const [guests, setGuests] = useState(1);

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    Promise.all([getRoomDetail(roomId), getRoomReviews(roomId, { size: 20 }).catch(() => ({ content: [] }))])
      .then(([roomRes, reviewRes]) => {
        if (ignore) return;
        setRoom(roomRes);
        setReviews(reviewRes?.content || []);
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
  }, [roomId]);

  /**
   * BookmarksContext의 has()는 로그인 시 마이페이지를 한 번 불러와 만든 캐시라,
   * 이 방 하나만 놓고 보면 아직 못 불러왔거나 다른 탭에서 바뀌었을 수 있습니다.
   * 단건 확인 API로 실제 값을 물어보고, 캐시와 어긋나면 캐시를 다시 불러와 맞춥니다.
   */
  useEffect(() => {
    if (!room || !isAuthenticated) return;
    let ignore = false;
    getBookmarkStatus(room.id)
      .then((res) => {
        if (ignore) return;
        if (Boolean(res?.bookmarked) !== has(room.id)) reloadBookmarks();
      })
      .catch(() => {
        /* 부가 확인이라 실패해도 화면은 캐시 값을 그대로 보여줍니다. */
      });
    return () => {
      ignore = true;
    };
  }, [room, isAuthenticated, has, reloadBookmarks]);

  // 클램프된 문단이 실제로 잘려나갔는지(=10줄을 넘겼는지)는 렌더된 실제 높이로만 알 수 있습니다.
  useLayoutEffect(() => {
    const el = descriptionRef.current;
    if (!el) {
      setDescriptionOverflows(false);
      return;
    }
    setDescriptionOverflows(el.scrollHeight > el.clientHeight + 1);
  }, [room?.description]);

  // 백엔드 응답에 images 배열이 없을 수도 있어(대표사진만 내려주는 경우) 폭넓게 받아냅니다.
  const images = useMemo(() => {
    if (!room) return [];
    const list = room.images || room.roomImages || [];
    const urls = list.map((img) => (typeof img === 'string' ? img : img.url)).filter(Boolean);
    if (urls.length) return urls;
    return room.thumbnailUrl ? [room.thumbnailUrl] : [];
  }, [room]);

  const showPrevImage = () => setActiveImage((i) => (i - 1 + images.length) % images.length);
  const showNextImage = () => setActiveImage((i) => (i + 1) % images.length);

  const nights = calculateNights(checkin, checkout);
  const stay = room
    ? calculateStayBreakdown(checkin, checkout, room.weekdayPrice, room.weekendPrice)
    : { weekdayNights: 0, weekendNights: 0, weekdayTotal: 0, weekendTotal: 0, total: 0 };
  const estimate = stay.total;

  if (loading) return <Spinner label="묘소를 여는 중" />;
  if (error) {
    return (
      <div className="container page">
        <Alert>{error}</Alert>
      </div>
    );
  }
  if (!room) return null;

  const active = isRoomActive(room.status);
  const rating = Number(room.ratingAverage || 0);
  const bookmarked = has(room.id);

  const handleBookmark = () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: `/rooms/${roomId}` } } });
      return;
    }
    toggle(room.id);
  };

  const handleReserve = () => {
    setFormError('');
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: `/rooms/${roomId}` } } });
      return;
    }
    if (nights <= 0) {
      setFormError('입실일과 퇴실일을 올바르게 선택해 주십시오.');
      return;
    }
    if (guests > Number(room.maxGuests)) {
      setFormError(`이 묘소은 최대 ${room.maxGuests}명까지만 받습니다.`);
      return;
    }
    const params = new URLSearchParams({ checkin, checkout, guests: String(guests) });
    navigate(`/booking/${roomId}?${params.toString()}`);
  };

  return (
    <div className="container page">
      {/* ---------- 대표 사진 ---------- */}
      <div className="detail-hero">
        {images.length > 0 ? (
          <img src={images[activeImage]} alt={room.name} />
        ) : (
          <div className="card-media-empty" style={{ height: '100%' }} aria-hidden="true">
            †
          </div>
        )}
        {images.length > 1 && (
          <>
            <button
              type="button"
              className="detail-hero-arrow left"
              onClick={showPrevImage}
              aria-label="이전 사진"
            >
              ‹
            </button>
            <button
              type="button"
              className="detail-hero-arrow right"
              onClick={showNextImage}
              aria-label="다음 사진"
            >
              ›
            </button>
          </>
        )}
        <div className="detail-hero-caption">
          <p className="eyebrow">{[room.country, room.city].filter(Boolean).join(' · ')}</p>
          <h1 style={{ fontSize: 'clamp(28px, 5vw, 44px)' }}>{room.name}</h1>
        </div>
      </div>

      {images.length > 1 && (
        <div className="thumb-strip">
          {images.map((url, i) => (
            <button
              key={url}
              type="button"
              className={`thumb${i === activeImage ? ' active' : ''}`}
              onClick={() => setActiveImage(i)}
              aria-label={`사진 ${i + 1}`}
            >
              <img src={url} alt="" />
            </button>
          ))}
        </div>
      )}

      <div className="row-between mb-24">
        <div className="row gap-8">
          <StarRating value={rating} size={16} />
          <span className="tiny dim">
            {rating > 0 ? `${rating.toFixed(1)} · 리뷰 ${reviews.length}건` : '아직 리뷰이 없습니다'}
          </span>
          {!active && <span className="badge badge-cancelled">폐쇄됨</span>}
        </div>
        <button type="button" className="btn btn-outline btn-sm" onClick={handleBookmark}>
          {bookmarked ? '♥ 저장됨' : '♡ 저장하기'}
        </button>
      </div>

      <div className="detail-layout">
        {/* ---------- 본문 ---------- */}
        <div>
          <div className="spec-list mb-24">
            <div className="spec">
              <p className="spec-label">호스트</p>
              <p className="spec-value">{room.ownerName || '알 수 없음'}</p>
            </div>
            <div className="spec">
              <p className="spec-label">입실 / 퇴실</p>
              <p className="spec-value">
                {formatTime(room.checkinTime)} · {formatTime(room.checkoutTime)}
              </p>
            </div>
            <div className="spec">
              <p className="spec-label">최대 인원</p>
              <p className="spec-value">{room.maxGuests}명</p>
            </div>
            <div className="spec">
              <p className="spec-label">1박 요금</p>
              <p className="spec-value">
                {formatCurrency(room.weekdayPrice)} <span className="tiny dim">~ {formatCurrency(room.weekendPrice)}</span>
              </p>
            </div>
          </div>

          <h2 className="serif">이 묘소에 대하여</h2>
          <div className="rule mb-16" />
          <p
            ref={descriptionRef}
            className="muted clamp-text"
            style={{ lineHeight: 1.9, whiteSpace: 'pre-wrap', '--clamp-lines': 10 }}
          >
            {room.description || '기록이 남아 있지 않습니다.'}
          </p>
          {descriptionOverflows && (
            <button type="button" className="btn btn-outline btn-sm mt-8" onClick={() => setShowFullDescription(true)}>
              더보기
            </button>
          )}
          {showFullDescription && (
            <Modal title="이 묘소에 대하여" onClose={() => setShowFullDescription(false)}>
              {room.description}
            </Modal>
          )}

          <div className="divider" />

          <h2 className="serif">찾아오는 길</h2>
          <div className="rule mb-16" />
          <div className="row gap-16" style={{ alignItems: 'center' }}>
            <CountryShape country={room.country} />
            <p className="muted">{[room.country, room.city, room.street].filter(Boolean).join(' ')}</p>
          </div>

          <div className="divider" />

          <h2 className="serif">리뷰</h2>
          <div className="rule mb-16" />
          {reviews.length === 0 ? (
            <p className="tiny dim">아직 리뷰가 없습니다.</p>
          ) : (
            reviews.map((review) => (
              <div className="review" key={review.id}>
                <div className="review-head">
                  <span className="review-name">{review.reviewerName || '익명'}</span>
                  <span className="tiny dim">{formatDate(review.createdAt)}</span>
                </div>
                <StarRating value={review.rating} size={13} />
                <p className="muted mt-8" style={{ whiteSpace: 'pre-wrap' }}>
                  {review.content}
                </p>
              </div>
            ))
          )}
        </div>

        {/* ---------- 예약 위젯 ---------- */}
        <aside className="booking-box">
          <div className="row-between" style={{ alignItems: 'baseline', marginBottom: 6 }}>
            <p className="price" style={{ fontSize: 22 }}>
              {formatCurrency(room.weekdayPrice)}
              <small>/ 평일 1박</small>
            </p>
          </div>
          <p className="tiny dim mb-16">~ {formatCurrency(room.weekendPrice)} (금/토)</p>

          <div className="booking-fields">
            <div className="booking-field">
              <label className="booking-field-label" htmlFor="bf-checkin">
                입실일
              </label>
              <input
                id="bf-checkin"
                type="date"
                min={today}
                value={checkin}
                onChange={(e) => {
                  setCheckin(e.target.value);
                  if (e.target.value >= checkout) setCheckout(addDays(e.target.value, 1));
                }}
              />
            </div>
            <div className="booking-field">
              <label className="booking-field-label" htmlFor="bf-checkout">
                퇴실일
              </label>
              <input
                id="bf-checkout"
                type="date"
                min={addDays(checkin, 1)}
                value={checkout}
                onChange={(e) => setCheckout(e.target.value)}
              />
            </div>
            <div className="booking-field full">
              <div>
                <p className="booking-field-label" style={{ marginBottom: 0 }}>
                  인원
                </p>
              </div>
              <GuestStepper value={guests} onChange={setGuests} min={1} max={Math.max(1, Number(room.maxGuests) || 1)} />
            </div>
          </div>

          <Alert>{formError}</Alert>

          {!active && <Alert tone="info">예약할 수 없는 날짜입니다.</Alert>}

          <button
            type="button"
            className="btn btn-primary btn-block btn-lg"
            disabled={!active}
            onClick={handleReserve}
          >
            입실 예약하기
          </button>
          <p className="tiny dim text-center mt-8">최종 금액은 예약 시 확정됩니다.</p>

          {nights > 0 && (
            <div className="mt-24">
              {stay.weekdayNights > 0 && (
                <div className="price-line">
                  <span>
                    {formatCurrency(room.weekdayPrice)} × {stay.weekdayNights}박
                  </span>
                  <span>{formatCurrency(stay.weekdayTotal)}</span>
                </div>
              )}
              {stay.weekendNights > 0 && (
                <div className="price-line">
                  <span>
                    {formatCurrency(room.weekendPrice)} × {stay.weekendNights}박 (금·토)
                  </span>
                  <span>{formatCurrency(stay.weekendTotal)}</span>
                </div>
              )}
              <div className="price-line total">
                <span>총 합계</span>
                <span>{formatCurrency(estimate)}</span>
              </div>
            </div>
          )}
        </aside>
      </div>
    </div>
  );
}
