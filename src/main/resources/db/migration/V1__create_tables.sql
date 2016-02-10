create table fruit (
      id int auto_increment not null,
      name varchar not null,
      juiciness int not null
    );

create table smoothy (
      id int auto_increment not null,
      description varchar not null
    );

create table ingredients (
      smoothy_id int not null,
      fruit_id int not null
    );