import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  deleteRoomImage,
  getAdminRoomDetail,
  getRoomImages,
  reorderRoomImages,
  setRoomImageThumbnail,
  uploadRoomImages,
} from '../../api/admin';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';
import EmptyState from '../../components/EmptyState';

const ACCEPT = 'image/png,image/jpeg,image/jpg,image/webp,image/gif';

export default function AdminRoomImagesPage() {
  const { roomId } = useParams();
  const fileRef = useRef(null);
  /** 드래그 중인 사진의 id. state가 아니라 ref인 이유: 드래그 도중 리렌더될 필요가 없어서. */
  const draggedIdRef = useRef(null);

  const [room, setRoom] = useState(null);
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [savingOrder, setSavingOrder] = useState(false);
  const [dragOverId, setDragOverId] = useState(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    Promise.all([getAdminRoomDetail(roomId), getRoomImages(roomId)])
      .then(([roomRes, imageRes]) => {
        setRoom(roomRes);
        setImages(Array.isArray(imageRes) ? imageRes : imageRes?.content || []);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [roomId]);

  useEffect(load, [load]);

  const handleFiles = async (files) => {
    if (!files || files.length === 0) return;
    setError('');
    setNotice('');
    setUploading(true);
    try {
      // multipart/form-data, 필드명 "images" — 여러 장을 한 번에 올립니다.
      await uploadRoomImages(roomId, files);
      setNotice(`${files.length}장을 올렸습니다.`);
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setUploading(false);
      if (fileRef.current) fileRef.current.value = '';
    }
  };

  const remove = async (imageId) => {
    if (!window.confirm('이 사진을 지울까요?')) return;
    setError('');
    try {
      await deleteRoomImage(roomId, imageId);
      setImages((prev) => prev.filter((img) => img.id !== imageId));
      setNotice('사진을 지웠습니다.');
    } catch (err) {
      setError(err.message);
    }
  };

  /** 이 사진을 대표 사진으로 지정합니다. 화면은 먼저 바꾸고, 실패하면 되돌립니다. */
  const makeThumbnail = async (imageId) => {
    setError('');
    const prevImages = images;
    setImages((cur) => cur.map((img) => ({ ...img, thumbnail: img.id === imageId })));
    try {
      const updated = await setRoomImageThumbnail(roomId, imageId);
      setImages(updated);
      setNotice('대표 사진을 바꿨습니다.');
    } catch (err) {
      setImages(prevImages);
      setError(err.message);
    }
  };

  const handleDragStart = (imageId) => (e) => {
    draggedIdRef.current = imageId;
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDragOverCell = (imageId) => (e) => {
    e.preventDefault();
    if (draggedIdRef.current == null || draggedIdRef.current === imageId) return;
    setDragOverId((cur) => (cur === imageId ? cur : imageId));
  };

  const handleDragEnd = () => {
    draggedIdRef.current = null;
    setDragOverId(null);
  };

  /** 드롭한 자리로 사진을 옮기고, 새 순서를 서버에 저장합니다. */
  const handleDrop = (targetImageId) => async (e) => {
    e.preventDefault();
    const draggedId = draggedIdRef.current;
    draggedIdRef.current = null;
    setDragOverId(null);
    if (draggedId == null || draggedId === targetImageId) return;

    const prevImages = images;
    const fromIndex = prevImages.findIndex((img) => img.id === draggedId);
    const toIndex = prevImages.findIndex((img) => img.id === targetImageId);
    if (fromIndex === -1 || toIndex === -1) return;

    const reordered = [...prevImages];
    const [moved] = reordered.splice(fromIndex, 1);
    reordered.splice(toIndex, 0, moved);

    setImages(reordered);
    setSavingOrder(true);
    setError('');
    try {
      const updated = await reorderRoomImages(
        roomId,
        reordered.map((img) => img.id)
      );
      setImages(updated);
    } catch (err) {
      setImages(prevImages);
      setError(err.message);
    } finally {
      setSavingOrder(false);
    }
  };

  if (loading) return <Spinner />;

  return (
    <>
      <div className="admin-topbar">
        <div>
          <p className="eyebrow">Photos</p>
          <h1>{room?.name || '묘소'} · 사진 관리</h1>
          <p className="tiny dim mt-8">
            사진을 끌어다 놓으면 순서가 바뀝니다. 각 사진 왼쪽 위의 ‘대표’ 버튼을 누르면 그 사진이 대표 사진이 됩니다.
          </p>
        </div>
        <div className="row gap-8">
          <Link to={`/admin/rooms/${roomId}`} className="btn btn-ghost">
            ← 묘소 상세
          </Link>
          <Link to="/admin/rooms" className="btn btn-outline">
            목록으로
          </Link>
        </div>
      </div>

      <Alert>{error}</Alert>
      <Alert tone="success">{notice}</Alert>

      <div
        className="dropzone mb-24"
        role="button"
        tabIndex={0}
        onClick={() => fileRef.current?.click()}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') fileRef.current?.click();
        }}
        onDragOver={(e) => e.preventDefault()}
        onDrop={(e) => {
          e.preventDefault();
          handleFiles(e.dataTransfer.files);
        }}
      >
        <p className="serif" style={{ fontSize: 17, color: 'var(--bone)', marginBottom: 8 }}>
          {uploading ? '올리는 중…' : '사진을 여기로 끌어다 놓거나 눌러서 고르십시오'}
        </p>
        <p className="tiny">PNG · JPG · WEBP · GIF · 여러 장 동시 선택 가능</p>
        <input
          ref={fileRef}
          type="file"
          accept={ACCEPT}
          multiple
          hidden
          onChange={(e) => handleFiles(e.target.files)}
        />
      </div>

      {images.length === 0 ? (
        <EmptyState mark="▣" title="등록된 사진이 없습니다" description="첫 사진을 올려 묘소의 얼굴을 만들어 주십시오." />
      ) : (
        <div className="image-grid">
          {images.map((img, i) => (
            <div
              className={`image-cell${dragOverId === img.id ? ' drag-over' : ''}`}
              key={img.id}
              draggable
              onDragStart={handleDragStart(img.id)}
              onDragOver={handleDragOverCell(img.id)}
              onDrop={handleDrop(img.id)}
              onDragEnd={handleDragEnd}
            >
              <img src={img.url} alt={`${room?.name} 사진 ${i + 1}`} draggable={false} />
              <button
                type="button"
                className={`image-thumb-btn${img.thumbnail ? ' active' : ''}`}
                onClick={() => makeThumbnail(img.id)}
                disabled={img.thumbnail || savingOrder}
                title={img.thumbnail ? '현재 대표 사진입니다' : '이 사진을 대표 사진으로 지정'}
              >
                대표
              </button>
              <button type="button" className="image-del" onClick={() => remove(img.id)} disabled={savingOrder}>
                지우기
              </button>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
