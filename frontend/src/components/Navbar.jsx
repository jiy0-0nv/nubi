import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { updateRole } from '../api/auth';

const THEME_KEY = 'nubi-theme';

function getInitialTheme() {
  return document.documentElement.dataset.theme === 'light' ? 'light' : 'dark';
}

/**
 * 사용자 영역 상단 헤더.
 * 관리자 영역(/admin)은 이 헤더를 쓰지 않고 자체 사이드바 셸을 씁니다.
 * 다만 ADMIN으로 로그인한 채 사용자 화면을 볼 수도 있으므로,
 * 그때는 "호스트 화면" 진입 링크를 하나 노출해 줍니다.
 */
export default function Navbar() {
  const { isAuthenticated, isAdmin, userId, profile, logout, reloadProfile } = useAuth();
  const navigate = useNavigate();
  const [becomingHost, setBecomingHost] = useState(false);
  const [theme, setTheme] = useState(getInitialTheme);

  // 묘지 컨셉(다크)이 기본. 라이트모드는 평범한 숙소 사이트처럼 보이는 이스터에그.
  const toggleTheme = () => {
    const next = theme === 'light' ? 'dark' : 'light';
    setTheme(next);
    document.documentElement.dataset.theme = next;
    try {
      localStorage.setItem(THEME_KEY, next);
    } catch {
      /* 저장 실패해도 이번 세션 화면 전환은 그대로 유지됩니다. */
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/', { replace: true });
  };

  const handleBecomeHost = async () => {
    if (!window.confirm('호스트로 등록하시겠습니까?')) return;
    setBecomingHost(true);
    try {
      await updateRole(userId, 'ADMIN');
      await reloadProfile();
      navigate('/admin');
    } catch (err) {
      window.alert(err.message || '호스트 등록에 실패했습니다.');
    } finally {
      setBecomingHost(false);
    }
  };

  return (
    <header className="site-header">
      <div className="container row" style={{ width: '100%', gap: 26 }}>
        <Link to="/" className="row" style={{ gap: 10, flexShrink: 0 }} aria-label="누비 홈으로">
          <span className="brand-icon" aria-hidden="true" />
          <span className="brand-mark">
            NUBI <span className="brand-mark-pink"> MYOBI</span>
          </span>
        </Link>

        <nav className="nav">
          <NavLink to="/rooms" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
            SEARCH
          </NavLink>
          {isAuthenticated && (
            <NavLink to="/mypage/bookings" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
              예약 내역
            </NavLink>
          )}
        </nav>

        <div className="row" style={{ marginLeft: 'auto', gap: 18, flexShrink: 0 }}>
          {isAdmin ? (
            <NavLink to="/admin" className="nav-link nav-link-strong">
              호스트 화면
            </NavLink>
          ) : isAuthenticated ? (
            <button
              type="button"
              className="nav-link nav-link-strong"
              style={{ background: 'none', border: 0, cursor: 'pointer' }}
              onClick={handleBecomeHost}
              disabled={becomingHost}
            >
              {becomingHost ? '등록하는 중…' : '호스팅하기'}
            </button>
          ) : (
            <Link to="/signup" className="nav-link nav-link-strong">
              호스팅하기
            </Link>
          )}
          <button
            type="button"
            className="theme-toggle"
            onClick={toggleTheme}
            aria-label={theme === 'light' ? '다크모드로 전환' : '라이트모드로 전환'}
            title={theme === 'light' ? '다크모드로 전환' : '라이트모드로 전환'}
          >
            {theme === 'light' ? '☾' : '☀'}
          </button>

          {isAuthenticated ? (
            <div className="login-pill">
              <Link to="/mypage" className="tiny" style={{ color: 'var(--bone)', fontWeight: 500, whiteSpace: 'nowrap' }}>
                {profile?.name ? `${profile.name} 님` : '마이페이지'}
              </Link>
              <button
                type="button"
                className="logout-btn"
                onClick={handleLogout}
                aria-label="로그아웃"
                title="로그아웃"
              >
                로그아웃
              </button>
            </div>
          ) : (
            <Link to="/login" className="login-pill">
              <span className="tiny" style={{ color: 'var(--ash)' }}>
                로그인
              </span>
              <span className="login-pill-avatar" aria-hidden="true" />
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}
