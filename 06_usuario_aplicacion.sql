-- Usuario de aplicacion con privilegio minimo (ejecutar como root despues de 01)
CREATE USER IF NOT EXISTS 'vetgest_app'@'localhost' IDENTIFIED BY 'cambiar_esta_clave';
GRANT SELECT, INSERT, UPDATE, DELETE ON vetgest_db.* TO 'vetgest_app'@'localhost';
FLUSH PRIVILEGES;
