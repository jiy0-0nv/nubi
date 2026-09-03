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
              width: 32,
              height: 32,
              borderRadius: '50%',
              display: 'grid',
              placeItems: 'center',
              border: '1px solid var(--hair-strong)',
              background: 'rgba(7,9,13,0.72)',
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
        <div className="row-between" style={{ gap: 8, alignItems: 'baseline' }}>
          <p className="card-title" style={{ flex: 1, minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {room.name}
          </p>
          {rating > 0 && (
            <span className="mono" style={{ flexShrink: 0, color: 'var(--bone)', fontWeight: 600 }}>
              ☾ {rating.toFixed(2)}
            </span>
          )}
        </div>
        <p className="card-sub">
          {[room.country, room.city].filter(Boolean).join(' · ') || '위치 미상'} · 최대 {room.maxGuests}인
        </p>
        <p className="price" style={{ marginTop: 2 }}>
          {formatCurrency(room.weekdayPrice)}
          <small>/ 1박</small>
        </p>
        <p className="tiny dim">~ {formatCurrency(room.weekendPrice)}</p>
      </div>
    </Link>
  );
}
