import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Spinner from './Spinner';

/* ------------------------------------------------------------------
 * 라우트 가드 3종.
 *
 *  ProtectedRoute : 로그인만 되어 있으면 통과 (일반 사용자 영역)
 *  AdminRoute     : 로그인 + role === 'ADMIN' 이어야 통과 (관리자 영역)
 *  GuestOnlyRoute : 이미 로그인했다면 각자의 홈으로 되돌려보냄 (로그인/회원가입)
 *
 * 공통 규칙: 토큰은 있는데 아직 GET /api/accounts/{id}로 role을 못 읽었으면
 *           (roleResolved === false) 판단을 미루고 로딩을 보여줍니다.
 *           안 그러면 새로고침 직후 관리자가 잠깐 쫓겨나는 깜빡임이 생겨요.
 * ------------------------------------------------------------------ */

function Booting({ label }) {
  return <Spinner label={label} />;
}

export function ProtectedRoute({ children }) {
  const { isAuthenticated, booting } = useAuth();
  const location = useLocation();

  if (booting) return <Booting label="확인하는 중" />;

  if (!isAuthenticated) {
    // 로그인 후 원래 가려던 곳으로 돌려보내기 위해 위치를 실어 보냅니다.
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  return children;
}

export function AdminRoute({ children }) {
  const { isAuthenticated, isAdmin, roleResolved, booting } = useAuth();
  const location = useLocation();

  if (booting || (isAuthenticated && !roleResolved)) return <Booting label="권한 확인 중" />;

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  if (!isAdmin) {
    // 로그인은 했지만 USER 권한 — 관리자 영역은 존재 자체를 알려주지 않고 차단 화면으로.
    return <Navigate to="/forbidden" replace />;
  }
  return children;
}

export function GuestOnlyRoute({ children }) {
  const { isAuthenticated, isAdmin, roleResolved, booting } = useAuth();

  if (booting || (isAuthenticated && !roleResolved)) return <Booting label="확인하는 중" />;

  if (isAuthenticated) {
    return <Navigate to={isAdmin ? '/admin' : '/'} replace />;
  }
  return children;
}
