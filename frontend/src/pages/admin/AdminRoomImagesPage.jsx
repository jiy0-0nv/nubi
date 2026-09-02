import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { deleteRoomImage, getAdminRoomDetail, getRoomImages, uploadRoomImages } from '../../api/admin';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';
import EmptyState from '../../components/EmptyState';

const ACCEPT = 'image/png,image/jpeg,image/jpg,image/webp,image/gif';

export default function AdminRoomImagesPage() {
  const { roomId } = useParams();
  const fileRef = useRef(null);

  const [room, setRoom] = useState(null);
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
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

  if (loading) return <Spinner />;

  return (
    <>
      <div className="admin-topbar">
        <div>
          <p className="eyebrow">Photos</p>
          <h1>{room?.name || '산장'} · 사진 관리</h1>
          <p className="tiny dim mt-8">맨 처음 사진이 목록에서 대표 사진으로 쓰입니다.</p>
        </div>
        <div className="row gap-8">
          <Link to={`/admin/rooms/${roomId}`} className="btn btn-ghost">
            ← 산장 상세
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
        <EmptyState mark="▣" title="등록된 사진이 없습니다" description="첫 사진을 올려 산장의 얼굴을 만들어 주십시오." />
      ) : (
        <div className="image-grid">
          {images.map((img, i) => (
            <div className="image-cell" key={img.id}>
              <img src={img.url} alt={`${room?.name} 사진 ${i + 1}`} />
              {i === 0 && (
                <span style={{ position: 'absolute', left: 8, top: 8 }}>
                  <span className="badge badge-blood">대표</span>
                </span>
              )}
              <button type="button" className="image-del" onClick={() => remove(img.id)}>
                지우기
              </button>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
