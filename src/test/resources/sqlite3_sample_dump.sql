PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "label" (
"attr" TEXT,
"label" TEXT
);
INSERT INTO label VALUES('recnum','Record number');
INSERT INTO label VALUES('agegrp','Age group');
INSERT INTO label VALUES('gender','Sex of respondent');
INSERT INTO label VALUES('edu','Education - highest certificate, diploma or degree completed');
INSERT INTO label VALUES('prov','Province of residence');
INSERT INTO label VALUES('cma','Census metropolitan area');
INSERT INTO label VALUES('imm_status','Immigration status');
INSERT INTO label VALUES('employed','Employment status');
CREATE TABLE IF NOT EXISTS "data" (
"recnum" TEXT,
"agegrp" TEXT,
"gender" TEXT,
"edu" TEXT,
"prov" TEXT,
"cma" TEXT,
"imm_status" TEXT,
"employed" TEXT
);
INSERT INTO data VALUES('1','35 to 39 years','Female','Master''s degree','Ontario','Toronto','Immigrants','Yes');
INSERT INTO data VALUES('2','40 to 44 years','Male','Bachelor''s degree','Quebec','Montréal','Non-immigrants','Yes');
INSERT INTO data VALUES('3','25 to 29 years','Male','Bachelor''s degree','Ontario','Ottawa – Gatineau','Not available','No');
COMMIT;
