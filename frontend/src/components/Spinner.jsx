export default function Spinner({ label = '불러오는 중' }) {
  return (
    <div className="spinner-wrap">
      <div className="spinner" />
      <span className="spinner-text">{label}</span>
    </div>
  );
}
