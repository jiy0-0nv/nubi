import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { getToken, setToken as persistToken, UNAUTHORIZED_EVENT } from '../api/client';
import * as authApi from '../api/auth';

/* ------------------------------------------------------------------
 * 인증 + 권한(ROLE) 컨텍스트
 *
 * 백엔드 확인 결과 (컴파일된 클래스 기준):
 *   - JWT payload에는 sub(userId)와 email 만 들어있고 role은 없습니다.
 *   - AccountResponseDTO 에는 String role 필드가 있고,
 *     AccountResponseDTO.from() 이 user.getRole().name() 을 넣으므로
 *     값은 항상 대문자 "USER" / "ADMIN" 입니다.
 *   - DB users.role 컬럼에는 소문자('user'/'admin')로 저장되고
 *     RoleConverter가 읽을 때 대문자로 바꿔 enum으로 만듭니다.
 *
 * 따라서 흐름은:
 *   1) POST /api/accounts/login   -> JWT 문자열
 *   2) JWT의 sub 에서 userId 추출
 *   3) GET /api/accounts/{userId} -> { ..., role: "USER" | "ADMIN" }
 *   4) role === 'ADMIN' 이면 관리자
 *
 * profileStatus 로 "아직 못 읽음"과 "읽었는데 실패함"을 구분합니다.
 * (예전에는 실패를 조용히 삼켜서, 실패 시 관리자 화면이 로딩 스피너에서
 *  영영 멈추거나 이유 없이 USER로 강등되는 문제가 있었습니다.)
 * ------------------------------------------------------------------ */

export const ROLE = { USER: 'USER', ADMIN: 'ADMIN' };

function decodeJwtPayload(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join('')
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function userIdFromToken(token) {
  const payload = token ? decodeJwtPayload(token) : null;
  return payload?.sub ? Number(payload.sub) : null;
}

/** exp(초 단위)가 지났으면 서버를 부르기 전에 걸러냅니다. */
function isExpired(token) {
  const payload = token ? decodeJwtPayload(token) : null;
  if (!payload?.exp) return false;
  return payload.exp * 1000 <= Date.now();
}

/**
 * 서버가 role을 어떤 모양으로 주더라도 USER/ADMIN 중 하나로 정규화합니다.
 *  - "ADMIN" / "admin" / " Admin "  -> ADMIN
 *  - "ROLE_ADMIN"                   -> ADMIN  (Spring Security 관례 대비)
 *  - 그 외 / 없음                    -> USER   (권한은 확실할 때만 부여)
 */
export function normalizeRole(profile) {
  const raw = String(profile?.role ?? profile?.authority ?? '')
    .trim()
    .toUpperCase()
    .replace(/^ROLE_/, '');
  return raw === ROLE.ADMIN ? ROLE.ADMIN : ROLE.USER;
}

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(() => {
    const stored = getToken();
    if (stored && isExpired(stored)) {
      persistToken(null);
      return null;
    }
    return stored;
  });
  const [profile, setProfile] = useState(null);
  /** 'idle'(토큰 없음) | 'loading' | 'ready'(성공) | 'error'(실패) */
  const [profileStatus, setProfileStatus] = useState('loading');
  const [profileError, setProfileError] = useState(null);

  /** 내 계정 정보(= role 포함)를 서버에서 읽어옵니다. */
  const loadProfile = useCallback(async (activeToken) => {
    const useToken = activeToken !== undefined ? activeToken : getToken();
    const userId = userIdFromToken(useToken);

    if (!useToken || !userId) {
      setProfile(null);
      setProfileError(useToken && !userId ? { status: 0, message: '토큰에서 사용자 ID(sub)를 읽지 못했습니다.' } : null);
      setProfileStatus(useToken ? 'error' : 'idle');
      return null;
    }

    setProfileStatus('loading');
    try {
      const account = await authApi.getAccount(userId);
      setProfile(account);
      setProfileError(null);
      setProfileStatus('ready');
      return account;
    } catch (err) {
      // 401이면 client.js가 토큰을 지우고 이벤트를 쏴줍니다.
      setProfile(null);
      setProfileError({ status: err?.status ?? 0, message: err?.message || '계정 정보를 읽지 못했습니다.' });
      setProfileStatus('error');
      return null;
    }
  }, []);

  // 새로고침 후 복구
  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  // 어떤 API든 401을 받으면 즉시 로그아웃 상태로 동기화
  useEffect(() => {
    const onUnauthorized = () => {
      setTokenState(null);
      setProfile(null);
      setProfileError(null);
      setProfileStatus('idle');
    };
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized);
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized);
  }, []);

  /**
   * 로그인. role을 함께 돌려주므로 LoginPage가 곧바로 분기할 수 있습니다.
   * 계정 정보를 못 읽었으면 role은 null 이고, 이때는 분기 대신 안내를 띄웁니다.
   */
  const login = useCallback(
    async (email, password) => {
      const newToken = await authApi.login(email, password);
      if (!newToken || typeof newToken !== 'string') {
        throw new Error('로그인 응답이 올바르지 않습니다. (토큰 문자열이 아님)');
      }
      persistToken(newToken);
      setTokenState(newToken);
      const account = await loadProfile(newToken);
      return { token: newToken, profile: account, role: account ? normalizeRole(account) : null };
    },
    [loadProfile]
  );

  const logout = useCallback(() => {
    persistToken(null);
    setTokenState(null);
    setProfile(null);
    setProfileError(null);
    setProfileStatus('idle');
  }, []);

  const value = useMemo(() => {
    const role = normalizeRole(profile);
    const settled = profileStatus === 'ready' || profileStatus === 'error' || profileStatus === 'idle';
    return {
      token,
      profile,
      profileStatus,
      profileError,
      userId: userIdFromToken(token),
      role,
      isAuthenticated: Boolean(token),
      isAdmin: Boolean(token) && profileStatus === 'ready' && role === ROLE.ADMIN,
      /**
       * 권한 판정이 끝났는지. 성공이든 실패든 "한 번 시도가 끝났으면" true 입니다.
       * (실패를 영영 pending으로 두면 가드가 스피너에서 멈춰버립니다)
       */
      roleResolved: !token || settled,
      booting: profileStatus === 'loading',
      login,
      logout,
      reloadProfile: loadProfile,
    };
  }, [token, profile, profileStatus, profileError, login, logout, loadProfile]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth()는 <AuthProvider> 안에서만 쓸 수 있습니다.');
  return ctx;
}
