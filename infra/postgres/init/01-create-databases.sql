-- 서비스별 데이터베이스 생성 (Database per Service).
-- postgres 컨테이너 최초 기동 시 1회 실행됨 (/docker-entrypoint-initdb.d).
CREATE DATABASE member_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE delivery_db;
