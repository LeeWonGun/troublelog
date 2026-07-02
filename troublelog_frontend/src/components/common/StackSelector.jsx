import { STACK_CATEGORIES } from '../../constants/mockData.js'

/**
 * 기술 스택 카테고리별 멀티 선택 컴포넌트
 * - categories 전달 시: API 데이터 기반 ({ techStackId, name }) - toggle key는 techStackId
 * - categories 미전달 시: 목업(STACK_CATEGORIES) 폴백 - SearchDetailModal 등 미연동 화면 하위 호환
 */
function StackSelector({ categories, toggles, onToggle }) {
  const list = categories?.length ? categories : STACK_CATEGORIES

  return (
    <div className="stack-grid">
      {list.map(c => (
        <div key={c.cat} className="stack-row">
          <span className="cat">{c.cat}</span>
          <div className="chip-set">
            {c.items.map(item => {
              // API 항목은 객체, 목업 항목은 string이므로 형태에 따라 key/표시명 분기
              const isMock = typeof item === 'string'
              const key = isMock ? `${c.cat}-${item}` : item.techStackId
              const name = isMock ? item : item.name
              return (
                <button
                  key={key}
                  className={`chip-toggle ${toggles[key] ? 'on' : ''}`}
                  onClick={() => onToggle(key)}
                >
                  {name}
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