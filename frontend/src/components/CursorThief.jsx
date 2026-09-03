import { useEffect, useRef } from 'react';

/* ==================================================================
 * CursorThief — 마우스가 일정 시간 멈춰 있으면 유령이 스르륵 나타나
 *               커서를 들고 도망다니는 컴포넌트.
 *
 * 이 파일 하나만 있으면 됩니다. (React 외 의존성 없음, CSS 파일 불필요)
 *
 * ── 쓰는 법 ──────────────────────────────────────────────────────
 *   import CursorThief from './CursorThief';
 *
 *   function App() {
 *     return (
 *       <>
 *         <내앱 />
 *         <CursorThief />
 *       </>
 *     );
 *   }
 *
 *   <CursorThief idleDelay={6000} />   // 6초 뒤에 나타나게
 *   <CursorThief enabled={false} />    // 잠시 끄기
 *
 * ── 원리 ────────────────────────────────────────────────────────
 *   브라우저 JS는 운영체제의 진짜 마우스 포인터를 옮길 수 없습니다.
 *   그래서 이렇게 속입니다:
 *     1) 유휴 상태가 되면 진짜 커서를 숨기고(cursor: none)
 *        마지막 좌표에 똑같이 생긴 가짜 포인터를 그립니다. (이음새가 안 보임)
 *     2) 유령이 그 옆에서 서서히 맺힙니다. (opacity 0 → 1)
 *     3) 포인터를 손에 쥐고 화면을 느릿하게 떠돕니다.
 *     4) 사용자가 마우스를 움직이거나 키를 누르면 놓아주고 흩어지며,
 *        진짜 커서가 즉시 돌아옵니다.
 *
 * ── 움직임 ──────────────────────────────────────────────────────
 *   목표 지점으로 순간이동하듯 끌려가면 통통 튀어 보이기 때문에,
 *   "속도"를 따로 두고 가속·감속시키는 방식(steering)을 씁니다.
 *   방향 전환도 즉시 뒤집지 않고 facing 값을 -1↔1로 서서히 보간해서
 *   천천히 몸을 돌리는 것처럼 보이게 합니다.
 *
 * ── 안전장치 ────────────────────────────────────────────────────
 *   - pointer-events: none 이라 클릭/입력을 절대 방해하지 않습니다.
 *   - 매 프레임 React state가 아니라 DOM transform을 직접 씁니다. (리렌더 0회)
 *   - 터치 기기, 모션 최소화 설정(prefers-reduced-motion),
 *     탭이 백그라운드일 때는 아예 나오지 않습니다.
 *   - localStorage 'cursor-thief' 가 'off' 면 비활성화됩니다.
 * ================================================================== */

const CONFIG = {
  /** 이 시간(ms) 동안 아무 입력이 없으면 나타납니다. */
  idleDelay: 10000,
  /** 맺히는 데 걸리는 시간 (아래 CSS의 transition과 같아야 합니다) */
  appearMs: 900,

  /* --- 움직임 --- */
  /** 최고 속도 (16.67ms 당 px). 낮을수록 느긋합니다. */
  maxSpeed: 1.15,
  /** 가속/감속의 부드러움 (낮을수록 관성이 큽니다) */
  steer: 0.012,
  /** 목표에 이만큼 가까워지면 다음 목적지를 고릅니다. */
  arriveRadius: 90,
  /** 목적지에 못 닿아도 이 시간이 지나면 새로 고릅니다. (ms) */
  wanderTimeout: [5200, 9000],
  /** 한 번에 최소 이만큼(px)은 떨어진 곳으로 향합니다. */
  minTravel: 320,
  /** 화면 가장자리에서 이만큼 안쪽으로만 떠돕니다. */
  edgeMargin: 110,
  /** 좌우 반전 보간 속도 (낮을수록 천천히 돌아섭니다) */
  facingEase: 0.035,
  /** 이 속도 이상으로 움직일 때만 몸을 돌립니다. (미세한 떨림 무시) */
  turnThreshold: 0.22,

  /** 렌더링 크기 */
  size: { w: 150, h: 165 },
  /** 캐릭터 중심 기준, 포인터를 쥔 손의 위치 (반전 시 x가 뒤집힙니다) */
  hand: { x: 57, y: 14 },
  /** 맺힐 때 커서에서 이만큼 떨어진 곳에 나타납니다. */
  spawnOffset: { x: 76, y: -42 },
  /** 다른 UI 위에 뜨도록 */
  zIndex: 9998,
};

const ACTIVITY_EVENTS = ['mousemove', 'mousedown', 'wheel', 'keydown', 'touchstart', 'pointerdown'];
const STORAGE_KEY = 'cursor-thief';
const STYLE_ID = 'cursor-thief-style';

/* ------------------------------------------------------------------
 * 스타일. 별도 CSS 파일 없이 이 파일이 직접 <head>에 한 번만 주입합니다.
 * ------------------------------------------------------------------ */
const CSS = `
/* 유령이 활동 중일 때는 진짜 커서를 감춥니다.
   가짜 포인터가 정확히 같은 자리에 그려지므로 이음새가 보이지 않습니다. */
html.ct-hunting, html.ct-hunting * { cursor: none !important; }

.ct-root {
  position: fixed;
  inset: 0;
  z-index: ${CONFIG.zIndex};
  pointer-events: none;          /* 클릭/입력을 절대 가로채지 않습니다 */
}

.ct-ghost, .ct-cursor {
  position: absolute;
  top: 0;
  left: 0;
  will-change: transform, opacity;
}

/* 등장/퇴장은 투명도로만 */
.ct-ghost {
  opacity: 0;
  filter: drop-shadow(0 14px 26px rgba(0, 0, 0, 0.42));
  transition: opacity ${CONFIG.appearMs}ms ease;
}
.ct-root.is-visible .ct-ghost { opacity: 1; }

.ct-cursor {
  opacity: 0;
  margin: -2px 0 0 -2px;          /* 포인터 끝(hotspot)이 좌표에 정확히 오도록 */
  transform-origin: 3px 3px;
  filter: drop-shadow(0 3px 5px rgba(0, 0, 0, 0.5));
  transition: opacity 0.25s ease;
}
/* 가짜 포인터는 진짜 커서를 숨기는 순간 바로 보여야 합니다 (페이드인 없음) */
html.ct-hunting .ct-cursor { opacity: 1; }

.ct-svg { width: 100%; height: 100%; overflow: visible; }

/* transform-box: fill-box 를 줘야 transform-origin이 그 도형 자신의
   바운딩 박스 기준이 됩니다. (기본값은 SVG 전체 기준) */
.ct-body {
  transform-box: fill-box;
  transform-origin: top center;
  animation: ct-sway 4.6s ease-in-out infinite alternate;
}
@keyframes ct-sway {
  from { transform: skewX(-1.8deg) scaleY(0.99); }
  to   { transform: skewX(1.8deg) scaleY(1.015); }
}

.ct-arm {
  transform-box: fill-box;
  transform-origin: 50% 30%;
  animation: ct-arm 3.4s ease-in-out infinite alternate;
}
.ct-arm-r { animation-delay: -1.7s; }
@keyframes ct-arm {
  from { transform: rotate(-5deg); }
  to   { transform: rotate(5deg); }
}
/* 커서를 쥐면 신이 나서 빨라집니다 */
.ct-root.is-carrying .ct-arm-r { animation-duration: 1.1s; }
.ct-root.is-carrying .ct-body  { animation-duration: 2.6s; }

.ct-eye {
  transform-box: fill-box;
  transform-origin: center;
  animation: ct-blink 5.4s infinite;
}
.ct-eye-r { animation-delay: 0.06s; }
@keyframes ct-blink {
  0%, 93%, 100% { transform: scaleY(1); }
  95%, 97%      { transform: scaleY(0.1); }
}

/* 훔치고 나면 입이 커집니다 */
.ct-mouth {
  transform-box: fill-box;
  transform-origin: center;
  transition: transform 0.3s cubic-bezier(0.34, 1.4, 0.64, 1);
}
.ct-root.is-carrying .ct-mouth { transform: scale(1.3); }

@media (prefers-reduced-motion: reduce) { .ct-root { display: none; } }
`;

function injectStyles() {
  if (typeof document === 'undefined' || document.getElementById(STYLE_ID)) return;
  const el = document.createElement('style');
  el.id = STYLE_ID;
  el.textContent = CSS;
  document.head.appendChild(el);
}

/* ------------------------------------------------------------------ */

const rand = (min, max) => min + Math.random() * (max - min);
const clamp = (v, lo, hi) => Math.min(Math.max(v, lo), hi);
/** 프레임 간격에 상관없이 같은 감속감을 내기 위한 보정 */
const easeFactor = (base, dt) => 1 - Math.pow(1 - base, dt / 16.67);

export default function CursorThief({ idleDelay = CONFIG.idleDelay, enabled = true }) {
  const rootRef = useRef(null);
  const ghostRef = useRef(null);
  const cursorRef = useRef(null);

  // 매 프레임 바뀌는 값들은 전부 ref에 둡니다. (state로 두면 초당 60번 리렌더)
  const S = useRef({
    phase: 'off', // 'off' | 'appear' | 'carry' | 'vanish'
    pointer: { x: 0, y: 0 },
    stolen: { x: 0, y: 0 },
    ghost: { x: 0, y: 0 },
    vel: { x: 0, y: 0 },
    target: { x: 0, y: 0 },
    fake: { x: 0, y: 0 },
    /** 바라보는 방향의 목표값(-1 또는 1)과, 실제로 보간 중인 값 */
    dir: 1,
    facing: 1,
    /** 실제 마우스 위치를 한 번이라도 관측했는지 */
    seen: false,
    time: 0,
    lastFrame: 0,
    repickAt: 0,
    grabAt: 0,
    raf: 0,
    idleTimer: 0,
    vanishTimer: 0,
  }).current;

  useEffect(() => {
    if (!enabled) return undefined;

    // 마우스가 없는 기기, 모션 최소화 설정, 사용자가 꺼둔 경우는 실행하지 않습니다.
    const coarse = window.matchMedia('(pointer: coarse)').matches;
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    let turnedOff = false;
    try {
      turnedOff = localStorage.getItem(STORAGE_KEY) === 'off';
    } catch {
      /* storage 접근이 막혀도 그냥 진행 */
    }
    if (coarse || reduced || turnedOff) return undefined;

    injectStyles();
    const root = () => rootRef.current;

    /* ---------------- 렌더 ---------------- */
    const paint = () => {
      const ghost = ghostRef.current;
      const cursor = cursorRef.current;
      if (!ghost || !cursor) return;

      const float = Math.sin(S.time / 1450) * 5; // 아주 느린 부유
      const tilt = Math.sin(S.time / 1900) * 2.4;

      ghost.style.transform =
        `translate3d(${S.ghost.x}px, ${S.ghost.y + float}px, 0) translate(-50%, -50%)` +
        ` rotate(${tilt}deg) scaleX(${S.facing})`;

      const swing = S.phase === 'carry' ? Math.sin(S.time / 700) * 10 - 4 : 0;
      cursor.style.transform = `translate3d(${S.fake.x}px, ${S.fake.y}px, 0) rotate(${swing}deg)`;
    };

    /**
     * 목표를 향해 "가속"합니다. 목표가 바뀌어도 속도가 이어지므로
     * 방향이 꺾일 때 툭 끊기지 않고 곡선으로 휩니다.
     */
    const steerToward = (target, dt) => {
      const dx = target.x - S.ghost.x;
      const dy = target.y - S.ghost.y;
      const dist = Math.hypot(dx, dy) || 1;

      // 목표에 가까워질수록 천천히 (도착 감속)
      const speed = CONFIG.maxSpeed * clamp(dist / CONFIG.arriveRadius, 0.18, 1);
      const k = easeFactor(CONFIG.steer, dt);
      S.vel.x += ((dx / dist) * speed - S.vel.x) * k;
      S.vel.y += ((dy / dist) * speed - S.vel.y) * k;

      const step = dt / 16.67;
      S.ghost.x += S.vel.x * step;
      S.ghost.y += S.vel.y * step;

      // 충분히 빠르게 옆으로 움직일 때만 돌아설 방향을 바꾸고,
      if (Math.abs(S.vel.x) > CONFIG.turnThreshold) S.dir = S.vel.x > 0 ? 1 : -1;
      // 그 방향으로 서서히 몸을 돌립니다. (-1 ↔ 1 보간)
      S.facing += (S.dir - S.facing) * easeFactor(CONFIG.facingEase, dt);

      return dist;
    };

    /** 지금 위치에서 충분히 먼 곳을 골라 멀찍이 달아나는 느낌을 냅니다. */
    const pickTarget = (now) => {
      const m = CONFIG.edgeMargin;
      const maxX = Math.max(m + 1, window.innerWidth - m);
      const maxY = Math.max(m + 1, window.innerHeight - m);
      let best = { x: rand(m, maxX), y: rand(m, maxY) };
      for (let i = 0; i < 8; i += 1) {
        const cand = { x: rand(m, maxX), y: rand(m, maxY) };
        best = cand;
        if (Math.hypot(cand.x - S.ghost.x, cand.y - S.ghost.y) > CONFIG.minTravel) break;
      }
      S.target = best;
      S.repickAt = now + rand(CONFIG.wanderTimeout[0], CONFIG.wanderTimeout[1]);
    };

    /* ---------------- 루프 ---------------- */
    const loop = (now) => {
      const dt = Math.min(50, now - (S.lastFrame || now));
      S.lastFrame = now;
      S.time += dt;

      if (S.phase === 'appear') {
        // 맺히는 동안에는 제자리에 떠 있고, 포인터는 아직 원래 자리에 있습니다.
        S.fake = { ...S.stolen };
        if (now >= S.grabAt) {
          S.phase = 'carry';
          root()?.classList.add('is-carrying');
          pickTarget(now);
        }
      } else if (S.phase === 'carry') {
        const dist = steerToward(S.target, dt);
        if (dist < CONFIG.arriveRadius * 0.5 || now >= S.repickAt) pickTarget(now);
        S.fake = { x: S.ghost.x + CONFIG.hand.x * S.facing, y: S.ghost.y + CONFIG.hand.y };
      } else if (S.phase === 'vanish') {
        steerToward(S.target, dt);
      }

      paint();
      S.raf = requestAnimationFrame(loop);
    };

    const start = () => {
      if (S.phase !== 'off') return;
      // 진짜 커서 위치를 아직 모르면 나서지 않습니다. 엉뚱한 자리에 가짜 포인터를
      // 그려두고 진짜를 숨기면 커서가 순간이동한 것처럼 보이니까요.
      if (!S.seen) return;

      S.stolen = { ...S.pointer };
      S.fake = { ...S.pointer };
      S.vel = { x: 0, y: 0 };

      // 화면 밖으로 삐져나가지 않게 등장 위치를 안쪽으로 당깁니다.
      const m = CONFIG.edgeMargin;
      const side = S.pointer.x > window.innerWidth - m * 2 ? -1 : 1;
      S.ghost = {
        x: clamp(S.pointer.x + CONFIG.spawnOffset.x * side, m, Math.max(m, window.innerWidth - m)),
        y: clamp(S.pointer.y + CONFIG.spawnOffset.y, m, Math.max(m, window.innerHeight - m)),
      };
      S.target = { ...S.ghost };
      // 커서 쪽(= 손이 향할 쪽)을 보도록 처음부터 그 방향으로 세워둡니다.
      S.dir = S.ghost.x > S.stolen.x ? -1 : 1;
      S.facing = S.dir;
      S.phase = 'appear';
      S.grabAt = performance.now() + CONFIG.appearMs;

      document.documentElement.classList.add('ct-hunting');
      root()?.classList.remove('is-carrying');
      paint();
      // 다음 프레임에 클래스를 올려야 opacity 트랜지션이 실제로 재생됩니다.
      requestAnimationFrame(() => root()?.classList.add('is-visible'));

      S.lastFrame = 0;
      if (S.raf) cancelAnimationFrame(S.raf);
      S.raf = requestAnimationFrame(loop);
    };

    const stop = () => {
      S.phase = 'off';
      if (S.raf) cancelAnimationFrame(S.raf);
      S.raf = 0;
    };

    const scatter = () => {
      // 진짜 커서는 즉시 돌려주고, 유령만 흩어지게 둡니다.
      document.documentElement.classList.remove('ct-hunting');
      if (S.phase === 'off' || S.phase === 'vanish') return;

      S.phase = 'vanish';
      root()?.classList.remove('is-carrying', 'is-visible');
      S.target = { x: S.ghost.x + rand(-90, 90), y: S.ghost.y - 170 }; // 위로 스르륵
      window.clearTimeout(S.vanishTimer);
      S.vanishTimer = window.setTimeout(stop, CONFIG.appearMs);
    };

    /* ---------------- 유휴 감지 ---------------- */
    const resetIdle = () => {
      window.clearTimeout(S.idleTimer);
      S.idleTimer = window.setTimeout(start, idleDelay);
    };

    const onActivity = (e) => {
      if (e.type === 'mousemove') {
        S.pointer = { x: e.clientX, y: e.clientY };
        S.seen = true;
        // 커서를 숨긴 동안에도 mousemove가 계속 오므로,
        // "정말 사람이 움직였을 때"만 반응하도록 아주 작은 흔들림은 무시합니다.
        if (S.phase !== 'off' && Math.hypot(e.clientX - S.stolen.x, e.clientY - S.stolen.y) < 4) return;
      }
      if (S.phase !== 'off') scatter();
      resetIdle();
    };

    const onVisibility = () => {
      if (document.hidden) {
        window.clearTimeout(S.idleTimer);
        scatter();
      } else {
        resetIdle();
      }
    };

    ACTIVITY_EVENTS.forEach((type) => window.addEventListener(type, onActivity, { passive: true }));
    window.addEventListener('blur', onVisibility);
    document.addEventListener('visibilitychange', onVisibility);
    resetIdle();

    return () => {
      ACTIVITY_EVENTS.forEach((type) => window.removeEventListener(type, onActivity));
      window.removeEventListener('blur', onVisibility);
      document.removeEventListener('visibilitychange', onVisibility);
      window.clearTimeout(S.idleTimer);
      window.clearTimeout(S.vanishTimer);
      if (S.raf) cancelAnimationFrame(S.raf);
      S.raf = 0;
      document.documentElement.classList.remove('ct-hunting');
      root()?.classList.remove('is-visible', 'is-carrying');
      S.phase = 'off';
    };
  }, [enabled, idleDelay, S]);

  if (!enabled) return null;

  return (
    <div className="ct-root" ref={rootRef} aria-hidden="true">
      <div className="ct-ghost" ref={ghostRef} style={{ width: CONFIG.size.w, height: CONFIG.size.h }}>
        <GhostArt />
      </div>

      {/* 진짜 커서를 대신하는 가짜 포인터 */}
      <div className="ct-cursor" ref={cursorRef}>
        <svg width="20" height="26" viewBox="0 0 20 26" fill="none">
          <path
            d="M2 1.6 17.4 12.6 10.4 13.4 14 21.6 11 23 7.4 14.8 2.6 19.4Z"
            fill="#ffffff"
            stroke="#2b2b33"
            strokeWidth="1.6"
            strokeLinejoin="round"
          />
        </svg>
      </div>
    </div>
  );
}

/* ------------------------------------------------------------------
 * 캐릭터 — 직접 그린 SVG입니다. 색만 바꾸면 다른 느낌으로 쓸 수 있습니다.
 * ------------------------------------------------------------------ */
function GhostArt() {
  return (
    <svg viewBox="0 0 200 220" className="ct-svg">
      <defs>
        <linearGradient id="ctSheet" x1="0.3" y1="0" x2="0.55" y2="1">
          <stop offset="0%" stopColor="#ffffff" />
          <stop offset="64%" stopColor="#f7fafd" />
          <stop offset="100%" stopColor="#dfe9f3" />
        </linearGradient>
      </defs>

      {/* 팔 (몸 뒤에 두어 둥근 혹처럼 보이게) */}
      <path
        className="ct-arm ct-arm-l"
        d="M44 116c-15-3-27 4-29 15-1 8 5 13 12 11 6-2 8-8 5-12"
        fill="none"
        stroke="#f2f7fc"
        strokeWidth="19"
        strokeLinecap="round"
      />
      <path
        className="ct-arm ct-arm-r"
        d="M156 116c15-3 27 4 29 15 1 8-5 13-12 11-6-2-8-8-5-12"
        fill="none"
        stroke="#f2f7fc"
        strokeWidth="19"
        strokeLinecap="round"
      />

      {/* 몸통 — 아래가 물결처럼 나풀거립니다 */}
      <path
        className="ct-body"
        d="M30 124C30 68 61 24 100 24s70 44 70 100c0 22 3 40-4 54-6 12-19 11-25-1-6-13-17-13-23 0-6 13-19 13-25 0-6-13-17-13-23 0-6 12-19 13-25 1-7-14-15-32-15-54Z"
        fill="url(#ctSheet)"
      />

      {/* 볼 */}
      <ellipse cx="59" cy="110" rx="13" ry="8" fill="#ffb3c7" opacity="0.85" />
      <ellipse cx="141" cy="110" rx="13" ry="8" fill="#ffb3c7" opacity="0.85" />

      {/* 눈 */}
      <ellipse className="ct-eye ct-eye-l" cx="76" cy="88" rx="11" ry="16" fill="#4b3226" />
      <ellipse className="ct-eye ct-eye-r" cx="124" cy="88" rx="11" ry="16" fill="#4b3226" />

      {/* 입 */}
      <ellipse className="ct-mouth" cx="100" cy="120" rx="9" ry="12" fill="#4b3226" />
    </svg>
  );
}
