CREATE table PROCESSING_ORG_DEFAULTS_COLLATERAL_MANAGEMENT(
PROCESSING_ORG_ID BIGINT NOT NULL, 
QUOTE_SET_ID BIGINT,
CONSTRAINT FK_PROCESSING_ORG_DEFAULTS_COLLATERAL_MANAGEMENT_LEGAL_ENTITY_ID FOREIGN KEY (PROCESSING_ORG_ID) REFERENCES LEGAL_ENTITY(ID),
CONSTRAINT FK_PROCESSING_ORG_DEFAULTS_COLLATERAL_MANAGEMENT_QUOTE_SET_ID FOREIGN KEY (QUOTE_SET_ID) REFERENCES QUOTE_SET(ID));