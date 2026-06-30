import { Routes, Route, Navigate } from 'react-router-dom'
import AppLayout from '../components/layout/AppLayout.jsx'
import LoginPage from '../pages/LoginPage.jsx'
import SignupPage from '../pages/SignupPage.jsx'
import ResetPasswordPage from '../pages/ResetPasswordPage.jsx'
import HomePage from '../pages/HomePage.jsx'
import QuestionListPage from '../pages/QuestionListPage.jsx'
import QuestionDetailPage from '../pages/QuestionDetailPage.jsx'
import QuestionCreatePage from '../pages/QuestionCreatePage.jsx'
import QuestionEditPage from '../pages/QuestionEditPage.jsx'
import MyPage from '../pages/MyPage.jsx'
import TeamCreatePage from '../pages/TeamCreatePage.jsx'
import TeamJoinPage from '../pages/TeamJoinPage.jsx'
import TeamListPage from '../pages/TeamListPage.jsx'

function ApiRouter() {
  return (
    <Routes>
      {/* 인증 페이지: 사이드바 없음 */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      {/* 앱 페이지: 사이드바 포함 레이아웃 */}
      <Route element={<AppLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/board" element={<QuestionListPage />} />
        <Route path="/questions/create" element={<QuestionCreatePage />} />
        <Route path="/questions/:id" element={<QuestionDetailPage />} />
        <Route path="/questions/:id/edit" element={<QuestionEditPage />} />
        <Route path="/mypage" element={<MyPage />} />
        <Route path="/teams" element={<TeamListPage />} />
        <Route path="/teams/create" element={<TeamCreatePage />} />
        <Route path="/teams/join" element={<TeamJoinPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default ApiRouter