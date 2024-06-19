CREATE TABLE IF NOT EXISTS users
(
    id            char(21) PRIMARY KEY DEFAULT nanoid(),
    username      varchar(50) UNIQUE NOT NULL,
    user_email    text UNIQUE,
    user_password text
);
