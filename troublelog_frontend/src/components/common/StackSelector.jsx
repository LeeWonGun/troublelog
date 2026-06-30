import { STACK_CATEGORIES } from '../../constants/mockData.js'

// 기술 스택 카테고리별 멀티 선택 컴포넌트
function StackSelector({ toggles, onToggle }) {
  return (
    <div className="stack-grid">
      {STACK_CATEGORIES.map(c => (
        <div key={c.cat} className="stack-row">
          <span className="cat">{c.cat}</span>
          <div className="chip-set">
            {c.items.map(item => {
              const key = `${c.cat}-${item}`
              return (
                <button
                  key={key}
                  className={`chip-toggle ${toggles[key] ? 'on' : ''}`}
                  onClick={() => onToggle(key)}
                >
                  {item}
                </button>
              )
            })}
          </div>
        </div>
      ))}
    </div>
  )
}

export default StackSelector