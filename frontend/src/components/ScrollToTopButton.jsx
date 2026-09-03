import { useEffect, useState } from 'react';

const SHOW_AFTER_PX = 400;

/** 일정 이상 스크롤하면 나타나는 맨 위로 가기 버튼 (우측 하단 고정) */
export default function ScrollToTopButton() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const onScroll = () => setVisible(window.scrollY > SHOW_AFTER_PX);
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  if (!visible) return null;

  return (
    <button
      type="button"
      className="scroll-to-top"
      onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
      aria-label="맨 위로"
      title="맨 위로"
    >
      ↑
    </button>
  );
}
