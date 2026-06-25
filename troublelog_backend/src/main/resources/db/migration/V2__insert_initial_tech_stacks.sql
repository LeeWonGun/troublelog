-- TroubleLog V2 Initial Tech Stack Data
-- File: V2__insert_initial_tech_stacks.sql

INSERT INTO tech_stacks (name, category, active)
VALUES
-- language
('Java', 'language', 1),
('Kotlin', 'language', 1),
('Python', 'language', 1),
('JavaScript', 'language', 1),

-- backend
('Spring Boot', 'backend', 1),
('Node.js', 'backend', 1),
('Django', 'backend', 1),
('Express', 'backend', 1),

-- frontend
('React', 'frontend', 1),
('Vue', 'frontend', 1),
('Next.js', 'frontend', 1),

-- database
('MySQL', 'database', 1),
('PostgreSQL', 'database', 1),
('MongoDB', 'database', 1),
('Oracle', 'database', 1),

-- devops
('Docker', 'devops', 1),
('Kubernetes', 'devops', 1),
('AWS', 'devops', 1),
('GitHub Actions', 'devops', 1),

-- tool
('Git', 'tool', 1),
('Jira', 'tool', 1),
('Notion', 'tool', 1),
('IntelliJ', 'tool', 1),

-- build tool
('Gradle', 'build tool', 1),
('Maven', 'build tool', 1),
('npm', 'build tool', 1),
('yarn', 'build tool', 1);

