import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * USER 권한으로 /admin 에 접근했을 때 나오는 화면.
 * AdminRoute가 여기로 보냅니다.
 */
export default function ForbiddenPage() {
  const { isAuthenticated, profile } = useAuth();

  return (
    <div className="denied">
      <div>
        <div className="denied-mark" aria-hidden="true">
          ⛔
        </div>
        <p className="eyebrow">Access Denied · 403</p>
        <h1>여기는 묘지기의 구역입니다</h1>
        <p className="muted mt-16" style={{ maxWidth: 460, margin: '16px auto 0' }}>
          {isAuthenticated
            ? `${profile?.name || '당신'}의 권한으로는 이 문을 열 수 없습니다. 관리자(ADMIN) 계정으로만 들어갈 수 있습니다.`
            : '먼저 입주 기록을 남기십시오.'}
        </p>
        <div className="row gap-8 mt-24" style={{ justifyContent: 'center' }}>
          <Link to="/" className="btn btn-primary">
            무덤 밖으로 나가기
          </Link>
          {isAuthenticated ? (
            <Link to="/whoami" className="btn btn-outline">
              왜 막혔는지 확인하기
            </Link>
          ) : (
            <Link to="/login" className="btn btn-outline">
              입주 기록
            </Link>
          )}
        </div>
      </div>
    </div>
  );
}
