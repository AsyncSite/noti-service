-- =====================================================
-- Noti Service Database Initialization Script
-- =====================================================
-- This script creates the necessary databases and users
-- for the Noti Service microservice
-- =====================================================

-- Create main database
CREATE DATABASE IF NOT EXISTS notidb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create test database for integration tests
CREATE DATABASE IF NOT EXISTS notidb_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create noti service user if not exists
CREATE USER IF NOT EXISTS 'noti'@'%' IDENTIFIED BY 'noti1234';

-- Grant privileges on main database
GRANT ALL PRIVILEGES ON notidb.* TO 'noti'@'%';

-- Grant privileges on test database
GRANT ALL PRIVILEGES ON notidb_test.* TO 'noti'@'%';

-- Apply privilege changes
FLUSH PRIVILEGES;

-- Switch to main database
USE notidb;

-- Show created databases
SHOW DATABASES LIKE 'notidb%';
SELECT User, Host FROM mysql.user WHERE User = 'noti';
