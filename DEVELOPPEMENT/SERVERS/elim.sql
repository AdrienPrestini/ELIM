-- phpMyAdmin SQL Dump
-- version 4.7.0
-- https://www.phpmyadmin.net/
--
-- Hôte : localhost
-- Généré le :  mer. 28 juin 2017 à 12:50
-- Version du serveur :  10.1.22-MariaDB
-- Version de PHP :  7.1.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données :  'elim'
--

-- --------------------------------------------------------

--
-- Structure de la table 'recherche'
--

CREATE TABLE IF NOT EXISTS recherche (
	idRecherche int(10) NOT NULL AUTO_INCREMENT,
	imei varchar(255) NOT NULL,
	motCherche varchar(255) NOT NULL,
	dateRecherche date NOT NULL,
	heureRecherche time NOT NULL,
	jourSemaine int(10) NOT NULL,
	PRIMARY KEY (idRecherche)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



INSERT INTO recherche (idRecherche, imei, motCherche, dateRecherche, heureRecherche, jourSemaine) VALUES
(1, '123456', 'boulangerie', '2018-01-27', "8:35:00", 3),
(2, '123456', 'boulangerie', '2018-01-24', "8:52:34", 3),
(3, '123456', 'boulangerie', '2018-01-31', "9:09:23", 3),
(4, '123456', 'boulangerie', '2018-02-07', "8:46:26", 3),
(5, '123456', 'boulangerie', '2018-02-14', "8:31:55", 3),
(6, '123456', 'boulangerie', '2018-02-21', "9:28:36", 3),
(7, '123456', 'restaurant', '2018-02-21', "9:24:56", 3),
(8, '123456', 'restaurant', '2018-02-14', "8:59:41", 3),
(9, '123456', 'restaurant', '2018-02-07', "9:02:34", 3);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
