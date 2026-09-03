import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ROLE, useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';

/* ------------------------------------------------------------------
 * 로그인 화면.
 *
 * 여기가 관리자/사용자 분기의 출발점입니다.
 *   login() -> JWT 저장 -> GET /api/accounts/{id} 로 role 확인
 *   role === 'ADMIN'  -> /admin (관리자 대시보드)
 *   role === 'USER'   -> 원래 가려던 곳, 없으면 /
 *
 * 로그인 전에 보호된 페이지로 접근했다면 location.state.from 에 그 주소가
 * 담겨 오는데, 관리자는 그 주소가 사용자 화면일 수 있으므로 무시하고
 * 항상 관리자 구역으로 보냅니다.
 * ------------------------------------------------------------------ */
export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [warning, setWarning] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const from = location.state?.from?.pathname;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setWarning('');
    setSubmitting(true);
    try {
      const { role } = await login(email.trim(), password);

      // role === null 이면 로그인은 됐지만 GET /api/accounts/{id} 가 실패한 경우입니다.
      // 이때 조용히 홈으로 보내면 "관리자인데 왜 안 가지?"가 되므로 진단 화면으로 안내합니다.
      if (role === null) {
        setWarning('로그인은 되었지만 계정 권한을 확인하지 못했습니다.');
        return;
      }

      if (role === ROLE.ADMIN) {
        navigate('/admin', { replace: true });
      } else {
        navigate(from && !from.startsWith('/admin') ? from : '/', { replace: true });
      }
    } catch (err) {
      setError(err.message || '아이디 또는 비밀번호를 확인해 주십시오.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="auth-head">
          <p className="eyebrow">Entry Record</p>
          <h1>입주 기록</h1>
          <p>이름을 남긴 자만이 이 문을 지날 수 있습니다.</p>
        </div>

        <Alert>{error}</Alert>
        {warning && (
          <Alert tone="info">
            {warning}{' '}
            <Link to="/whoami" className="link">
              권한 진단 열기
            </Link>
          </Alert>
        )}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="email">이메일</label>
            <input
              id="email"
              type="email"
              autoComplete="username"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
            />
          </div>
          <div className="field">
            <label htmlFor="password">비밀번호</label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
            />
          </div>
          <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={submitting}>
            {submitting ? '확인하는 중…' : '입주하기'}
          </button>
        </form>

        <div className="auth-foot">
          <Link to="/signup" className="link">
            입주 신청
          </Link>
          <span className="dim">·</span>
          <Link to="/find-account" className="link">
            기록을 잃어버렸습니다
          </Link>
        </div>
      </div>
    </div>
  );
}
