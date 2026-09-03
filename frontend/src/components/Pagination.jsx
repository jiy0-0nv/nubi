/** Spring Page 응답(totalPages)을 그대로 받아 이전/다음만 제공하는 단순 페이저 */
export default function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null;
  return (
    <div className="pagination">
      <button type="button" className="btn btn-outline btn-sm" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        ← 이전
      </button>
      <span className="pagination-info">
        {String(page + 1).padStart(2, '0')} / {String(totalPages).padStart(2, '0')}
      </span>
      <button
        type="button"
        className="btn btn-outline btn-sm"
        disabled={page + 1 >= totalPages}
        onClick={() => onChange(page + 1)}
      >
        다음 →
      </button>
    </div>
  );
}
