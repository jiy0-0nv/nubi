import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMypage } from '../api/mypage';
import RoomCard from '../components/RoomCard';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';
import EmptyState from '../components/EmptyState';

export default function BookmarkListPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getMypage()
      .then((data) => setItems(data?.bookmarks || []))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner />;

  // /rooms 목록과 같은 RoomCard를 그대로 쓰기 위해 북마크 항목을 room 모양으로 맞춥니다.
  // (해제는 카드에 이미 있는 하트 버튼으로 — BookmarksContext가 낙관적으로 처리합니다.)
  const rooms = items.map((item) => ({
    id: item.roomId ?? item.id,
    name: item.roomName ?? item.name,
    thumbnailUrl: item.thumbnailUrl,
    country: item.country,
    city: item.city,
    maxGuests: item.maxGuests,
    ratingAverage: item.ratingAverage,
    weekdayPrice: item.weekdayPrice,
    weekendPrice: item.weekendPrice,
    status: item.status,
  }));

  return (
    <div className="container page">
      <p className="eyebrow">Marks</p>
      <h1>저장한 묘소</h1>
      <div className="rule mb-24" />

      <Alert>{error}</Alert>

      {rooms.length === 0 ? (
        <EmptyState
          mark="♡"
          title="아직 저장한 묘소가 없습니다."
          description="가보고 싶은 묘소를 저장해보세요."
          action={
            <Link to="/rooms" className="btn btn-primary">
              SEARCH
            </Link>
          }
        />
      ) : (
        <div className="grid-4">
          {rooms.map((room) => (
            <RoomCard key={room.id} room={room} />
          ))}
        </div>
      )}
    </div>
  );
}
