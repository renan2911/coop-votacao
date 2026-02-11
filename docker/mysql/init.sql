-- ==============================================================================
-- Script de inicialização do MySQL (opcional)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS coop_votacao CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE coop_votacao;

SET GLOBAL max_connections = 200;
SET GLOBAL innodb_buffer_pool_size = 268435456; -- 256MB

SELECT 'Database coop_votacao initialized successfully' AS status;
