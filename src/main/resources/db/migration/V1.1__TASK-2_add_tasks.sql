CREATE TABLE IF NOT EXISTS TASKS(
    TASK_ID NUMBER GENERATED ALWAYS AS IDENTITY,
    TASK_NAME VARCHAR2(100) NOT NULL,
    TASK_DESCRIPTION VARCHAR2(500),
    DEADLINE TIMESTAMP NOT NULL,
    CATEGORY_ID NUMBER NOT NULL,
    CONSTRAINT TASKS_PK PRIMARY KEY(TASK_ID),
    CONSTRAINT TASK_CATEGORIES_FK FOREIGN KEY (CATEGORY_ID) REFERENCES TASK_CATEGORIES (CATEGORY_ID)
);