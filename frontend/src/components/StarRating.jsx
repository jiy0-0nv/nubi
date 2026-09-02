const FULL = '★';
const EMPTY = '☆';

/** readOnly면 표시용, 아니면 클릭해서 점수를 고르는 입력용 */
export default function StarRating({ value = 0, onChange, readOnly = true, size = 15 }) {
  const rounded = Math.round(Number(value) || 0);

  if (readOnly) {
    return (
      <span className="stars" style={{ fontSize: size }} aria-label={`${value}점`}>
        {[1, 2, 3, 4, 5].map((n) => (
          <span key={n} style={{ opacity: n <= rounded ? 1 : 0.28 }}>
            {n <= rounded ? FULL : EMPTY}
          </span>
        ))}
      </span>
    );
  }

  return (
    <span className="stars" style={{ fontSize: size + 8 }}>
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          type="button"
          className={`star-btn${n <= rounded ? ' on' : ''}`}
          onClick={() => onChange?.(n)}
          aria-label={`${n}점 주기`}
        >
          {n <= rounded ? FULL : EMPTY}
        </button>
      ))}
    </span>
  );
}
