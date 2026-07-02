ALTER TABLE question_tech_stacks
    DROP PRIMARY KEY;

ALTER TABLE question_tech_stacks
    ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE question_tech_stacks
    ADD CONSTRAINT uk_question_tech_stacks_question_tech
        UNIQUE (question_id, tech_stack_id);
