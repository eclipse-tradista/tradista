CREATE table GCBASKET_SECURITY(
GCBASKET_ID BIGINT, 
SECURITY_ID BIGINT,
CONSTRAINT FK_GCBASKET_ID FOREIGN KEY (GCBASKET_ID) REFERENCES GCBASKET(ID),
CONSTRAINT FK_SECURITY_ID FOREIGN KEY (SECURITY_ID) REFERENCES SECURITY(PRODUCT_ID));