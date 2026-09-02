import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { changePassword, withdraw } from '../api/auth';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';

export default function EditProfilePage() {
  const navigate = useNavigate();
  const { profile, userId, token, logout } = useAuth();

  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState('');
  const [done, setDone] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [confirmWithdraw, setConfirmWithdraw] = useState(false);

  const handlePassword = async (e) => {
    e.preventDefault();
    setError('');
    setDone('');

    if (password.length < 4) {
      setError('비밀번호는 4자 이상이어야 합니다.');
      return;
    }
    if (password !== confirm) {
      setError('두 비밀번호가 서로 다릅니다.');
      return;
    }

    setSubmitting(true);
    try {
      // 서버 스펙상 헤더가 아니라 body의 userToken으로 사용자를 식별합니다.
      await changePassword(token, password);
      setDone('비밀번호를 바꾸었습니다.');
      setPassword('');
      setConfirm('');
    } catch (err) {
      setError(err.message || '비밀번호를 바꾸지 못했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleWithdraw = async () => {
    setError('');
    setSubmitting(true);
    try {
      await withdraw(userId);
      logout();
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message || '탈퇴 처리에 실패했습니다.');
      setSubmitting(false);
    }
  };

  return (
    <div className="container-narrow page">
      <p className="eyebrow">Record</p>
      <h1>기록 수정</h1>
      <div className="rule mb-24" />

      <Alert>{error}</Alert>
      <Alert tone="success">{done}</Alert>

      <div className="panel mb-24">
        <p className="eyebrow eyebrow-ash">기본 정보</p>
        <div className="price-line">
          <span>이름</span>
          <span>{profile?.name || '-'}</span>
        </div>
        <div className="price-line">
          <span>이메일</span>
          <span className="mono">{profile?.email || '-'}</span>
        </div>
        <div className="price-line" style={{ borderBottom: 0 }}>
          <span>연락처</span>
          <span>{profile?.phone || '-'}</span>
        </div>
        <p className="tiny dim mt-16">
          이름·이메일·연락처는 현재 서버에서 수정 API를 제공하지 않습니다. 변경이 필요하면 산장지기에게 문의하십시오.
        </p>
      </div>

      <div className="panel mb-24">
        <p className="eyebrow eyebrow-ash">비밀번호 변경</p>
        <form onSubmit={handlePassword} className="mt-16">
          <div className="field">
            <label htmlFor="np">새 비밀번호</label>
            <input
              id="np"
              type="password"
              autoComplete="new-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <div className="field">
            <label htmlFor="np2">새 비밀번호 확인</label>
            <input
              id="np2"
              type="password"
              autoComplete="new-password"
              required
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
            />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
            {submitting ? '바꾸는 중…' : '비밀번호 바꾸기'}
          </button>
        </form>
      </div>

      <div className="panel" style={{ borderColor: 'var(--hair-blood)' }}>
        <p className="eyebrow">위험 구역</p>
        <p className="tiny muted mb-16">
          탈퇴하면 이 이름은 산에서 지워집니다. 남아 있는 예약과 증언도 함께 사라집니다.
        </p>
        {confirmWithdraw ? (
          <div className="row gap-8">
            <button type="button" className="btn btn-danger" onClick={handleWithdraw} disabled={submitting}>
              정말 지웁니다
            </button>
            <button type="button" className="btn btn-ghost" onClick={() => setConfirmWithdraw(false)}>
              그만두기
            </button>
          </div>
        ) : (
          <button type="button" className="btn btn-danger" onClick={() => setConfirmWithdraw(true)}>
            이름 지우기 (회원 탈퇴)
          </button>
        )}
      </div>
    </div>
  );
}
