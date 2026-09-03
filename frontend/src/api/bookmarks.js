import { http } from './client';

/** POST /api/bookmarks - 북마크 추가 (201) */
export function addBookmark(roomId) {
  return http.post('/api/bookmarks', { roomId });
}

/** DELETE /api/bookmarks/{roomId} - 북마크 해제 (204) */
export function removeBookmark(roomId) {
  return http.delete(`/api/bookmarks/${roomId}`);
}

/** GET /api/bookmarks/{roomId} - 이 방을 내가 북마크했는지 단건 확인 -> { bookmarked: boolean } */
export function getBookmarkStatus(roomId) {
  return http.get(`/api/bookmarks/${roomId}`);
}
