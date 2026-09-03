import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signup } from '../api/auth';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';
import AuthBrandPanel from '../components/AuthBrandPanel';

export default function SignupPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm] = useState({ email: '', password: '', passwordConfirm: '', name: '', phone: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (form.password !== form.passwordConfirm) {
      setError('비밀번호가 서로 다릅니다.');
      return;
    }
    if (form.password.length < 4) {
      setError('비밀번호는 4자 이상이어야 합니다.');
      return;
    }

    setSubmitting(true);
    try {
      await signup({
        email: form.email.trim(),
        password: form.password,
        name: form.name.trim(),
        phone: form.phone.trim(),
      });
      // 가입 직후 바로 로그인까지 이어서 처리합니다. (신규 계정은 항상 USER 권한)
      await login(form.email.trim(), form.password);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message || '가입에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-split">
      {/* ---------- 좌측 브랜드 패널 ---------- */}
      <AuthBrandPanel />

      {/* ---------- 우측 폼 패널 ---------- */}
      <div className="auth-split-form">
        <div className="auth-split-form-inner">
          <div className="auth-tabs">
            <Link to="/login" className="auth-tab">
              로그인
            </Link>
            <span className="auth-tab active">회원가입</span>
          </div>

          <Alert>{error}</Alert>

          <form onSubmit={handleSubmit}>
            <label htmlFor="name" className="label" style={{ display: 'block', marginBottom: 9 }}>
              이름
            </label>
            <div className="input-box">
              <input id="name" type="text" required value={form.name} onChange={update('name')} placeholder="홍길동" />
            </div>

            <label htmlFor="email" className="label" style={{ display: 'block', marginBottom: 9 }}>
              이메일
            </label>
            <div className="input-box">
              <input
                id="email"
                type="email"
                autoComplete="username"
                required
                value={form.email}
                onChange={update('email')}
                placeholder="you@example.com"
              />
            </div>

            <label htmlFor="phone" className="label" style={{ display: 'block', marginBottom: 9 }}>
              연락처
            </label>
            <div className="input-box" style={{ marginBottom: 8 }}>
              <input
                id="phone"
                type="tel"
                required
                value={form.phone}
                onChange={update('phone')}
                placeholder="010-0000-0000"
              />
            </div>

            <label htmlFor="pw" className="label" style={{ display: 'block', marginBottom: 9 }}>
              비밀번호
            </label>
            <div className="input-box">
              <input
                id="pw"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                required
                value={form.password}
                onChange={update('password')}
              />
              <button
                type="button"
                className="input-box-toggle"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 표시'}
              >
                {showPassword ? '숨김' : '표시'}
              </button>
            </div>

            <label htmlFor="pw2" className="label" style={{ display: 'block', marginBottom: 9 }}>
              비밀번호 확인
            </label>
            <div className="input-box">
              <input
                id="pw2"
                type={showPasswordConfirm ? 'text' : 'password'}
                autoComplete="new-password"
                required
                value={form.passwordConfirm}
                onChange={update('passwordConfirm')}
              />
              <button
                type="button"
                className="input-box-toggle"
                onClick={() => setShowPasswordConfirm((v) => !v)}
                aria-label={showPasswordConfirm ? '비밀번호 숨기기' : '비밀번호 표시'}
              >
                {showPasswordConfirm ? '숨김' : '표시'}
              </button>
            </div>

            <button type="submit" className="btn btn-primary btn-block btn-lg" style={{ marginTop: 8 }} disabled={submitting}>
              {submitting ? '기록하는 중…' : '회원가입'}
            </button>
          </form>

          <p className="tiny dim" style={{ lineHeight: 1.7, marginTop: 26 }}>
            계속하면 이용약관과 개인정보 처리방침에 동의하게 됩니다. 만 19세 이상만 가입할 수
            있습니다.
          </p>
        </div>
      </div>
    </div>
  );
}
