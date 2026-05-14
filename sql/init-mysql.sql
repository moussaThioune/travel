-- Script d'initialisation MySQL pour Voyageur
-- Exécuter AVANT de lancer le backend avec le profil MySQL

CREATE DATABASE IF NOT EXISTS voyageur_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE voyageur_db;

-- L'application créera les tables automatiquement via Hibernate (ddl-auto=update)
-- Ce script crée juste la base de données.

-- Optionnel : créer un utilisateur dédié
-- CREATE USER IF NOT EXISTS 'voyageur_user'@'localhost' IDENTIFIED BY 'VoyageurPass2024!';
-- GRANT ALL PRIVILEGES ON voyageur_db.* TO 'voyageur_user'@'localhost';
-- FLUSH PRIVILEGES;
