import { Link, useNavigate } from 'react-router-dom';
import { formatCurrency, isRoomActive } from '../utils/format';
import { useAuth } from '../context/AuthContext';
import { useBookmarks } from '../context/BookmarksContext';

export default function RoomCard({ room }) {
  const { isAuthenticated, isAdmin } = useAuth();
  const { has, isPending, toggle } = useBookmarks();
  const navigate = useNavigate();

  const active = isRoomActive(room.status);
  const bookmarked = has(room.id);
  const rating = Number(room.ratingAverage || 0);

  const handleBookmark = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    toggle(room.id);
  };

  return (
    <Link to={`/rooms/${room.id}`} className="card">
      <div className="card-media">
        {room.thumbnailUrl ? (
          <img src={room.thumbnailUrl} alt={room.name} loading="lazy" />
        ) : (
          <div className="card-media-empty" aria-hidden="true">
            †
          </div>
        )}
        {!isAdmin && (
          <button
            type="button"
            onClick={handleBookmark}
            disabled={isPending(room.id)}
            aria-label={bookmarked ? '북마크 해제' : '북마크 추가'}
            style={{
              position: 'absolute',
              top: 12,
              right: 12,
              zIndex: 2,
              width: 34,
              height: 34,
              borderRadius: '50%',
              display: 'grid',
              placeItems: 'center',
              border: '1px solid var(--hair-strong)',
              background: 'rgba(5,4,6,0.7)',
              backdropFilter: 'blur(6px)',
              color: bookmarked ? 'var(--blood-bright)' : 'var(--ash)',
              cursor: 'pointer',
              fontSize: 15,
              lineHeight: 1,
            }}
          >
            {bookmarked ? '♥' : '♡'}
          </button>
        )}
        {!active && (
          <span style={{ position: 'absolute', left: 12, top: 12, zIndex: 2 }}>
            <span className="badge badge-cancelled">폐쇄됨</span>
          </span>
        )}
      </div>

      <div className="card-body">
        <p className="card-sub">
          {[room.country, room.city].filter(Boolean).join(' · ') || '위치 미상'}
        </p>
        <p className="card-title">{room.name}</p>
        <p className="card-sub">
          {rating > 0 ? (
            <>
              <span className="blood-text">★ {rating.toFixed(1)}</span>
              {room.reviewCount ? ` · 증언 ${room.reviewCount}건` : ''}
            </>
          ) : (
            '아직 아무도 다녀가지 않음'
          )}
        </p>
        <div className="card-foot">
          <span className="price">
            {formatCurrency(room.weekdayPrice)}
            <small>/ 1박</small>
          </span>
          <span className="tiny dim">최대 {room.maxGuests}인</span>
        </div>
      </div>
    </Link>
  );
}
