--
-- Copyright 2002-2004 Apache Software Foundation
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

drop table SHIPPER;

drop table LINE_ITEM;

drop table CUSTOMER;

drop table PURCHASE_ORDER;

-- -----------------------------------------------------------------------
-- PURCHASE_ORDER
-- -----------------------------------------------------------------------

create table PURCHASE_ORDER
(
    PK INTEGER NOT NULL ,
    FK_CUSTOMER INTEGER NULL ,
    DATE INTEGER NULL ,
    FK_SHIPPER INTEGER NULL ,
    PRIMARY KEY (PK)
);

-- -----------------------------------------------------------------------
-- CUSTOMER
-- -----------------------------------------------------------------------

create table CUSTOMER
(
    PK INTEGER NOT NULL ,
    NAME INTEGER NULL ,
    ADDRESS INTEGER NULL ,
    AGE INTEGER NULL ,
    MOO INTEGER NULL ,
    POO INTEGER NULL ,
    PRIMARY KEY (PK)
);

-- -----------------------------------------------------------------------
-- LINE_ITEM
-- -----------------------------------------------------------------------

create table LINE_ITEM
(
    PK INTEGER NOT NULL ,
    DESCRIPTION INTEGER NULL ,
    PER_UNIT_OUNCES INTEGER NULL ,
    PRICE INTEGER NULL ,
    QUANTITY INTEGER NULL ,
    FK_PURCHASE_ORDER INTEGER NULL ,
    PRIMARY KEY (PK)
);

ALTER TABLE LINE_ITEM
    ADD CONSTRAINT LINE_ITEM_FK_1 FOREIGN KEY (FK_PURCHASE_ORDER)
    REFERENCES PURCHASE_ORDER (PK)
;

-- -----------------------------------------------------------------------
-- SHIPPER
-- -----------------------------------------------------------------------

create table SHIPPER
(
    PK INTEGER NOT NULL ,
    NAME INTEGER NULL ,
    PER_OUNCE_RATE INTEGER NULL ,
    PRIMARY KEY (PK)
);

