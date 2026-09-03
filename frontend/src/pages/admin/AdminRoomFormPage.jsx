import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { createAdminRoom, getAdminRoomDetail, updateAdminRoom } from '../../api/admin';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';

const EMPTY = {
  name: '',
  description: '',
  country: '대한민국',
  city: '',
  street: '',
  checkinTime: '15:00',
  checkoutTime: '11:00',
  weekdayPrice: '',
  weekendPrice: '',
  maxGuests: 2,
  status: 'ACTIVE',
};

export default function AdminRoomFormPage() {
  const { roomId } = useParams();
  const isEdit = Boolean(roomId);
  const navigate = useNavigate();

  const [form, setForm] = useState(EMPTY);
  const [loading, setLoading] = useState(isEdit);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isEdit) return;
    getAdminRoomDetail(roomId)
      .then((room) =>
        setForm({
          name: room.name || '',
          description: room.description || '',
          country: room.country || '',
          city: room.city || '',
          street: room.street || '',
          checkinTime: String(room.checkinTime || '15:00:00').slice(0, 5),
          checkoutTime: String(room.checkoutTime || '11:00:00').slice(0, 5),
          weekdayPrice: room.weekdayPrice ?? '',
          weekendPrice: room.weekendPrice ?? '',
          maxGuests: room.maxGuests ?? 2,
          status: String(room.status || 'ACTIVE').toUpperCase(),
        })
      )
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [roomId, isEdit]);

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (Number(form.weekendPrice) < Number(form.weekdayPrice)) {
      // 막지는 않고 안내만 — 실제로 주말이 더 싼 묘소도 있을 수 있으니까요.
      // (여기서 return하지 않습니다)
    }

    setSubmitting(true);
    // 서버 LocalTime은 "HH:mm:ss" 형태를 기대하므로 초를 채워 보냅니다.
    const payload = {
      name: form.name.trim(),
      description: form.description.trim(),
      country: form.country.trim(),
      city: form.city.trim(),
      street: form.street.trim(),
      checkinTime: `${form.checkinTime}:00`,
      checkoutTime: `${form.checkoutTime}:00`,
      weekdayPrice: Number(form.weekdayPrice),
      weekendPrice: Number(form.weekendPrice),
      maxGuests: Number(form.maxGuests),
      ...(isEdit ? { status: form.status.toLowerCase() } : {}),
    };

    try {
      if (isEdit) {
        await updateAdminRoom(roomId, payload);
        navigate(`/admin/rooms/${roomId}`, { replace: true });
      } else {
        const created = await createAdminRoom(payload);
        // 등록 직후 사진을 올릴 수 있게 사진 관리 화면으로 이어줍니다.
        if (created?.id) navigate(`/admin/rooms/${created.id}/images`, { replace: true });
        else navigate('/admin/rooms', { replace: true });
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Spinner />;

  return (
    <>
      <div className="admin-topbar">
        <div>
          <p className="eyebrow">{isEdit ? 'Edit' : 'New'}</p>
          <h1>{isEdit ? '묘소 정보 수정' : '새 묘소 등록'}</h1>
        </div>
        <Link to="/admin/rooms" className="btn btn-ghost">
          ← 목록으로
        </Link>
      </div>

      <Alert>{error}</Alert>

      <form onSubmit={handleSubmit} className="panel" style={{ maxWidth: 720 }}>
        <div className="field">
          <label htmlFor="name">이름</label>
          <input
            id="name"
            type="text"
            required
            maxLength={100}
            value={form.name}
            onChange={update('name')}
            placeholder="예) 피라미드 108호"
          />
        </div>

        <div className="field">
          <label htmlFor="desc">소개</label>
          <textarea
            id="desc"
            rows={4}
            value={form.description}
            onChange={update('description')}
            placeholder="이 곳에 대해 손님이 알아야 할 것"
          />
        </div>

        <div className="field-row">
          <div className="field">
            <label htmlFor="country">국가</label>
            <input id="country" type="text" required value={form.country} onChange={update('country')} />
          </div>
          <div className="field">
            <label htmlFor="city">도시</label>
            <input id="city" type="text" required value={form.city} onChange={update('city')} placeholder="파주" />
          </div>
        </div>

        <div className="field">
          <label htmlFor="street">상세 주소</label>
          <input id="street" type="text" required value={form.street} onChange={update('street')} />
        </div>

        <div className="field-row">
          <div className="field">
            <label htmlFor="ci">체크인 시각</label>
            <input id="ci" type="time" required value={form.checkinTime} onChange={update('checkinTime')} />
          </div>
          <div className="field">
            <label htmlFor="co">체크아웃 시각</label>
            <input id="co" type="time" required value={form.checkoutTime} onChange={update('checkoutTime')} />
          </div>
        </div>

        <div className="field-row">
          <div className="field">
            <label htmlFor="wd">평일 요금 (원)</label>
            <input
              id="wd"
              type="number"
              min="0"
              step="1000"
              required
              value={form.weekdayPrice}
              onChange={update('weekdayPrice')}
            />
          </div>
          <div className="field">
            <label htmlFor="we">주말 요금 (원)</label>
            <input
              id="we"
              type="number"
              min="0"
              step="1000"
              required
              value={form.weekendPrice}
              onChange={update('weekendPrice')}
            />
            <span className="field-hint">금·토 숙박에 적용됩니다.</span>
          </div>
        </div>

        <div className="field-row">
          <div className="field">
            <label htmlFor="mg">최대 인원</label>
            <input id="mg" type="number" min="1" max="30" required value={form.maxGuests} onChange={update('maxGuests')} />
          </div>
          {isEdit && (
            <div className="field">
              <label htmlFor="st">운영 상태</label>
              <select id="st" value={form.status} onChange={update('status')}>
                <option value="ACTIVE">개방중</option>
                <option value="INACTIVE">폐쇄됨</option>
              </select>
            </div>
          )}
        </div>

        <div className="row gap-8 mt-24">
          <button type="submit" className="btn btn-primary btn-lg" disabled={submitting}>
            {submitting ? '저장하는 중…' : isEdit ? '수정 완료' : '묘소 세우기'}
          </button>
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/admin/rooms')}>
            취소
          </button>
        </div>
      </form>
    </>
  );
}
