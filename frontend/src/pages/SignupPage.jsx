import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signup } from '../api/auth';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';

export default function SignupPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm] = useState({ email: '', password: '', passwordConfirm: '', name: '', phone: '' });
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
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="auth-head">
          <p className="eyebrow">Application</p>
          <h1>입주 신청</h1>
          <p>이 아래에 남긴 이름은 지워지지 않습니다.</p>
        </div>

        <Alert>{error}</Alert>

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="name">이름</label>
            <input id="name" type="text" required value={form.name} onChange={update('name')} placeholder="홍길동" />
          </div>
          <div className="field">
            <label htmlFor="email">이메일</label>
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
          <div className="field">
            <label htmlFor="phone">연락처</label>
            <input
              id="phone"
              type="tel"
              required
              value={form.phone}
              onChange={update('phone')}
              placeholder="010-0000-0000"
            />
            <span className="field-hint">기록 분실 시 본인 확인에 사용됩니다.</span>
          </div>
          <div className="field">
            <label htmlFor="pw">비밀번호</label>
            <input
              id="pw"
              type="password"
              autoComplete="new-password"
              required
              value={form.password}
              onChange={update('password')}
            />
          </div>
          <div className="field">
            <label htmlFor="pw2">비밀번호 확인</label>
            <input
              id="pw2"
              type="password"
              autoComplete="new-password"
              required
              value={form.passwordConfirm}
              onChange={update('passwordConfirm')}
            />
          </div>
          <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={submitting}>
            {submitting ? '기록하는 중…' : '이름 남기기'}
          </button>
        </form>

        <div className="auth-foot">
          <span className="dim">이미 기록이 있습니까?</span>
          <Link to="/login" className="link">
            입주 기록으로
          </Link>
        </div>
      </div>
    </div>
  );
}
