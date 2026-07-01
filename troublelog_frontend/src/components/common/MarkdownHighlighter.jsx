import ReactMarkdown from 'react-markdown'
import rehypeHighlight from 'rehype-highlight'
import 'highlight.js/styles/github-dark.css'

const LANGUAGE_ALIAS = {
  js: 'javascript',
  ts: 'typescript',
  py: 'python',
  sh: 'bash',
  shell: 'bash',
  기타: 'text',
}

export function normalizeCodeLanguage(language) {
  const raw = (language ?? '').trim().toLowerCase()
  if (!raw || raw === '기타') return 'text'
  return LANGUAGE_ALIAS[raw] ?? raw
}

function MarkdownHighlighter({ markdown = '', emptyText = '내용이 없습니다.' }) {
  const safeMarkdown = markdown.trim()

  if (!safeMarkdown) {
    return <p className="markdown-empty">{emptyText}</p>
  }

  return (
    <div className="markdown-render">
      <ReactMarkdown rehypePlugins={[rehypeHighlight]}>
        {safeMarkdown}
      </ReactMarkdown>
    </div>
  )
}

export default MarkdownHighlighter