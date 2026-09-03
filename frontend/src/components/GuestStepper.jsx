/** 인원수를 -, + 버튼으로 증감시키는 스테퍼. min/max 범위를 벗어나면 해당 버튼이 비활성화됩니다. */
export default function GuestStepper({ value, onChange, min = 1, max = 8 }) {
  const dec = () => onChange(Math.max(min, value - 1));
  const inc = () => onChange(Math.min(max, value + 1));

  return (
    <div className="stepper">
      <button type="button" className="stepper-btn" onClick={dec} disabled={value <= min} aria-label="인원 줄이기">
        −
      </button>
      <span className="stepper-value">{value}명</span>
      <button type="button" className="stepper-btn" onClick={inc} disabled={value >= max} aria-label="인원 늘리기">
        +
      </button>
    </div>
  );
}
