import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import SearchBar from '../components/common/SearchBar.jsx'
import StatusChip from '../components/common/StatusChip.jsx'
import PostRow from '../components/common/PostRow.jsx'
import { getPopularQuestions, getPublicQuestions } from '../api/questionApi.js'

// 인기 게시글 랭킹 아이템
function RankItem({ post, rank, onClick }) {
  return (
    <div className="rank-item">
      <span className="rank-num">{String(rank).padStart(2, '0')}</span>
      <span className="rank-title" onClick={onClick}>{post.title}</span>
      <StatusChip solved={post.status === 'SOLVED'} />
      <span className="rank-meta like">♡ {post.likes}</span>
      <span className="rank-meta">{post.createdAt}</span>
    </div>
  )
}

function HomePage() {
  const navigate = useNavigate()
  const [popularPosts, setPopularPosts] = useState([])
  const [publicPosts, setPublicPosts] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchHome = async () => {
      try {
        // 인기글 + 공개 게시글 병렬 fetch
        const [popularRes, publicRes] = await Promise.all([
          getPopularQuestions(),
          getPublicQuestions({ page: 0, size: 4 }),
        ])

        setPopularPosts(popularRes ?? [])
        setPublicPosts(publicRes?.content ?? [])
      } catch (e) {
        console.error('홈 데이터 로드 실패', e)
      } finally {
        setLoading(false)
      }
    }

    fetchHome()
  }, [])

  if (loading) return <div className="main" />

  return (
    <div className="main">
      <SearchBar placeholder="에러 메시지, 키워드로 검색해보세요" />

      <div className="page-head">
        <h1><span className="eyebrow">ranked by ♥️</span> 인기 게시글</h1>
      </div>

      <div className="panel">
        <div className="rank-list">
          {popularPosts.length === 0 ? (
            <p className="empty-message">게시글이 없습니다.</p>
          ) : (
            popularPosts.map((p, i) => (
              <RankItem
                key={p.questionId}
                post={p}
                rank={i + 1}
                onClick={() => navigate(`/questions/${p.questionId}`)}
              />
            ))
          )}
        </div>
      </div>

      <div className="page-head">
        <h1>게시판</h1>
        {publicPosts.length > 0 && (
          <span className="more-link" style={{ cursor: 'pointer' }} onClick={() => navigate('/board')}>더보기 →</span>
        )}
      </div>

      <div className="panel">
        {publicPosts.length === 0
          ? <p className="empty-message">게시글이 없습니다.</p>
          : publicPosts.map(p => <PostRow key={p.id} post={p} />)
        }
      </div>
    </div>
  )
}

export default HomePage