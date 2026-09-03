import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { updateRole } from '../api/auth';

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
            NUBI <emp> MYOBI</emp>
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
          {/* <span className="mono dim" style={{ fontSize: 12 }}>
            TODO: 다크모드 전환 버튼
          </span> */}

          {isAuthenticated ? (
            <div className="login-pill">
              <Link to="/mypage" className="tiny" style={{ color: 'var(--bone)', fontWeight: 500, whiteSpace: 'nowrap' }}>
                {profile?.name ? `${profile.name} 님` : '마이페이지'}
              </Link>
              <button
                type="button"
                className="login-pill-avatar filled"
                onClick={handleLogout}
                aria-label="나가기"
                title="나가기"
              >
                {profile?.name ? profile.name[0] : '?'}
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
