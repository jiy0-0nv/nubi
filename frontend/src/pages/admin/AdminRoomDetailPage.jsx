import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { deleteAdminRoom, getAdminBookings, getAdminRoomDetail, getRoomImages } from '../../api/admin';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';
import Badge from '../../components/Badge';
import { bookingStatus, bookingTotal, formatCurrency, formatShortDate, formatTime, isRoomActive } from '../../utils/format';

export default function AdminRoomDetailPage() {
  const { roomId } = useParams();
  const navigate = useNavigate();

  const [room, setRoom] = useState(null);
  const [images, setImages] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    Promise.all([
      getAdminRoomDetail(roomId),
      getRoomImages(roomId).catch(() => []),
      getAdminBookings({ roomId, size: 50 }).catch(() => ({ content: [] })),
    ])
      .then(([roomRes, imageRes, bookingRes]) => {
        setRoom(roomRes);
        setImages(Array.isArray(imageRes) ? imageRes : imageRes?.content || []);
        setBookings(bookingRes?.content || []);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [roomId]);

  useEffect(load, [load]);

  const remove = async () => {
    if (!window.confirm(`"${room.name}"을(를) 완전히 삭제할까요?`)) return;
    try {
      await deleteAdminRoom(roomId);
      navigate('/admin/rooms', { replace: true });
    } catch (err) {
      setError(err.message);
    }
  };

  if (loading) return <Spinner />;
  if (!room) {
    return (
      <>
        <Alert>{error || '묘소를 찾을 수 없습니다.'}</Alert>
        <Link to="/admin/rooms" className="btn btn-ghost">
          ← 목록으로
        </Link>
      </>
    );
  }

  return (
    <>
      <div className="admin-topbar">
        <div>
          <p className="eyebrow">{[room.country, room.city].filter(Boolean).join(' · ')}</p>
          <h1>{room.name}</h1>
          <div className="row gap-8 mt-8">
            <Badge tone={isRoomActive(room.status) ? 'confirmed' : 'cancelled'}>
              {isRoomActive(room.status) ? '개방중' : '폐쇄됨'}
            </Badge>
            <span className="tiny dim mono">ROOM #{room.id}</span>
          </div>
        </div>
        <div className="row gap-8">
          <Link to={`/admin/rooms/${roomId}/images`} className="btn btn-outline">
            사진 관리
          </Link>
          <Link to={`/admin/rooms/${roomId}/edit`} className="btn btn-primary">
            정보 수정
          </Link>
        </div>
      </div>

      <Alert>{error}</Alert>

      <div className="grid-4 mb-24">
        <div className="stat">
          <p className="stat-label">평일 요금</p>
          <p className="stat-value" style={{ fontSize: 22 }}>
            {formatCurrency(room.weekdayPrice)}
          </p>
        </div>
        <div className="stat">
          <p className="stat-label">주말 요금</p>
          <p className="stat-value" style={{ fontSize: 22 }}>
            {formatCurrency(room.weekendPrice)}
          </p>
        </div>
        <div className="stat">
          <p className="stat-label">최대 인원</p>
          <p className="stat-value">{room.maxGuests}</p>
        </div>
        <div className="stat">
          <p className="stat-label">운영 시간</p>
          <p className="stat-value" style={{ fontSize: 20 }}>
            {formatTime(room.checkinTime)} ~ {formatTime(room.checkoutTime)}
          </p>
        </div>
      </div>

      <div className="panel mb-24">
        <p className="eyebrow eyebrow-ash">소개</p>
        <p className="muted" style={{ whiteSpace: 'pre-wrap', lineHeight: 1.8 }}>
          {room.description || '아직 소개가 없습니다.'}
        </p>
        <div className="divider" style={{ margin: '20px 0' }} />
        <p className="eyebrow eyebrow-ash">주소</p>
        <p className="muted">{[room.country, room.city, room.street].filter(Boolean).join(' ')}</p>
      </div>

      <div className="section-head">
        <div>
          <p className="eyebrow eyebrow-ash">Photos</p>
          <h2>등록된 사진 {images.length}장</h2>
        </div>
        <Link to={`/admin/rooms/${roomId}/images`} className="link tiny">
          사진 관리 →
        </Link>
      </div>

      {images.length === 0 ? (
        <p className="tiny dim">등록된 사진이 없습니다. 첫 사진을 올려주십시오.</p>
      ) : (
        <div className="image-grid">
          {images.slice(0, 8).map((img) => (
            <div className="image-cell" key={img.id}>
              <img src={img.url} alt="" />
            </div>
          ))}
        </div>
      )}

      <div className="section-head">
        <div>
          <p className="eyebrow eyebrow-ash">Bookings</p>
          <h2>이 묘소의 예약 {bookings.length}건</h2>
        </div>
        <Link to={`/admin/bookings?room_id=${roomId}`} className="link tiny">
          예약 관리로 →
        </Link>
      </div>

      {bookings.length === 0 ? (
        <p className="tiny dim">아직 예약이 없습니다.</p>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>예약자</th>
                <th>일정</th>
                <th>인원</th>
                <th>금액</th>
                <th>상태</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => {
                const s = bookingStatus(b.status);
                return (
                  <tr key={b.id}>
                    <td className="cell-strong">{b.guest?.name || b.guestName || '-'}</td>
                    <td className="tiny">
                      {formatShortDate(b.checkInDate)} → {formatShortDate(b.checkOutDate)}
                    </td>
                    <td className="tiny">{b.guestCount}명</td>
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

      <div className="panel mt-40" style={{ borderColor: 'var(--hair-blood)' }}>
        <p className="eyebrow">이 묘소 삭제하기</p>
        <p className="tiny muted mb-16">이 묘소를 삭제하면 모든 관련 정보가 영구적으로 삭제됩니다.</p>
        <button type="button" className="btn btn-danger" onClick={remove}>
          동의하고 삭제하기
        </button>
      </div>
    </>
  );
}
