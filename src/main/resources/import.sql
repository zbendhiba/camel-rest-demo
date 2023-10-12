create sequence CoffeeOrder_SEQ start with 1;
create sequence UserID_SEQ start with 1000;

ALTER SEQUENCE CoffeeOrder_SEQ INCREMENT 1;


create table CoffeeOrder (
       id int not null,
       blend_name varchar(255),
       coffeeId bigint,
       userId varchar(255),
       primary key (id)
);

INSERT INTO CoffeeOrder(id,blend_name,coffeeId,userId) VALUES (nextval('CoffeeOrder_SEQ'),'Strong Select', 469, nextval('UserID_SEQ'));
INSERT INTO CoffeeOrder(id,blend_name,coffeeId,userId) VALUES (nextval('CoffeeOrder_SEQ'),'Green Pie', 4351, nextval('UserID_SEQ'));
INSERT INTO CoffeeOrder(id,blend_name,coffeeId,userId) VALUES (nextval('CoffeeOrder_SEQ'),'Morning America', 3036,  nextval('UserID_SEQ'));

