-- Noti Service 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS notidb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 권한 설정
CREATE USER IF NOT EXISTS 'noti'@'%' IDENTIFIED BY 'noti1234';
GRANT ALL PRIVILEGES ON notidb.* TO 'noti'@'%';
FLUSH PRIVILEGES;
