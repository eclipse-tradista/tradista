CREATE SEQUENCE GUARD_SEQ START WITH 1 INCREMENT BY 50;
CREATE TABLE GUARD 
(ID BIGINT NOT NULL,
WORKFLOW_ID BIGINT,
NAME VARCHAR(255),
PRIMARY KEY (ID));