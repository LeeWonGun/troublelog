import { useNavigate } from 'react-router-dom'
import SearchBar from '../components/common/SearchBar.jsx'
import StatusChip from '../components/common/StatusChip.jsx'
import PostRow from '../components/common/PostRow.jsx'
import { MOCK_POPULAR_POSTS, MOCK_BOARD_POSTS } from '../constants/mockData.js'

// 인기 게시글 랭킹 아이템
function RankItem({ post, rank, onClick }) {
  return (
    <div className="rank-item">
      <span className="rank-num">{String(rank).padStart(2, '0')}</span>
      <span className="rank-title" onClick={onClick}>{post.title}</span>
      <StatusChip solved={post.solved} />
      <span className="rank-meta like">♡ {post.likes}</span>
      <span className="rank-meta">{post.date}</span>
    </div>
  )
}

function HomePage() {
  const navigate = useNavigate()
  // PUBLIC 게시글만 홈에 노출 (최대 4개)
  const publicPosts = MOCK_BOARD_POSTS.filter(p => p.visibility === 'PUBLIC').slice(0, 4)

  return (
    <div className="main">
      <SearchBar placeholder="에러 메시지, 키워드로 검색해보세요" />

      <div className="page-head">
        <h1><span className="eyebrow">ranked by ♡</span> 인기 게시글</h1>
      </div>
      <div className="panel">
        <div className="rank-list">
          {MOCK_POPULAR_POSTS.map((p, i) => (
            <RankItem key={p.id} post={p} rank={i + 1} onClick={() => navigate(`/questions/${p.id}`)} />
          ))}
        </div>
      </div>

      <div className="page-head">
        <h1>게시판</h1>
        <span className="more-link" style={{ cursor: 'pointer' }} onClick={() => navigate('/board')}>더보기 →</span>
      </div>
      {publicPosts.map(p => <PostRow key={p.id} post={p} />)}
    </div>
  )
}

export default HomePage