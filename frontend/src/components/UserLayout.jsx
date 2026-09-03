import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';

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
          <div>
            <p className="serif" style={{ letterSpacing: '0.24em', fontSize: 15 }}>
              누비 묘원
            </p>
            <p className="tiny dim mt-8">함부로 깨우지 말 것 · 땅은 아직 기억하고 있다</p>
          </div>
          <p className="mono dim">© {new Date().getFullYear()} NUBI</p>
        </div>
      </footer>
    </>
  );
}
