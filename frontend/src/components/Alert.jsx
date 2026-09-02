/** 에러/성공/안내 메시지. children이 비어 있으면 아무것도 그리지 않습니다. */
export default function Alert({ tone = 'error', children }) {
  if (!children) return null;
  const mark = tone === 'success' ? '✦' : tone === 'info' ? '※' : '⚠';
  return (
    <div className={`alert alert-${tone}`} role={tone === 'error' ? 'alert' : 'status'}>
      <span aria-hidden="true">{mark}</span>
      <span>{children}</span>
    </div>
  );
}
