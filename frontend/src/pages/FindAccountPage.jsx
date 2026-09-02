import { useState } from 'react';
import { Link } from 'react-router-dom';
import { findId, findPassword } from '../api/auth';
import Alert from '../components/Alert';

export default function FindAccountPage() {
  const [tab, setTab] = useState('id');

  // 아이디 찾기
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [foundEmail, setFoundEmail] = useState('');

  // 비밀번호 찾기
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);

  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const reset = (next) => {
    setTab(next);
    setError('');
    setFoundEmail('');
    setSent(false);
  };

  const handleFindId = async (e) => {
    e.preventDefault();
    setError('');
    setFoundEmail('');
    setSubmitting(true);
    try {
      const result = await findId(name.trim(), phone.trim());
      setFoundEmail(typeof result === 'string' ? result : result?.email || '');
    } catch (err) {
      setError(err.message || '일치하는 기록을 찾지 못했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleFindPassword = async (e) => {
    e.preventDefault();
    setError('');
    setSent(false);
    setSubmitting(true);
    try {
      await findPassword(email.trim());
      setSent(true);
    } catch (err) {
      setError(err.message || '임시 비밀번호를 보내지 못했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="auth-head">
          <p className="eyebrow">Lost Record</p>
          <h1>잃어버린 기록</h1>
          <p>남겨둔 흔적으로 되찾을 수 있습니다.</p>
        </div>

        <div className="tabs">
          <button type="button" className={`tab${tab === 'id' ? ' active' : ''}`} onClick={() => reset('id')}>
            이메일 찾기
          </button>
          <button type="button" className={`tab${tab === 'pw' ? ' active' : ''}`} onClick={() => reset('pw')}>
            비밀번호 재발급
          </button>
        </div>

        <Alert>{error}</Alert>

        {tab === 'id' ? (
          <form onSubmit={handleFindId}>
            <div className="field">
              <label htmlFor="fname">이름</label>
              <input id="fname" type="text" required value={name} onChange={(e) => setName(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="fphone">연락처</label>
              <input
                id="fphone"
                type="tel"
                required
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="010-0000-0000"
              />
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
              {submitting ? '뒤지는 중…' : '이메일 찾기'}
            </button>
            {foundEmail && (
              <div className="mt-24">
                <Alert tone="success">
                  기록을 찾았습니다 — <strong className="mono">{foundEmail}</strong>
                </Alert>
                <Link to="/login" className="btn btn-outline btn-block">
                  이 계정으로 입산하기
                </Link>
              </div>
            )}
          </form>
        ) : (
          <form onSubmit={handleFindPassword}>
            <div className="field">
              <label htmlFor="femail">이메일</label>
              <input
                id="femail"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
              />
              <span className="field-hint">임시 비밀번호를 해당 주소로 보내드립니다.</span>
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
              {submitting ? '보내는 중…' : '임시 비밀번호 받기'}
            </button>
            {sent && (
              <div className="mt-24">
                <Alert tone="success">메일을 보냈습니다. 받은 편지함을 확인해 주십시오.</Alert>
              </div>
            )}
          </form>
        )}

        <div className="auth-foot">
          <Link to="/login" className="link">
            ← 입산 기록으로 돌아가기
          </Link>
        </div>
      </div>
    </div>
  );
}
