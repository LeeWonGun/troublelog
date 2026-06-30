// compact: 마이페이지처럼 패널 내부에 위치할 때 상단 구분선 + 여백 적용
function Pagination({ current, total, onChange, compact = false }) {
  if (total <= 1) return null

  const pages = Array.from({ length: total }, (_, i) => i + 1)

  return (
    <div className={`pagination ${compact ? 'pagination--compact' : ''}`}>
      <span className={current === 1 ? 'disabled' : ''} onClick={() => current > 1 && onChange(1)}>처음</span>
      <span className={current === 1 ? 'disabled' : ''} onClick={() => current > 1 && onChange(current - 1)}>‹</span>
      {pages.map(p => (
        <span key={p} className={p === current ? 'current' : ''} onClick={() => onChange(p)}>{p}</span>
      ))}
      <span className={current === total ? 'disabled' : ''} onClick={() => current < total && onChange(current + 1)}>›</span>
      <span className={current === total ? 'disabled' : ''} onClick={() => current < total && onChange(total)}>마지막</span>
    </div>
  )
}

export default Pagination