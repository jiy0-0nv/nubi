import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMypage } from '../api/mypage';
import { useBookmarks } from '../context/BookmarksContext';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';
import EmptyState from '../components/EmptyState';
import { formatCurrency } from '../utils/format';

export default function BookmarkListPage() {
  const { toggle, reload } = useBookmarks();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getMypage()
      .then((data) => setItems(data?.bookmarks || []))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const handleRemove = async (roomId) => {
    await toggle(roomId);
    setItems((prev) => prev.filter((item) => Number(item.roomId ?? item.id) !== Number(roomId)));
    reload();
  };

  if (loading) return <Spinner />;

  return (
    <div className="container page">
      <p className="eyebrow">Marks</p>
      <h1>남긴 표식</h1>
      <div className="rule mb-24" />

      <Alert>{error}</Alert>

      {items.length === 0 ? (
        <EmptyState
          mark="♡"
          title="아직 표식이 없습니다"
          description="마음에 둔 산장에 표식을 남겨두십시오."
          action={
            <Link to="/rooms" className="btn btn-primary">
              산장 둘러보기
            </Link>
          }
        />
      ) : (
        items.map((item) => {
          const roomId = item.roomId ?? item.id;
          return (
            <div className="list-row list-row-hoverable" key={roomId}>
              <Link to={`/rooms/${roomId}`} style={{ flex: 1 }}>
                <p className="list-row-title">{item.roomName || item.name}</p>
                <p className="list-row-sub">
                  {[item.country, item.city].filter(Boolean).join(' · ')}
                  {item.weekdayPrice ? ` · ${formatCurrency(item.weekdayPrice)} / 1박` : ''}
                </p>
              </Link>
              <button type="button" className="btn btn-danger btn-sm" onClick={() => handleRemove(roomId)}>
                표식 지우기
              </button>
            </div>
          );
        })
      )}
    </div>
  );
}
