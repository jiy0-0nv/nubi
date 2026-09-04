import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import CursorThief from './CursorThief.jsx';
import ScrollToTopButton from './ScrollToTopButton';
import MusicBar from './MusicBar';

/** 사용자 영역 공통 셸 (헤더 + 본문 + 푸터) */
export default function UserLayout() {
  return (
    <>
      <Navbar />
      <main>
        <Outlet />
      </main>
      <footer style={{ borderTop: '1px solid var(--hair)', padding: '36px 0', background: 'var(--void)' }}>
        <div className="container row-between">
          {/* <div>
            <p className="serif" style={{ letterSpacing: '0.24em', fontSize: 15 }}>
              누비 묘원
            </p>
            <p className="tiny dim mt-8">함부로 깨우지 말 것 · 땅은 아직 기억하고 있다</p>
          </div> */}
          <p className="mono dim">© {new Date().getFullYear()} NUBI</p>
        </div>
      </footer>

      {/*
        가만히 두면 스르륵 나타나 커서를 들고 도망다니는 것.
        위치는 상관없습니다 — 내부적으로 position: fixed 이고 pointer-events: none 이라
        레이아웃에 영향을 주지 않습니다.

        관리자 영역(AdminLayout)에는 일부러 넣지 않았습니다 — 예약을 관리하는 중에
        커서가 사라지면 곤란하니까요. 전역으로 쓰고 싶으면 App.jsx로 옮기면 됩니다.
      */}
      <CursorThief idleDelay={10000} />
      <ScrollToTopButton />
      <MusicBar />
    </>
  );
}
