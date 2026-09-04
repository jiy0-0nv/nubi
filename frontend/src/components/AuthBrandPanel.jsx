/** 인증 화면 좌측의 달·피라미드 실루엣 (순수 CSS 장식) */
function BrandDecor() {
  return (
    <div className="auth-split-decor" aria-hidden="true">
      {/* 달이 오른쪽 끝에 잘리지 않도록 이 값만큼 전체 장식을 왼쪽으로 밀었습니다.
          그믐달 방향은 메인 페이지(HomePage)의 달과 맞춰서 초승달 쪽이 왼쪽을 향하게 뒀습니다. */}
      <div style={{ position: 'absolute', top: 90, right: 54, width: 130, height: 130, borderRadius: 999, background: 'var(--yellow)', opacity: 0.9 }} />
      <div style={{ position: 'absolute', top: 90, right: 20, width: 130, height: 130, borderRadius: 999, background: 'var(--void)' }} />
      <div
        style={{
          position: 'absolute', bottom: -1, right: 100,
          width: 0, height: 0,
          borderLeft: '84px solid transparent', borderRight: '84px solid transparent', borderBottom: '108px solid rgba(94,224,238,.14)',
        }}
      />
      <div
        style={{
          position: 'absolute', bottom: -1, right: 232,
          width: 0, height: 0,
          borderLeft: '52px solid transparent', borderRight: '52px solid transparent', borderBottom: '70px solid rgba(94,224,238,.08)',
        }}
      />
    </div>
  );
}

/** 로그인/회원가입 등 분할형 인증 화면의 좌측 브랜드 패널. 두 화면에서 동일하게 씁니다. */
export default function AuthBrandPanel() {
  return (
    <div className="auth-split-brand">
      <BrandDecor />

      <div className="auth-split-brand-bottom">
        <p className="mono" style={{ fontSize: 11, letterSpacing: '0.2em', color: 'var(--green)', marginBottom: 16 }}>
          WORLD&apos;S OLDEST GUESTHOUSES
        </p>
        <h2 style={{ fontSize: 32, lineHeight: 1.28, marginBottom: 14 }}>
          4,600년 된 방이
          <br />
          당신을 기다립니다
        </h2>
        <p className="muted" style={{ fontSize: 14, lineHeight: 1.75 }}>
          등록 묘소 1,204곳 · 41개국 · 최고 연식 6,600만 년
        </p>
      </div>
    </div>
  );
}
