import { http } from './client';

/** POST /api/bookmarks - 북마크 추가 (201) */
export function addBookmark(roomId) {
  return http.post('/api/bookmarks', { roomId });
}

/** DELETE /api/bookmarks/{roomId} - 북마크 해제 (204) */
export function removeBookmark(roomId) {
  return http.delete(`/api/bookmarks/${roomId}`);
}
