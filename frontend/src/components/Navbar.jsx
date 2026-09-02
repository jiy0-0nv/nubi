import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * 사용자 영역 상단 헤더.
 * 관리자 영역(/admin)은 이 헤더를 쓰지 않고 자체 사이드바 셸을 씁니다.
 * 다만 ADMIN으로 로그인한 채 사용자 화면을 볼 수도 있으므로,
 * 그때는 "관리자 구역" 진입 링크를 하나 노출해 줍니다.
 */
export default function Navbar() {
  const { isAuthenticated, isAdmin, profile, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/', { replace: true });
  };

  return (
    <header className="site-header">
      <div className="container row" style={{ width: '100%' }}>
        <Link to="/" className="brand" aria-label="누비 홈으로">
          <span className="brand-mark">
            누<em>비</em>
          </span>
          <span className="brand-sub">Nubi Mountain Lodge</span>
        </Link>

        <nav className="nav">
          <NavLink to="/rooms" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
            산장 둘러보기
          </NavLink>

          {isAuthenticated ? (
            <>
              {isAdmin && (
                <NavLink to="/admin" className="nav-link blood-text">
                  관리자 구역
                </NavLink>
              )}
              <NavLink to="/mypage" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
                {profile?.name ? `${profile.name} 님` : '마이페이지'}
              </NavLink>
              <button type="button" className="btn btn-ghost btn-sm" onClick={handleLogout}>
                나가기
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className="nav-link">
                입산 기록
              </NavLink>
              <Link to="/signup" className="btn btn-primary btn-sm">
                입산 신청
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
