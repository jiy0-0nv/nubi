import { useRef, useState } from 'react';

const TRACK_SRC = '/audio/bgm.mp3';

/**
 * 좌측 하단 고정 배경음악 재생 바.
 * 브라우저 자동재생 정책 때문에 페이지 진입 시엔 항상 멈춰 있고,
 * 사용자가 재생 버튼을 눌러야 소리가 납니다.
 */
export default function MusicBar() {
  const audioRef = useRef(null);
  const [playing, setPlaying] = useState(false);

  const toggle = () => {
    const audio = audioRef.current;
    if (!audio) return;
    if (playing) {
      audio.pause();
    } else {
      audio.play().catch(() => {});
    }
    setPlaying((p) => !p);
  };

  return (
    <div className={`music-bar${playing ? ' playing' : ''}`}>
      <audio ref={audioRef} src={TRACK_SRC} loop preload="none" />
      <button
        type="button"
        className="music-bar-toggle"
        onClick={toggle}
        aria-label={playing ? '배경음악 일시정지' : '배경음악 재생'}
      >
        {playing ? '❚❚' : '▶'}
      </button>
      {/* <span className="music-bar-label">묘지의 소리</span> */}
      <span className="music-bar-bars" aria-hidden="true">
        <i />
        <i />
        <i />
      </span>
    </div>
  );
}
