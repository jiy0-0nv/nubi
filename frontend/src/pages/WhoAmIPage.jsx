import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { checkIsAdmin, getAccount, updateRole } from '../api/auth';
import { getToken } from '../api/client';
import { ROLE, normalizeRole, useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';
import Badge from '../components/Badge';

/* ------------------------------------------------------------------
 * 권한 진단 화면 (/whoami)
 *
 * "분명 관리자 계정인데 왜 /admin 으로 안 가지?" 를 한 화면에서 답하기 위한
 * 도구입니다. 서버가 실제로 내려주는 값을 그대로 보여줍니다.
 * ------------------------------------------------------------------ */

function decode(token) {
  try {
    const b64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(decodeURIComponent(escape(atob(b64))));
  } catch {
    return null;
  }
}

function Row({ label, value, ok }) {
  return (
    <div className="price-line">
      <span>{label}</span>
      <span className="mono" style={{ color: ok === false ? 'var(--dead-text)' : 'var(--bone)', textAlign: 'right' }}>
        {value}
      </span>
    </div>
  );
}

export default function WhoAmIPage() {
  const { profile, profileStatus, profileError, isAdmin, role, reloadProfile } = useAuth();
  const [raw, setRaw] = useState(null);
  const [rawError, setRawError] = useState(null);
  const [adminCheck, setAdminCheck] = useState(null);
  const [adminCheckError, setAdminCheckError] = useState(null);
  const [applying, setApplying] = useState(false);
  const [applyError, setApplyError] = useState('');
  const [applied, setApplied] = useState(false);

  const token = getToken();
  const payload = token ? decode(token) : null;
  const userId = payload?.sub ? Number(payload.sub) : null;

  // 컨텍스트를 거치지 않고 한 번 더 직접 호출해서 "날것의 응답"을 보여줍니다.
  useEffect(() => {
    if (!userId) return;
    getAccount(userId)
      .then(setRaw)
      .catch((err) => setRawError({ status: err?.status ?? 0, message: err?.message }));
    checkIsAdmin(userId)
      .then(setAdminCheck)
      .catch((err) => setAdminCheckError({ status: err?.status ?? 0, message: err?.message }));
  }, [userId]);

  /**
   * 관리자 권한 자가 신청. 지금 서버는 로그인만 했으면 누구나 아무 계정의
   * role이나 바꿀 수 있게 열려 있어서(ADMIN이 하나도 없는 상태의 부트스트랩용),
   * 본인 계정을 스스로 ADMIN으로 올릴 수 있습니다.
   */
  const applyForAdmin = async () => {
    if (!userId) return;
    setApplying(true);
    setApplyError('');
    try {
      await updateRole(userId, 'ADMIN');
      await Promise.all([reloadProfile(), checkIsAdmin(userId).then(setAdminCheck)]);
      setApplied(true);
    } catch (err) {
      setApplyError(err.message || '권한 변경에 실패했습니다.');
    } finally {
      setApplying(false);
    }
  };

  const roleField = raw ? raw.role : profile?.role;
  const hasRoleField = raw ? Object.prototype.hasOwnProperty.call(raw, 'role') : undefined;

  return (
    <div className="container-narrow page">
      <p className="eyebrow">Diagnostics</p>
      <h1>권한 진단</h1>
      <div className="rule mb-24" />

      <div className="mb-24">
        {isAdmin ? (
          <Alert tone="success">이 계정은 ADMIN 으로 인식됩니다. 로그인하면 /admin 으로 이동합니다.</Alert>
        ) : profileStatus === 'error' ? (
          <Alert tone="error">
            계정 정보를 읽지 못해 권한을 판정할 수 없습니다 (HTTP {profileError?.status}). {profileError?.message}
          </Alert>
        ) : hasRoleField === false ? (
          <Alert tone="error">서버 응답에 role 필드가 없습니다. 백엔드 AccountResponseDTO를 확인해 주십시오.</Alert>
        ) : (
          <Alert tone="info">
            이 계정은 USER 로 인식됩니다. 서버가 내려준 role 값이 &quot;ADMIN&quot;이 아니면 관리자 화면으로 갈 수 없습니다.
          </Alert>
        )}
      </div>

      <div className="panel mb-24">
        <p className="eyebrow eyebrow-ash">1. 토큰 (JWT payload)</p>
        <Row label="sub (userId)" value={payload?.sub ?? '없음'} ok={Boolean(payload?.sub)} />
        <Row label="email" value={payload?.email ?? '없음'} />
        <Row
          label="만료"
          value={payload?.exp ? new Date(payload.exp * 1000).toLocaleString('ko-KR') : '없음'}
          ok={payload?.exp ? payload.exp * 1000 > Date.now() : undefined}
        />
        <p className="tiny dim mt-16">
          이 백엔드의 JWT에는 role 클레임이 없습니다. 권한은 아래 계정 조회 응답에서만 알 수 있습니다.
        </p>
      </div>

      <div className="panel mb-24">
        <p className="eyebrow eyebrow-ash">2. GET /api/accounts/{userId ?? '{id}'}</p>
        {rawError ? (
          <Alert tone="error">
            HTTP {rawError.status} — {rawError.message}
          </Alert>
        ) : !raw ? (
          <p className="tiny dim">불러오는 중…</p>
        ) : (
          <>
            <Row label="role 필드 존재" value={hasRoleField ? '있음' : '없음'} ok={hasRoleField} />
            <Row label="role 원본 값" value={JSON.stringify(roleField)} ok={normalizeRole(raw) === ROLE.ADMIN} />
            <Row label="정규화 결과" value={normalizeRole(raw)} ok={normalizeRole(raw) === ROLE.ADMIN} />
            <p className="eyebrow eyebrow-ash mt-24">응답 전문</p>
            <pre
              className="mono"
              style={{
                background: 'var(--void)',
                border: '1px solid var(--hair)',
                borderRadius: 4,
                padding: 14,
                overflowX: 'auto',
                margin: 0,
                color: 'var(--ash)',
              }}
            >
              {JSON.stringify(raw, null, 2)}
            </pre>
          </>
        )}
      </div>

      <div className="panel mb-24">
        <p className="eyebrow eyebrow-ash">3. GET /api/accounts/{userId ?? '{id}'}/is-admin</p>
        {adminCheckError ? (
          <Alert tone="error">
            HTTP {adminCheckError.status} — {adminCheckError.message}
          </Alert>
        ) : !adminCheck ? (
          <p className="tiny dim">불러오는 중…</p>
        ) : (
          <Row label="admin" value={String(adminCheck.admin)} ok={adminCheck.admin} />
        )}
      </div>

      <div className="panel mb-24">
        <p className="eyebrow eyebrow-ash">4. 앱이 내린 판정</p>
        <Row label="profileStatus" value={profileStatus} ok={profileStatus === 'ready'} />
        <Row label="role" value={role} ok={role === ROLE.ADMIN} />
        <div className="price-line" style={{ borderBottom: 0 }}>
          <span>isAdmin</span>
          <Badge tone={isAdmin ? 'confirmed' : 'cancelled'}>{String(isAdmin)}</Badge>
        </div>
      </div>

      <div className="panel mb-24" style={{ borderColor: 'var(--hair-blood)' }}>
        <p className="eyebrow">role 이 USER 로 나온다면</p>
        <p className="tiny muted mb-16">
          PATCH /api/accounts/{'{userId}'}/role 로 본인 계정을 스스로 ADMIN으로 올릴 수 있습니다. 지금은
          ADMIN이 하나도 없는 상태를 벗어나기 위한 임시 조치라, 로그인만 했으면 아무 계정이나 바꿀 수
          있게 열려 있습니다 — 확인용으로만 쓰고, 실서비스 전에는 반드시 서버에서 이 호출 권한을
          제한해야 합니다.
        </p>
        {applied ? (
          <Alert tone="success">권한이 ADMIN으로 바뀌었습니다. 위 판정 결과가 갱신됐는지 확인해 보십시오.</Alert>
        ) : (
          <>
            <Alert tone="error">{applyError}</Alert>
            <button type="button" className="btn btn-primary" onClick={applyForAdmin} disabled={applying || !userId}>
              {applying ? '신청 중…' : '이 계정을 관리자로 신청'}
            </button>
          </>
        )}
      </div>

      <div className="panel" style={{ borderColor: 'var(--hair-blood)' }}>
        <p className="eyebrow">직접 DB에서 올리고 싶다면</p>
        <p className="tiny muted mb-16">
          users.role 컬럼에는 <strong className="mono">소문자</strong>로 저장됩니다 (RoleConverter가 읽을 때
          대문자로 바꿉니다).
        </p>
        <pre
          className="mono"
          style={{
            background: 'var(--void)',
            border: '1px solid var(--hair)',
            borderRadius: 4,
            padding: 14,
            overflowX: 'auto',
            margin: 0,
            color: 'var(--ash)',
          }}
        >
{`USE accommodation_db;

-- 지금 값 확인
SELECT id, email, role FROM users WHERE email = '${payload?.email || 'you@example.com'}';

-- 관리자로 올리기
UPDATE users SET role = 'admin' WHERE email = '${payload?.email || 'you@example.com'}';`}
        </pre>
        <p className="tiny dim mt-16">바꾼 뒤에는 로그아웃하고 다시 로그인해야 반영됩니다.</p>
      </div>

      <div className="row gap-8 mt-24">
        <Link to="/" className="btn btn-outline">
          홈으로
        </Link>
        {isAdmin && (
          <Link to="/admin" className="btn btn-primary">
            관리자 구역으로
          </Link>
        )}
      </div>
    </div>
  );
}
