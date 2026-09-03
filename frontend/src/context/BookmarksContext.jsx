import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { addBookmark, removeBookmark } from '../api/bookmarks';
import { getMypage } from '../api/mypage';
import { useAuth } from './AuthContext';

/* ------------------------------------------------------------------
 * 북마크 상태를 앱 전역에 한 벌만 두는 컨텍스트.
 *
 * 서버에 "내 북마크 목록"만 따로 주는 API가 없어서(POST /api/mypage 응답에
 * 같이 들어옵니다) 로그인 시 한 번 마이페이지를 읽어 id 집합을 만들어두고,
 * 이후 토글은 낙관적으로 반영한 뒤 실패하면 되돌립니다.
 *
 * 관리자(ADMIN)도 일반 계정과 동일하게 북마크를 쓸 수 있습니다.
 * ------------------------------------------------------------------ */

const BookmarksContext = createContext(null);

export function BookmarksProvider({ children }) {
  const { isAuthenticated, roleResolved } = useAuth();
  const [ids, setIds] = useState(() => new Set());
  const [pending, setPending] = useState(() => new Set());

  const reload = useCallback(async () => {
    if (!isAuthenticated) {
      setIds(new Set());
      return;
    }
    try {
      const data = await getMypage();
      const list = data?.bookmarks || [];
      setIds(new Set(list.map((b) => Number(b.roomId ?? b.id)).filter(Number.isFinite)));
    } catch {
      /* 북마크는 부가 기능이라 실패해도 화면을 막지 않습니다. */
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (!roleResolved) return;
    reload();
  }, [reload, roleResolved]);

  const toggle = useCallback(
    async (roomId) => {
      const id = Number(roomId);
      if (!isAuthenticated || pending.has(id)) return;

      const wasBookmarked = ids.has(id);
      setPending((p) => new Set(p).add(id));
      setIds((prev) => {
        const next = new Set(prev);
        if (wasBookmarked) next.delete(id);
        else next.add(id);
        return next;
      });

      try {
        if (wasBookmarked) await removeBookmark(id);
        else await addBookmark(id);
      } catch {
        // 실패하면 원래대로 되돌립니다.
        setIds((prev) => {
          const next = new Set(prev);
          if (wasBookmarked) next.add(id);
          else next.delete(id);
          return next;
        });
      } finally {
        setPending((p) => {
          const next = new Set(p);
          next.delete(id);
          return next;
        });
      }
    },
    [ids, pending, isAuthenticated]
  );

  const value = useMemo(
    () => ({
      ids,
      has: (roomId) => ids.has(Number(roomId)),
      isPending: (roomId) => pending.has(Number(roomId)),
      toggle,
      reload,
    }),
    [ids, pending, toggle, reload]
  );

  return <BookmarksContext.Provider value={value}>{children}</BookmarksContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useBookmarks() {
  const ctx = useContext(BookmarksContext);
  if (!ctx) throw new Error('useBookmarks()는 <BookmarksProvider> 안에서만 쓸 수 있습니다.');
  return ctx;
}
