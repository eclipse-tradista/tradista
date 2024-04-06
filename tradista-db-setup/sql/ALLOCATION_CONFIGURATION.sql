CREATE table ALLOCATION_CONFIGURATION(
ID BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY  (START WITH 1, INCREMENT BY 1), 
NAME VARCHAR(50) UNIQUE NOT NULL,
PROCESSING_ORG_ID BIGINT NOT NULL,
CONSTRAINT FK_ALLOCATION_CONFIGURATION_PROCESSING_ORG_ID FOREIGN KEY (PROCESSING_ORG_ID) REFERENCES LEGAL_ENTITY(ID));