DROP TABLE IF EXISTS project_category;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS step;
DROP TABLE IF EXISTS material;
DROP TABLE IF EXISTS project;

CREATE TABLE project(
project_id INT AUTO_INCREMENT NOT NULL,
project_name varchar(128) NOT NULL,
estimated_hours decimal(7,2),
actual_hours decimal(7,2),
difficulty int,
notes text,
primary KEY (project_id)
);

CREATE TABLE material(
material_id int AUTO_INCREMENT NOT NULL,
project_id int NOT NULL,
material_name varchar(128) NOT NULL,
num_required int,
cost decimal(7,2),
primary key (material_id), 
foreign key (project_id)REFERENCES project(project_id) on delete cascade
);

CREATE TABLE step(
step_id int AUTO_INCREMENT NOT NULL,
project_id int NOT NULL,
step_text text NOT NULL,
step_order int NOT null,
primary key (step_id), 
foreign key (project_id)REFERENCES project(project_id) on delete cascade
);

CREATE TABLE category(
category_id int AUTO_INCREMENT NOT NULL,
category_name varchar(128) NOT null,
primary key (category_id)
);

CREATE TABLE project_category(
project_id int NOT NULL,
category_id int NOT null,
PRIMARY KEY (project_id, category_id),
FOREIGN KEY (project_id) REFERENCES project(project_id) on delete cascade,
FOREIGN KEY (category_id) REFERENCES category(category_id) on delete cascade,
unique key (project_id, category_id)
);

insert into project (project_name, estimated_hours, actual_hours, difficulty, notes) values('Testing code', 999, 1000, 3, 'This sucks, this sucks a lot.');
insert into material (project_id, material_name, num_required, cost) values(1, 'Laptop', 1, 253.70);
insert into step (Project_id, step_text, step_order) values(1, 'Write the code', 1);
INSERT INTO step (Project_id, step_text, step_order) VALUES(1, 'Test the code', 2);
INSERT INTO step (Project_id, step_text, step_order) VALUES(1, 'Cry', 3);
insert into category (category_id, category_name) values(1, 'coding');
insert into project_category (project_id, category_id) values(1,1);




