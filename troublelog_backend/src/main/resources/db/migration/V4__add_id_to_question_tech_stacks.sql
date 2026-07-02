ALTER TABLE question_tech_stacks
    DROP FOREIGN KEY fk_question_tech_stacks_question;

ALTER TABLE question_tech_stacks
    DROP FOREIGN KEY fk_question_tech_stacks_tech_stack;

ALTER TABLE question_tech_stacks
    DROP PRIMARY KEY;

ALTER TABLE question_tech_stacks
    ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE question_tech_stacks
    ADD CONSTRAINT uk_question_tech_stacks_question_tech
        UNIQUE (question_id, tech_stack_id);

ALTER TABLE question_tech_stacks
    ADD CONSTRAINT fk_question_tech_stacks_question
        FOREIGN KEY (question_id)
            REFERENCES questions (id);

ALTER TABLE question_tech_stacks
    ADD CONSTRAINT fk_question_tech_stacks_tech_stack
        FOREIGN KEY (tech_stack_id)
            REFERENCES tech_stacks (id);
