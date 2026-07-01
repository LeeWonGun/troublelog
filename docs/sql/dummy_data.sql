-- TroubleLog Dummy Data
-- File: docs/sql/dummy_data.sql
-- Purpose: Local development / API test / Frontend screen test

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 주의:
-- 이 파일은 개발/테스트용 더미 데이터입니다.
-- 실행할 때마다 기존 테스트 데이터를 삭제하고 다시 삽입합니다.
-- tech_stacks 테이블은 Flyway V2에서 관리한다고 가정하고 삭제하지 않습니다.

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE likes;
TRUNCATE TABLE question_files;
TRUNCATE TABLE question_tech_stacks;
TRUNCATE TABLE answers;
TRUNCATE TABLE questions;
TRUNCATE TABLE team_members;
TRUNCATE TABLE teams;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. users
-- =========================================================
-- LOCAL 사용자 테스트 비밀번호는 모두 password 입니다.
-- 아래 password_hash는 BCrypt로 암호화된 password 예시입니다.

INSERT INTO users
(id, email, password_hash, nickname, auth_provider, provider_id, delflag, created_at, updated_at)
VALUES
    (1, 'leader@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'leaderUser', 'LOCAL', NULL, 'N', NOW(), NULL),
    (2, 'backend@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'backendDev', 'LOCAL', NULL, 'N', NOW(), NULL),
    (3, 'frontend@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'frontendDev', 'LOCAL', NULL, 'N', NOW(), NULL),
    (4, 'googleuser@gmail.com', NULL, 'userA7K9Q2LM', 'GOOGLE', 'google-provider-id-001', 'N', NOW(), NULL),
    (5, 'outsider@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'outsideUser', 'LOCAL', NULL, 'N', NOW(), NULL);

-- =========================================================
-- 2. teams
-- =========================================================

INSERT INTO teams
(id, name, description, team_code, owner_id, delflag, created_at, updated_at)
VALUES
    (1, 'TroubleLog Backend Team', 'TroubleLog 백엔드 기능 개발팀입니다.', 'BACK1234', 1, 'N', NOW(), NULL),
    (2, 'TroubleLog Frontend Team', 'TroubleLog 프론트엔드 화면 개발팀입니다.', 'FRONT1234', 1, 'N', NOW(), NULL),
    (3, 'Deleted Test Team', '삭제된 팀 테스트용 데이터입니다.', 'DEL1234', 1, 'Y', NOW(), NULL);

-- =========================================================
-- 3. team_members
-- =========================================================

INSERT INTO team_members
(id, user_id, team_id, role, joined_at, leaved_at, delflag)
VALUES
    (1, 1, 1, 'LEADER', NOW(), NULL, 'N'),
    (2, 2, 1, 'MEMBER', NOW(), NULL, 'N'),
    (3, 3, 1, 'MEMBER', NOW(), NULL, 'N'),


    (4, 1, 2, 'LEADER', NOW(), NULL, 'N'),
    (5, 3, 2, 'MEMBER', NOW(), NULL, 'N'),
    (6, 4, 2, 'MEMBER', NOW(), NULL, 'N'),

    (7, 5, 1, 'MEMBER', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), 'Y'),
    (8, 1, 3, 'LEADER', NOW(), NULL, 'Y');


-- =========================================================
-- 4. questions
-- =========================================================

INSERT INTO questions
(id, writer_id, team_id, accepted_answer_id, title, content, error_message, environment, tried, visibility, status, answer_count, view_count, like_count, delflag, created_at, updated_at)
VALUES
    (1, 1, NULL, NULL,
    'Spring Boot 서버 실행 시 8080 포트 충돌 문제',
    'Spring Boot 서버를 실행하려고 하는데 8080 포트가 이미 사용 중이라는 오류가 발생합니다. 어떤 프로세스가 포트를 사용 중인지 확인하고 종료하는 방법이 궁금합니다.

```javascript
import { useEffect, useState } from ''react''

export default function Board() {
  const [count, setCount] = useState(0)
  const options = { limit: 10 }

  useEffect(() => {
    fetch(''/api/posts?limit='' + options.limit)
      .then(res => res.json())
      .then(() => {
        setCount(prev => prev + 1)
      })
  }, [options])

  return <div>{count}</div>
}
```',
    'Port 8080 was already in use',
    'Windows 11, Java 21, Spring Boot 4, IntelliJ',
    'netstat으로 포트를 확인해보려고 했지만 어떤 프로세스를 종료해야 하는지 모르겠습니다.',
    'PUBLIC', 'UNSOLVED', 0, 24, 0, 'N', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL),

    (2, 2, 1, NULL,
     'Docker Compose MySQL 연결 실패',
     'docker compose로 MySQL 컨테이너를 실행했는데 Spring Boot에서 DB 연결이 되지 않습니다.',
     'Communications link failure',
     'Windows 11, Docker Desktop, MySQL 8, Spring Boot',
     'docker ps로 컨테이너 실행 여부를 확인했고 application.properties도 확인했습니다.',
     'TEAM', 'UNSOLVED', 0, 18, 0, 'N', DATE_SUB(NOW(), INTERVAL 4 DAY), NULL),

    (3, 3, NULL, NULL,
     'React에서 Spring Boot API 호출 시 CORS 오류',
     'React Vite 프로젝트에서 Spring Boot API를 호출하면 CORS 오류가 발생합니다.',
     'Access to fetch at http://localhost:8080 from origin http://localhost:5173 has been blocked by CORS policy',
     'React, Vite, Spring Boot, Axios',
     'Controller에 @CrossOrigin을 붙여봤지만 전체 설정이 맞는지 모르겠습니다.',
     'PUBLIC', 'UNSOLVED', 0, 35, 0, 'N', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL),

    (4, 1, NULL, NULL,
     'Flyway V1 수정 후 checksum mismatch 오류',
     'V1__init_schema.sql을 수정했더니 Spring Boot 실행 시 Flyway checksum mismatch 오류가 발생합니다.',
     'Validate failed: Migration checksum mismatch for migration version 1',
     'Spring Boot, Flyway, MySQL, Docker Compose',
     'docker compose down만 실행하고 다시 서버를 실행했습니다.',
     'PUBLIC', 'UNSOLVED', 0, 42, 0, 'N', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),

    (5, 2, 1, NULL,
     '팀 게시글이 팀원이 아닌 사용자에게도 보이는 문제',
     'TEAM visibility로 작성한 게시글이 팀원이 아닌 사용자에게도 조회되는 것 같습니다.',
     NULL,
     'Spring Security, JWT, MySQL',
     'team_members 조건을 추가해야 하는지 확인 중입니다.',
     'TEAM', 'UNSOLVED', 0, 12, 0, 'N', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),

    (6, 4, 2, NULL,
     'Google 로그인 사용자 닉네임 처리 방식',
     'Google 로그인 사용자는 비밀번호가 없는데 nickname을 어떤 방식으로 생성하고 수정해야 할지 궁금합니다.',
     NULL,
     'Spring Security OAuth2, JWT',
     'Google email 앞부분을 nickname으로 쓰는 방법을 생각해봤습니다.',
     'TEAM', 'UNSOLVED', 0, 9, 0, 'N', DATE_SUB(NOW(), INTERVAL 12 HOUR), NULL),

    (7, 5, NULL, NULL,
     '삭제된 질문 테스트 데이터',
     '이 질문은 delflag 테스트용입니다.',
     NULL,
     'Test',
     'None',
     'PUBLIC', 'UNSOLVED', 0, 0, 0, 'Y', DATE_SUB(NOW(), INTERVAL 10 DAY), NULL),

    (8, 1, NULL, NULL,
     'Flyway V1 수정 후 checksum mismatch 오류',
     'V1__init_schema.sql을 수정했더니 Spring Boot 실행 시 Flyway checksum mismatch 오류가 발생합니다.',
     'Validate failed: Migration checksum mismatch for migration version 1',
     'Spring Boot, Flyway, MySQL, Docker Compose',
     'docker compose down만 실행하고 다시 서버를 실행했습니다.',
     'PUBLIC', 'UNSOLVED', 0, 42, 0, 'N', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),

    (9, 1, NULL, NULL,
     'Flyway V1 수정 후 checksum mismatch 오류',
     'V1__init_schema.sql을 수정했더니 Spring Boot 실행 시 Flyway checksum mismatch 오류가 발생합니다.',
     'Validate failed: Migration checksum mismatch for migration version 1',
     'Spring Boot, Flyway, MySQL, Docker Compose',
     'docker compose down만 실행하고 다시 서버를 실행했습니다.',
     'PUBLIC', 'UNSOLVED', 0, 42, 0, 'N', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),
    (10, 1, NULL, NULL,
     'Flyway V1 수정 후 checksum mismatch 오류',
     'V1__init_schema.sql을 수정했더니 Spring Boot 실행 시 Flyway checksum mismatch 오류가 발생합니다.',
     'Validate failed: Migration checksum mismatch for migration version 1',
     'Spring Boot, Flyway, MySQL, Docker Compose',
     'docker compose down만 실행하고 다시 서버를 실행했습니다.',
     'PUBLIC', 'UNSOLVED', 0, 42, 0, 'N', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),
    (11, 1, NULL, NULL,
     'Flyway V1 수정 후 checksum mismatch 오류',
     'V1__init_schema.sql을 수정했더니 Spring Boot 실행 시 Flyway checksum mismatch 오류가 발생합니다.',
     'Validate failed: Migration checksum mismatch for migration version 1',
     'Spring Boot, Flyway, MySQL, Docker Compose',
     'docker compose down만 실행하고 다시 서버를 실행했습니다.',
     'PUBLIC', 'UNSOLVED', 0, 42, 0, 'N', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),
    (12, 1, NULL, NULL,
     'Flyway V1 수정 후 checksum mismatch 오류',
     'V1__init_schema.sql을 수정했더니 Spring Boot 실행 시 Flyway checksum mismatch 오류가 발생합니다.',
     'Validate failed: Migration checksum mismatch for migration version 1',
     'Spring Boot, Flyway, MySQL, Docker Compose',
     'docker compose down만 실행하고 다시 서버를 실행했습니다.',
     'PUBLIC', 'UNSOLVED', 0, 42, 0, 'N', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),
    (13, 1, NULL, NULL,
     'Flyway V1 수정 후 checksum mismatch 오류',
     'V1__init_schema.sql을 수정했더니 Spring Boot 실행 시 Flyway checksum mismatch 오류가 발생합니다.',
     'Validate failed: Migration checksum mismatch for migration version 1',
     'Spring Boot, Flyway, MySQL, Docker Compose',
     'docker compose down만 실행하고 다시 서버를 실행했습니다.',
     'PUBLIC', 'UNSOLVED', 0, 42, 0, 'N', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),
    (14, 1, NULL, NULL,
     'Flyway V1 수정 후 checksum mismatch 오류',
     'V1__init_schema.sql을 수정했더니 Spring Boot 실행 시 Flyway checksum mismatch 오류가 발생합니다.',
     'Validate failed: Migration checksum mismatch for migration version 1',
     'Spring Boot, Flyway, MySQL, Docker Compose',
     'docker compose down만 실행하고 다시 서버를 실행했습니다.',
     'PUBLIC', 'UNSOLVED', 0, 42, 0, 'N', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),
    (15, 1, NULL, NULL,
     'Flyway V1 수정 후 checksum mismatch 오류',
     'V1__init_schema.sql을 수정했더니 Spring Boot 실행 시 Flyway checksum mismatch 오류가 발생합니다.',
     'Validate failed: Migration checksum mismatch for migration version 1',
     'Spring Boot, Flyway, MySQL, Docker Compose',
     'docker compose down만 실행하고 다시 서버를 실행했습니다.',
     'PUBLIC', 'UNSOLVED', 0, 42, 0, 'N', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL);
     


-- =========================================================
-- 5. answers
-- =========================================================

INSERT INTO answers
(id, question_id, writer_id, parent_answer_id, depth, content, like_count, delflag, created_at, updated_at)
VALUES
    (1, 1, 2, NULL, 0,
    'Windows에서는 netstat -ano | findstr :8080 명령어로 8080 포트를 사용 중인 PID를 확인할 수 있습니다.',
    0, 'N', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL),


    (2, 1, 1, 1, 1,
     'PID를 확인한 다음에는 어떻게 종료하면 될까요?',
     0, 'N', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL),

    (3, 1, 2, 2, 2,
     'taskkill /PID PID번호 /F 명령어를 사용하면 됩니다. 관리자 권한 터미널에서 실행하는 것이 좋습니다.',
     0, 'N', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL),

    (4, 2, 1, NULL, 0,
     'Spring Boot에서 바라보는 DB host가 localhost인지 컨테이너 서비스명인지 확인해보세요. 컨테이너 내부 통신이면 서비스명을 사용해야 합니다.',
     0, 'N', DATE_SUB(NOW(), INTERVAL 4 DAY), NULL),

    (5, 3, 1, NULL, 0,
     'Spring Security를 사용 중이라면 SecurityConfig에서 CORS 설정을 등록하고, 허용 origin에 프론트 주소를 추가해야 합니다.',
     0, 'N', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL),

    (6, 4, 2, NULL, 0,
     '이미 적용된 Flyway V1을 수정하면 checksum mismatch가 발생합니다. 개발 초기라면 DB 스키마 또는 볼륨을 초기화한 뒤 다시 실행하면 됩니다.',
     0, 'N', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),

    (7, 4, 3, NULL, 0,
     '운영 환경이라면 V1을 수정하지 말고 V2, V3 같은 새로운 migration 파일을 만들어야 합니다.',
     0, 'N', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),

    (8, 5, 1, NULL, 0,
     'TEAM 게시글 조회 시 questions.team_id와 team_members.team_id를 조인하고 현재 사용자 ID 조건을 반드시 넣어야 합니다.',
     0, 'N', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),

    (9, 6, 1, NULL, 0,
     'Google 사용자의 nickname은 서버에서 랜덤 문자열로 생성하고, 이후 마이페이지에서 변경할 수 있게 하는 방식이 좋습니다.',
     0, 'N', DATE_SUB(NOW(), INTERVAL 10 HOUR), NULL),

    (10, 1, 5, NULL, 0,
     '삭제된 답변 테스트 데이터입니다.',
     0, 'Y', DATE_SUB(NOW(), INTERVAL 8 DAY), NULL);


-- =========================================================
-- 6. accepted answer
-- =========================================================
-- 4번 질문은 6번 답변을 채택한 상태로 둔다.
-- 이 데이터로 채택 답변 수정/삭제 제한 테스트를 할 수 있다.

UPDATE questions
SET accepted_answer_id = 6,
    status = 'SOLVED'
WHERE id = 4;

-- =========================================================
-- 7. answer_count 업데이트
-- =========================================================

UPDATE questions q
SET answer_count = (
    SELECT COUNT(*)
    FROM answers a
    WHERE a.question_id = q.id
      AND a.depth = 0
      AND a.delflag = 'N'
);

-- =========================================================
-- 8. question_tech_stacks
-- =========================================================
-- tech_stacks는 V2에서 이미 들어갔다고 가정한다.
-- 아래 INSERT는 기술 스택 name 기준으로 id를 조회해서 연결한다.

INSERT INTO question_tech_stacks (question_id, tech_stack_id)
SELECT 1, id FROM tech_stacks WHERE name IN ('Java', 'Spring Boot');

INSERT INTO question_tech_stacks (question_id, tech_stack_id)
SELECT 2, id FROM tech_stacks WHERE name IN ('Spring Boot', 'MySQL', 'Docker');

INSERT INTO question_tech_stacks (question_id, tech_stack_id)
SELECT 3, id FROM tech_stacks WHERE name IN ('React', 'Spring Boot');

INSERT INTO question_tech_stacks (question_id, tech_stack_id)
SELECT 4, id FROM tech_stacks WHERE name IN ('Spring Boot', 'Flyway', 'MySQL', 'Docker');

INSERT INTO question_tech_stacks (question_id, tech_stack_id)
SELECT 5, id FROM tech_stacks WHERE name IN ('Spring Security', 'JWT', 'MySQL');

INSERT INTO question_tech_stacks (question_id, tech_stack_id)
SELECT 6, id FROM tech_stacks WHERE name IN ('Spring Security', 'OAuth2', 'JWT');

-- =========================================================
-- 9. question_files
-- =========================================================
-- 실제 파일은 없는 DB 메타데이터 테스트용 데이터다.
-- 실제 파일 다운로드/이미지 조회 API 테스트는 파일 업로드 API로 진행하는 것을 권장한다.

INSERT INTO question_files
(id, question_id, original_filename, upload_filename, file_path, file_url, content_type, file_size, delflag, created_at, deleted_at, purge_due_at, purged_at)
VALUES
    (1, 1, 'port_error.png', 'dummy-port-error.png', '/uploads/questions/dummy-port-error.png', '/api/files/1', 'image/png', 124000, 'N', NOW(), NULL, NULL, NULL),
    (2, 3, 'cors_error.png', 'dummy-cors-error.png', '/uploads/questions/dummy-cors-error.png', '/api/files/2', 'image/png', 98000, 'N', NOW(), NULL, NULL, NULL),
    (3, 7, 'deleted_question_file.png', 'dummy-deleted-file.png', '/uploads/questions/dummy-deleted-file.png', '/api/files/3', 'image/png', 80000, 'Y', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NULL);

-- =========================================================
-- 10. likes
-- =========================================================

INSERT INTO likes
(id, user_id, target_id, target_type, created_at)
VALUES
    (1, 2, 1, 'QUE', NOW()),
    (2, 3, 1, 'QUE', NOW()),
    (3, 4, 1, 'QUE', NOW()),
    (4, 1, 3, 'QUE', NOW()),
    (5, 2, 3, 'QUE', NOW()),
    (6, 3, 4, 'QUE', NOW()),
    (7, 5, 4, 'QUE', NOW()),


    (8, 1, 1, 'ANS', NOW()),
    (9, 3, 1, 'ANS', NOW()),
    (10, 1, 4, 'ANS', NOW()),
    (11, 2, 6, 'ANS', NOW()),
    (12, 3, 6, 'ANS', NOW()),
    (13, 2, 9, 'ANS', NOW());


-- =========================================================
-- 11. like_count 업데이트
-- =========================================================

UPDATE questions q
SET like_count = (
    SELECT COUNT(*)
    FROM likes l
    WHERE l.target_type = 'QUE'
    AND l.target_id = q.id
    );

UPDATE answers a
SET like_count = (
    SELECT COUNT(*)
    FROM likes l
    WHERE l.target_type = 'ANS'
      AND l.target_id = a.id
);

-- =========================================================
-- 12. 확인용 SELECT
-- =========================================================

SELECT 'dummy data inserted successfully' AS result;

SELECT
    'users' AS table_name,
    COUNT(*) AS row_count
FROM users
UNION ALL
SELECT 'teams', COUNT(*) FROM teams
UNION ALL
SELECT 'team_members', COUNT(*) FROM team_members
UNION ALL
SELECT 'questions', COUNT(*) FROM questions
UNION ALL
SELECT 'answers', COUNT(*) FROM answers
UNION ALL
SELECT 'question_tech_stacks', COUNT(*) FROM question_tech_stacks
UNION ALL
SELECT 'question_files', COUNT(*) FROM question_files
UNION ALL
SELECT 'likes', COUNT(*) FROM likes;
