export const STACK_CATEGORIES = [
  { cat: 'language',   items: ['Java', 'Kotlin', 'Python', 'JavaScript'] },
  { cat: 'backend',    items: ['Spring Boot', 'Node.js', 'Django', 'Express'] },
  { cat: 'frontend',   items: ['React', 'Vue', 'Next.js'] },
  { cat: 'database',   items: ['MySQL', 'PostgreSQL', 'MongoDB', 'Oracle'] },
  { cat: 'devops',     items: ['Docker', 'Kubernetes', 'AWS', 'GitHub Actions'] },
  { cat: 'tool',       items: ['Git', 'Jira', 'Notion', 'IntelliJ'] },
  { cat: 'build tool', items: ['Gradle', 'Maven', 'npm', 'yarn'] },
]

export const MOCK_POPULAR_POSTS = [
  { id: 1, title: '[Spring] 마이페이지 진입 시 500 Internal Server Error', likes: 128, date: '2026.06.23', solved: true },
  { id: 2, title: 'React useEffect 무한 루프 도는 이유를 모르겠습니다',      likes: 96,  date: '2026.05.23', solved: false },
  { id: 3, title: 'Docker 컨테이너 재시작마다 환경변수가 초기화돼요',         likes: 74,  date: '2026.06.20', solved: true },
  { id: 4, title: 'JWT 만료 시 자동 로그아웃이 동작하지 않습니다',           likes: 61,  date: '2026.06.03', solved: false },
  { id: 5, title: 'Next.js 로컬은 되는데 배포하면 500 에러가 납니다',        likes: 53,  date: '2026.06.15', solved: true },
]

export const MOCK_BOARD_POSTS = [
  { id: 11, title: '[Spring] 마이페이지 진입 시 500 Internal Server Error', tags: ['JAVA', 'Spring'],         likes: 128, solved: false, team: 1, visibility: 'PUBLIC' },
  { id: 12, title: 'React useEffect 무한 루프 도는 이유를 모르겠습니다',      tags: ['React', 'JavaScript'], likes: 96,  solved: true,  team: 1, visibility: 'TEAM'   },
  { id: 13, title: 'Docker 컨테이너 재시작마다 환경변수가 초기화돼요',         tags: ['Docker', 'DevOps'],    likes: 74,  solved: false, team: 2, visibility: 'PUBLIC' },
  { id: 14, title: 'Next.js 빌드는 되는데 배포만 하면 500 에러가 납니다',     tags: ['Next.js', 'AWS'],      likes: 53,  solved: true,  team: 1, visibility: 'TEAM'   },
  { id: 15, title: 'JPA N+1 문제, fetch join으로도 안 잡혀요',               tags: ['JAVA', 'Spring'],      likes: 41,  solved: false, team: 3, visibility: 'PUBLIC' },
  { id: 16, title: 'GitHub Actions 워크플로우가 캐시를 계속 무시합니다',       tags: ['GitHub Actions'],      likes: 19,  solved: true,  team: 2, visibility: 'TEAM'   },
]

export const MOCK_ANSWERS = [
  { id: 101, author: '김철수', body: 'getUser() 호출 전에 인증 인터셉터에서 SecurityContext가 비어있는 경우입니다. 배포 환경 필터 체인 순서를 한번 확인해보세요.', likes: 6, comments: 2 },
  { id: 102, author: '이영희', body: '로컬과 배포 환경의 세션 저장 방식이 다른 경우가 많습니다. Redis 세션 설정값을 비교해보시면 좋을 것 같아요.',                 likes: 0, comments: 0 },
  { id: 103, author: '박민수', body: 'NPE 스택트레이스 상으로는 빈 주입이 안 된 것처럼 보이는데, @Service 어노테이션 누락 아닐까요?',                               likes: 0, comments: 1 },
]

export const MOCK_MY_QUESTIONS = [
  { id: 1, title: '[Spring] 마이페이지 진입 시 500 Internal Server Error', likes: 128, date: '2026.06.23' },
  { id: 2, title: 'React useEffect 무한 루프 도는 이유를 모르겠습니다',      likes: 96,  date: '2026.05.23' },
  { id: 3, title: 'Docker 컨테이너 재시작마다 환경변수가 초기화돼요',         likes: 74,  date: '2026.06.20' },
  { id: 4, title: 'JWT 만료 시 자동 로그아웃이 동작하지 않습니다',           likes: 61,  date: '2026.06.03' },
  { id: 5, title: 'Next.js 로컬은 되는데 배포하면 500 에러가 납니다',        likes: 53,  date: '2026.05.18' },
  { id: 6, title: 'JPA N+1 문제, fetch join으로도 안 잡혀요',               likes: 41,  date: '2026.05.02' },
]

export const MOCK_MY_ANSWERS = [
  { id: 1, title: 'JWT 만료 시 자동 로그아웃이 동작하지 않습니다',        likes: 32, date: '2026.06.23', accepted: true  },
  { id: 2, title: 'GitHub Actions 워크플로우가 캐시를 계속 무시합니다',    likes: 8,  date: '2026.06.10', accepted: false },
  { id: 3, title: 'Spring Security 필터 순서가 뒤바뀌는 문제',            likes: 14, date: '2026.06.02', accepted: true  },
  { id: 4, title: 'MySQL 인덱스가 안 타는 쿼리 튜닝 방법',               likes: 5,  date: '2026.05.21', accepted: false },
  { id: 5, title: 'Docker Compose 네트워크 분리가 안 돼요',              likes: 2,  date: '2026.05.09', accepted: false },
]

export const MOCK_MY_COMMENTS = [
  { id: 1, body: '저도 같은 문제 겪었는데 필터 체인 순서 문제였어요.',            on: '[Spring] 마이페이지 진입 시 500 Internal Server Error',  date: '2026.06.23' },
  { id: 2, body: 'Redis 세션 TTL 설정도 같이 확인해보시는 걸 추천해요.',         on: 'React useEffect 무한 루프 도는 이유를 모르겠습니다',       date: '2026.06.11' },
  { id: 3, body: '감사합니다! 말씀하신 방법으로 해결했습니다.',                   on: 'Docker 컨테이너 재시작마다 환경변수가 초기화돼요',           date: '2026.06.05' },
  { id: 4, body: '토큰 갱신 로직에 refresh 만료 체크가 빠진 것 같아요.',         on: 'JWT 만료 시 자동 로그아웃이 동작하지 않습니다',             date: '2026.05.30' },
  { id: 5, body: '캐시 키에 OS 버전이 포함돼서 매번 미스 나는 걸 수도 있어요.',  on: 'GitHub Actions 워크플로우가 캐시를 계속 무시합니다',        date: '2026.05.20' },
  { id: 6, body: '복합 인덱스 순서를 바꿔보시는 것도 추천드려요.',               on: 'MySQL 인덱스가 안 타는 쿼리 튜닝 방법',                   date: '2026.05.12' },
  { id: 7, body: '네트워크 이름을 명시적으로 지정해보세요.',                     on: 'Docker Compose 네트워크 분리가 안 돼요',                  date: '2026.05.10' },
]