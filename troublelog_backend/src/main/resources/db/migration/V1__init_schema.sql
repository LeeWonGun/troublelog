-- TroubleLog V1 Initial Schema
-- File: V1__init_schema.sql

CREATE TABLE users
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NULL,
    nickname      VARCHAR(50)  NOT NULL,
    auth_provider VARCHAR(20)  NOT NULL,
    provider_id   VARCHAR(255) NULL,
    delflag       CHAR(1)      NOT NULL DEFAULT 'N',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NULL,

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_nickname UNIQUE (nickname),
    CONSTRAINT chk_users_auth_provider CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),
    CONSTRAINT chk_users_delflag CHECK (delflag IN ('N', 'Y'))
);

CREATE TABLE teams
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT NULL,
    team_code   VARCHAR(50)  NOT NULL,
    owner_id    BIGINT       NOT NULL,
    delflag     CHAR(1)      NOT NULL DEFAULT 'N',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NULL,

    CONSTRAINT uk_teams_team_code UNIQUE (team_code),
    CONSTRAINT chk_teams_delflag CHECK (delflag IN ('N', 'Y')),
    CONSTRAINT fk_teams_owner
        FOREIGN KEY (owner_id)
            REFERENCES users (id)
);

CREATE TABLE team_members
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id   BIGINT      NOT NULL,
    team_id   BIGINT      NOT NULL,
    role      VARCHAR(30) NOT NULL,
    joined_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leaved_at DATETIME NULL,
    delflag   CHAR(1)     NOT NULL DEFAULT 'N',

    CONSTRAINT uk_team_members_team_user UNIQUE (team_id, user_id),
    CONSTRAINT chk_team_members_role CHECK (role IN ('LEADER', 'MEMBER')),
    CONSTRAINT chk_team_members_delflag CHECK (delflag IN ('N', 'Y')),
    CONSTRAINT fk_team_members_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),
    CONSTRAINT fk_team_members_team
        FOREIGN KEY (team_id)
            REFERENCES teams (id)
);

CREATE TABLE questions
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    writer_id          BIGINT       NOT NULL,
    team_id            BIGINT NULL,
    accepted_answer_id BIGINT NULL,
    title              VARCHAR(200) NOT NULL,
    content            TEXT         NOT NULL,
    error_message      TEXT NULL,
    environment        TEXT NULL,
    tried              TEXT NULL,
    visibility         VARCHAR(20)  NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'UNSOLVED',
    answer_count       INT          NOT NULL DEFAULT 0,
    view_count         INT          NOT NULL DEFAULT 0,
    like_count         INT          NOT NULL DEFAULT 0,
    delflag            CHAR(1)      NOT NULL DEFAULT 'N',
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME NULL,

    CONSTRAINT chk_questions_visibility CHECK (visibility IN ('PUBLIC', 'TEAM')),
    CONSTRAINT chk_questions_status CHECK (status IN ('UNSOLVED', 'SOLVED')),
    CONSTRAINT chk_questions_delflag CHECK (delflag IN ('N', 'Y')),
    CONSTRAINT fk_questions_writer
        FOREIGN KEY (writer_id)
            REFERENCES users (id),
    CONSTRAINT fk_questions_team
        FOREIGN KEY (team_id)
            REFERENCES teams (id)
);

CREATE TABLE answers
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id      BIGINT   NOT NULL,
    writer_id        BIGINT   NOT NULL,
    parent_answer_id BIGINT NULL,
    depth            TINYINT  NOT NULL DEFAULT 0,
    content          TEXT     NOT NULL,
    like_count       INT      NOT NULL DEFAULT 0,
    delflag          CHAR(1)  NOT NULL DEFAULT 'N',
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NULL,

    CONSTRAINT chk_answers_depth CHECK (depth IN (0, 1, 2)),
    CONSTRAINT chk_answers_delflag CHECK (delflag IN ('N', 'Y')),
    CONSTRAINT fk_answers_question
        FOREIGN KEY (question_id)
            REFERENCES questions (id),
    CONSTRAINT fk_answers_writer
        FOREIGN KEY (writer_id)
            REFERENCES users (id),
    CONSTRAINT fk_answers_parent
        FOREIGN KEY (parent_answer_id)
            REFERENCES answers (id)
);

ALTER TABLE questions
    ADD CONSTRAINT fk_questions_accepted_answer
        FOREIGN KEY (accepted_answer_id)
            REFERENCES answers (id);

CREATE TABLE tech_stacks
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    category   VARCHAR(50) NOT NULL,
    active     TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_tech_stacks_name UNIQUE (name),
    CONSTRAINT chk_tech_stacks_active CHECK (active IN (0, 1))
);

CREATE TABLE question_tech_stacks
(
    question_id   BIGINT NOT NULL,
    tech_stack_id BIGINT NOT NULL,

    CONSTRAINT pk_question_tech_stacks PRIMARY KEY (question_id, tech_stack_id),
    CONSTRAINT fk_question_tech_stacks_question
        FOREIGN KEY (question_id)
            REFERENCES questions (id),
    CONSTRAINT fk_question_tech_stacks_tech_stack
        FOREIGN KEY (tech_stack_id)
            REFERENCES tech_stacks (id)
);

CREATE TABLE question_files
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id       BIGINT       NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    upload_filename   VARCHAR(255) NOT NULL,
    file_path         VARCHAR(500) NOT NULL,
    file_url          VARCHAR(500) NOT NULL,
    content_type      VARCHAR(100) NOT NULL,
    file_size         BIGINT       NOT NULL,
    delflag           CHAR(1)      NOT NULL DEFAULT 'N',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at        DATETIME NULL,
    purge_due_at      DATETIME NULL,
    purged_at         DATETIME NULL,

    CONSTRAINT chk_question_files_delflag CHECK (delflag IN ('N', 'Y')),
    CONSTRAINT fk_question_files_question
        FOREIGN KEY (question_id)
            REFERENCES questions (id)
);

CREATE TABLE likes
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    target_id   BIGINT      NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_likes_target_user UNIQUE (target_type, target_id, user_id),
    CONSTRAINT chk_likes_target_type CHECK (target_type IN ('QUE', 'ANS')),
    CONSTRAINT fk_likes_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);