import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

/* ------------------------------------------------------------------
 * 관리자 전용 셸.
 *
 * 사용자 영역(UserLayout)과 완전히 분리된 레이아웃입니다.
 *  - 상단 헤더 대신 좌측 사이드바
 *  - 여기 진입 자체는 AdminRoute가 role === 'ADMIN' 으로 막습니다.
 * ------------------------------------------------------------------ */

const NAV = [
  { to: '/admin', end: true, label: '대시보드', mark: '◈' },
  { to: '/admin/rooms', end: false, label: '산장 관리', mark: '▣' },
  { to: '/admin/bookings', end: false, label: '예약 관리', mark: '❑' },
];

export default function AdminLayout() {
  const { profile, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="admin-shell">
      <aside className="admin-side">
        <div className="admin-brand">
          <Link to="/admin" className="admin-brand-mark">
            누비 산장
          </Link>
          <br />
          <span className="admin-brand-tag">Keeper Only</span>
        </div>

        <nav className="admin-nav">
          {NAV.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `admin-nav-link${isActive ? ' active' : ''}`}
            >
              <span aria-hidden="true">{item.mark}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="admin-side-foot">
          <p className="tiny" style={{ color: 'var(--bone)' }}>
            {profile?.name || '산장지기'}
          </p>
          <p className="tiny dim mono" style={{ marginBottom: 12 }}>
            {profile?.email}
          </p>
          <Link to="/" className="btn btn-ghost btn-sm" style={{ paddingLeft: 0 }}>
            사용자 화면 보기 →
          </Link>
          <br />
          <button type="button" className="btn btn-ghost btn-sm" style={{ paddingLeft: 0 }} onClick={handleLogout}>
            로그아웃
          </button>
        </div>
      </aside>

      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}
