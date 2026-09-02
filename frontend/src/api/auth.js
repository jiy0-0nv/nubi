import { http } from './client';

/**
 * POST /api/accounts/login
 * 응답: text/plain 순수 JWT 문자열 (JSON 아님)
 */
export function login(email, password) {
  return http.post('/api/accounts/login', { email, password }, { auth: false });
}

/**
 * POST /api/accounts/signup
 * 응답: 생성된 userId(숫자)
 */
export function signup({ email, password, name, phone }) {
  return http.post('/api/accounts/signup', { email, password, name, phone }, { auth: false });
}

/**
 * GET /api/accounts/{userId}  (본인 계정만 조회 가능, 남의 id면 403)
 * 응답: AccountResponseDTO { id, email, name, phone, role, ... }
 *  - role 은 UsersEntity.Role 열거형: "USER" | "ADMIN"
 *  - 프론트의 관리자/사용자 분기는 전적으로 이 값을 기준으로 합니다.
 */
export function getAccount(userId) {
  return http.get(`/api/accounts/${userId}`);
}

/** POST /api/accounts/find-id -> 이메일 문자열 */
export function findId(name, phone) {
  return http.post('/api/accounts/find-id', { name, phone }, { auth: false });
}

/** POST /api/accounts/find-password -> 임시 비밀번호 메일 발송 (본문 없음) */
export function findPassword(email) {
  return http.post('/api/accounts/find-password', { email }, { auth: false });
}

/**
 * PATCH /api/accounts/change-password
 * 서버가 헤더가 아니라 body의 userToken으로 사용자를 식별하는 스펙이라
 * 현재 보관 중인 JWT를 그대로 body에 실어 보냅니다.
 */
export function changePassword(userToken, newPassword) {
  return http.patch('/api/accounts/change-password', { userToken, newPassword }, { auth: false });
}

/** DELETE /api/accounts/{userId} -> 회원 탈퇴 */
export function withdraw(userId) {
  return http.delete(`/api/accounts/${userId}`);
}
