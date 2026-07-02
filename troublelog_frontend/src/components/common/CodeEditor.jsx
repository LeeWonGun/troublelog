import Editor from '@monaco-editor/react'
import { LANG_OPTIONS, MONACO_LANGUAGE_MAP } from '../../constants/languageOptions.js'
import '../../styles/code-editor.css'

/**
 * 오류 코드 입력용 공통 에디터 (언어 선택 + Monaco)
 * - 게시글 작성/수정 페이지에서 공용 사용
 * - 상태는 상위 리듀서(writeReducer)가 소유하고, 이 컴포넌트는 표시/입력만 담당 (controlled)
 */
function CodeEditor({ language, code, onLanguageChange, onCodeChange, height = 260 }) {
  // 매핑에 없는 언어('기타' 등)는 plaintext로 폴백
  const monacoLanguage = MONACO_LANGUAGE_MAP[language] || 'plaintext'

  return (
    <div className="code-editor">
      <select
        className="select code-editor-lang"
        value={language}
        onChange={e => onLanguageChange(e.target.value)}
      >
        <option value="">언어 선택</option>
        {LANG_OPTIONS.map(l => <option key={l} value={l}>{l}</option>)}
      </select>

      <div className="code-editor-frame">
        <Editor
          height={height}
          language={monacoLanguage}
          value={code}
          theme="vs-dark"
          // Monaco onChange는 event가 아닌 value를 직접 전달하며, 전체 삭제 시 undefined가 올 수 있음
          onChange={value => onCodeChange(value ?? '')}
          loading={<div className="code-editor-loading">에디터 로딩 중...</div>}
          options={{
            minimap: { enabled: false },
            fontSize: 13,
            fontFamily: "'JetBrains Mono', monospace",
            scrollBeyondLastLine: false,
            wordWrap: 'on',
            tabSize: 2,
            automaticLayout: true, // 패널 리사이즈 시 에디터 크기 자동 갱신
            padding: { top: 12, bottom: 12 },
            renderLineHighlight: 'none',
            contextmenu: false,
          }}
        />
      </div>
    </div>
  )
}

export default CodeEditor