/* ------------------------------------------------------------------
 * 얇은 fetch 래퍼.
 *
 * 백엔드(Spring Boot)는 모든 컨트롤러를 /api/** 아래에 두고 있고,
 * JwtAuthenticationFilter가 Authorization: Bearer <token> 헤더를 읽습니다.
 * 인증 실패 시 401 + {"errorCode":"NEED_SIGNUP"} 형태의 JSON을 내려줍니다.
 *
 * 주의: 로그인/아이디찾기 같은 일부 엔드포인트는 JSON이 아니라
 *      text/plain(순수 문자열)을 응답하므로 Content-Type을 보고 파싱합니다.
 * ------------------------------------------------------------------ */

const TOKEN_KEY = 'nubi.token';

/** 401 발생 시 AuthContext가 즉시 로그아웃 상태로 동기화하도록 쏘는 이벤트 이름 */
export const UNAUTHORIZED_EVENT = 'nubi:unauthorized';

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setToken(token) {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  } catch {
    /* 사생활 보호 모드 등에서 storage 접근이 막힐 수 있어 무시 */
  }
}

export class ApiError extends Error {
  constructor(status, message, data) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

async function parseBody(response) {
  const contentType = response.headers.get('content-type') || '';
  const text = await response.text();
  if (!text) return null;
  if (contentType.includes('application/json')) {
    try {
      return JSON.parse(text);
    } catch {
      return text;
    }
  }
  return text;
}

function buildErrorMessage(status, data) {
  if (data && typeof data === 'object') {
    if (data.errorCode === 'NEED_SIGNUP') return '세션이 끊겼습니다. 다시 로그인해 주세요.';
    if (data.message) return data.message;
    if (data.error) return data.error;
  }
  if (typeof data === 'string' && data.trim()) return data.trim();

  switch (status) {
    case 400:
      return '입력값을 다시 확인해 주세요.';
    case 401:
      return '로그인이 필요합니다.';
    case 403:
      return '접근 권한이 없습니다.';
    case 404:
      return '요청한 정보를 찾을 수 없습니다.';
    case 409:
      return '이미 처리되었거나 다른 예약과 겹칩니다.';
    case 413:
      return '파일 용량이 너무 큽니다.';
    default:
      return status >= 500 ? '서버에 문제가 발생했습니다.' : '요청 처리 중 오류가 발생했습니다.';
  }
}

export async function request(path, { method = 'GET', body, auth = true, headers = {}, raw = false } = {}) {
  const finalHeaders = { ...headers };

  // raw = true 이면 body(FormData 등)를 그대로 전송. Content-Type은 브라우저가 붙입니다.
  if (body !== undefined && !raw) finalHeaders['Content-Type'] = 'application/json';

  if (auth) {
    const token = getToken();
    if (token) finalHeaders.Authorization = `Bearer ${token}`;
  }

  let response;
  try {
    response = await fetch(path, {
      method,
      headers: finalHeaders,
      body: body === undefined ? undefined : raw ? body : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, '서버에 연결하지 못했습니다. 백엔드가 실행 중인지 확인해 주세요.', null);
  }

  const data = await parseBody(response);

  if (!response.ok) {
    if (response.status === 401) {
      setToken(null);
      window.dispatchEvent(new Event(UNAUTHORIZED_EVENT));
    }
    throw new ApiError(response.status, buildErrorMessage(response.status, data), data);
  }

  return data;
}

export const http = {
  get: (path, opts) => request(path, { ...opts, method: 'GET' }),
  post: (path, body, opts) => request(path, { ...opts, method: 'POST', body }),
  patch: (path, body, opts) => request(path, { ...opts, method: 'PATCH', body }),
  put: (path, body, opts) => request(path, { ...opts, method: 'PUT', body }),
  delete: (path, opts) => request(path, { ...opts, method: 'DELETE' }),
  upload: (path, formData, opts) => request(path, { ...opts, method: 'POST', body: formData, raw: true }),
};

/** 객체를 쿼리스트링으로. undefined / '' / null 은 제외합니다. */
export function toQuery(params = {}) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return;
    search.set(key, String(value));
  });
  const qs = search.toString();
  return qs ? `?${qs}` : '';
}
