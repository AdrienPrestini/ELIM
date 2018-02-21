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


/*--
-- Structure de la table 'motCherche'
--

CREATE TABLE IF NOT EXISTS motCherche (
	imei varchar(255) NOT NULL,
	motCherche varchar(255) NOT NULL,
	nombreDeFois DATE COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idRecherche)






--
-- Structure de la table 'outilAnomalie'
--

CREATE TABLE IF NOT EXISTS outilManipulation (
	idManipulation int(10) COLLATE utf8_bin NOT NULL,
	idOutil int(10) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idManipulation, idOutil),
	KEY idManipulation (idManipulation),
	KEY idOutil (idOutil)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

















--
-- Structure de la table 'role'
--

CREATE TABLE IF NOT EXISTS role (
	idRole int(10) NOT NULL AUTO_INCREMENT,
	nom varchar(255) NOT NULL,
	PRIMARY KEY (idRole)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'hierarchieRole'
--

CREATE TABLE IF NOT EXISTS hierarchieRole (
	idSuperieur int(10) NOT NULL AUTO_INCREMENT,
	idInferieur int(10) NOT NULL,
	PRIMARY KEY (idSuperieur, idInferieur),
	KEY idSuperieur (idSuperieur),
	KEY idInferieur (idInferieur)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'service'
--

CREATE TABLE IF NOT EXISTS service (
	idService int(10) NOT NULL AUTO_INCREMENT,
	nom varchar(255) COLLATE utf8_bin NOT NULL,
	idSite int(10) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idService),
	KEY idSite (idSite)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'atelier'
--

CREATE TABLE IF NOT EXISTS atelier (
	idAtelier int(10) NOT NULL AUTO_INCREMENT,
	nom varchar(255) COLLATE utf8_bin NOT NULL,
	idService int(10) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idAtelier),
	KEY idService (idService)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'tube'
--

CREATE TABLE IF NOT EXISTS tube (
	idTube int(10) NOT NULL AUTO_INCREMENT,
	label varchar(255) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idTube)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'tubePossibleOutil'
--

CREATE TABLE IF NOT EXISTS tubePossibleOutil (
	idRefOutil int(10) COLLATE utf8_bin NOT NULL,
	idTube int(10) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idRefOutil, idTube),
	KEY idRefOutil (idRefOutil),
	KEY idTube (idTube)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'materiaux'
--

CREATE TABLE IF NOT EXISTS materiaux (
	idMateriaux int(10) NOT NULL AUTO_INCREMENT,
	label varchar(255) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idMateriaux)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'materiauxOutil'
--

CREATE TABLE IF NOT EXISTS materiauxOutil (
	idRefOutil int(10) COLLATE utf8_bin NOT NULL,
	idMateriaux int(10) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idRefOutil, idMateriaux),
	KEY idRefOutil (idRefOutil),
	KEY idMateriaux (idMateriaux)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'referenceArticle'
--

CREATE TABLE IF NOT EXISTS referenceArticle (
	numArticle int(10) COLLATE utf8_bin NOT NULL,
	label varchar(255) COLLATE utf8_bin NOT NULL,
	designationSAP varchar(40) COLLATE utf8_bin NOT NULL,
	stockActuel int(10) COLLATE utf8_bin NOT NULL,
	stockMinimal int(10) COLLATE utf8_bin NOT NULL,
	nombrePosition int(10) COLLATE utf8_bin NOT NULL,
	postInstruction varchar(255) COLLATE utf8_bin NOT NULL,
	freqMaintenanceJour int(10) COLLATE utf8_bin NOT NULL,
	freqMaintenanceUtilisation int(10) COLLATE utf8_bin NOT NULL,
	prix int(10) COLLATE utf8_bin NOT NULL,
	description varchar(255) COLLATE utf8_bin,
	PRIMARY KEY (numArticle)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'utilisateur'
--

CREATE TABLE IF NOT EXISTS utilisateur (
	matricule int(6) COLLATE utf8_bin NOT NULL,
	nom varchar(40) COLLATE utf8_bin NOT NULL,
	prenom varchar(40) COLLATE utf8_bin NOT NULL,
	civilite varchar(10) COLLATE utf8_bin NOT NULL,
	tgi varchar(8) COLLATE utf8_bin NOT NULL,
	password varchar(50) COLLATE utf8_bin NOT NULL,
	telephone varchar(40) COLLATE utf8_bin NOT NULL,
	email varchar(100) COLLATE utf8_bin NOT NULL,
	pointure int(5) COLLATE utf8_bin NOT NULL,
	tailleBlouse varchar(10) COLLATE utf8_bin NOT NULL,
	dateNaissance DATE COLLATE utf8_bin NOT NULL,
	idRole int(10) COLLATE utf8_bin NOT NULL,
	idAtelier int(10) COLLATE utf8_bin NOT NULL,
	actif ENUM('non', 'oui') NOT NULL DEFAULT 'non',
	PRIMARY KEY (matricule),
	UNIQUE KEY email (email),
	KEY idAtelier (idAtelier),
	KEY idRole (idRole)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'emplacement'
--

CREATE TABLE IF NOT EXISTS emplacement (
	idEmplacement int(10) NOT NULL AUTO_INCREMENT,
	idAtelier int(10) COLLATE utf8_bin NOT NULL,
	idEmplacementParent int(10) COLLATE utf8_bin,
	description varchar(255) COLLATE utf8_bin NOT NULL,
	niveau int(1) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idEmplacement),
	KEY idAtelier (idAtelier),
	KEY idEmplacementParent (idEmplacementParent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'emprunt'
--

CREATE TABLE IF NOT EXISTS emprunt (
	idEmprunt int(10) NOT NULL AUTO_INCREMENT,
	idUtilisateur int(6) COLLATE utf8_bin NOT NULL,
	numOf int(10) COLLATE utf8_bin NOT NULL,
	dateEmprunt DATE COLLATE utf8_bin NOT NULL,
	dateRetour DATE COLLATE utf8_bin,
	idEmplacementRetour int(10) COLLATE utf8_bin,
	remarque varchar(255) COLLATE utf8_bin,
	typeEmprunt varchar(50) COLLATE utf8_bin NOT NULL,
	statut varchar(50) COLLATE utf8_bin NOT NULL,
	idUtilisateurRestituteur int(10) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idEmprunt),
	KEY idUtilisateur (idUtilisateur),
	KEY numOf (numOf),
	KEY idEmplacementRetour (idEmplacementRetour),
	KEY idUtilisateurRestituteur (idUtilisateurRestituteur)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'outilEmprunte'
--

CREATE TABLE IF NOT EXISTS outilEmprunte (
	idEmprunt int(10) COLLATE utf8_bin NOT NULL,
	idOutil int(10) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idEmprunt, idOutil),
	KEY idEmprunt (idEmprunt),
	KEY idOutil (idOutil)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'anomalie'
--

CREATE TABLE IF NOT EXISTS anomalie (
	idAnomalie int(10) NOT NULL AUTO_INCREMENT,
	idUtilisateur int(6) COLLATE utf8_bin NOT NULL,
	dateDeclaration DATE COLLATE utf8_bin NOT NULL,
	description varchar(255) COLLATE utf8_bin NOT NULL,
	typeAnomalie varchar(50) COLLATE utf8_bin NOT NULL,
	statut varchar(50) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idAnomalie),
	KEY idUtilisateur (idUtilisateur)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'outilAnomalie'
--

CREATE TABLE IF NOT EXISTS outilAnomalie (
	idAnomalie int(10) COLLATE utf8_bin NOT NULL,
	idOutil int(10) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idAnomalie, idOutil),
	KEY idAnomalie (idAnomalie),
	KEY idOutil (idOutil)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'manipulation'
--

CREATE TABLE IF NOT EXISTS manipulation (
	idManipulation int(10) NOT NULL AUTO_INCREMENT,
	idUtilisateur int(6) COLLATE utf8_bin NOT NULL,
	dateManipulation DATE COLLATE utf8_bin NOT NULL,
	description varchar(255) COLLATE utf8_bin NOT NULL,
	typeManipulation varchar(50) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idManipulation),
	KEY idUtilisateur (idUtilisateur)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Structure de la table 'outilAnomalie'
--

CREATE TABLE IF NOT EXISTS outilManipulation (
	idManipulation int(10) COLLATE utf8_bin NOT NULL,
	idOutil int(10) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idManipulation, idOutil),
	KEY idManipulation (idManipulation),
	KEY idOutil (idOutil)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'reservation'
--

CREATE TABLE IF NOT EXISTS reservation (
	idReservation int(10) NOT NULL AUTO_INCREMENT,
	idUtilisateur int(6) COLLATE utf8_bin NOT NULL,
	dateReservation DATE COLLATE utf8_bin NOT NULL,
	motif varchar(255) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idReservation),
	KEY idUtilisateur (idUtilisateur)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'outilReserve'
--

CREATE TABLE IF NOT EXISTS outilReserve (
	idReservation int(10) COLLATE utf8_bin NOT NULL,
	idOutil int(10) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idReservation, idOutil),
	KEY idReservation (idReservation),
	KEY idOutil (idOutil)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Structure de la table 'outil'
--

CREATE TABLE IF NOT EXISTS outil (
	idOutil int(10) NOT NULL AUTO_INCREMENT,
	numArticle int(10) COLLATE utf8_bin NOT NULL,
	numSerie varchar(10) COLLATE utf8_bin NOT NULL,
	nbUtilisation int(10) COLLATE utf8_bin NOT NULL,
	nbJour int(10) COLLATE utf8_bin NOT NULL,
	idEmplacement int(10) COLLATE utf8_bin NOT NULL,
	statut varchar(50) COLLATE utf8_bin NOT NULL,
	idAtelier int(10) COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (idOutil),
	KEY numArticle (numArticle),
	KEY idEmplacement (idEmplacement),
	KEY idAtelier (idAtelier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
--
-- Structure de la table 'of'
--

CREATE TABLE IF NOT EXISTS of (
	numOf int(10) COLLATE utf8_bin NOT NULL,
	description varchar(255) COLLATE utf8_bin NOT NULL,
	dateFinPrevu DATE COLLATE utf8_bin NOT NULL,
	PRIMARY KEY (numOf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- --------------------------------------------------------


--
-- Contraintes pour les tables exportées
--

--
-- Contraintes pour la table 'service'
ALTER TABLE service
  ADD CONSTRAINT service_ibfk_1 FOREIGN KEY (idSite) REFERENCES site (idSite);


--
-- Contraintes pour la table 'atelier'
ALTER TABLE atelier
  ADD CONSTRAINT atelier_ibfk_1 FOREIGN KEY (idService) REFERENCES service (idService);


--
-- Contraintes pour la table 'hierarchieRole'
ALTER TABLE hierarchieRole
  ADD CONSTRAINT hierarchieRole_ibfk_1 FOREIGN KEY (idSuperieur) REFERENCES role (idRole),
  ADD CONSTRAINT hierarchieRole_ibfk_2 FOREIGN KEY (idInferieur) REFERENCES role (idRole);


--
-- Contraintes pour la table 'tubePossibleOutil'
ALTER TABLE tubePossibleOutil
  ADD CONSTRAINT tube_ibfk_1 FOREIGN KEY (idRefOutil) REFERENCES referenceArticle (numArticle),
  ADD CONSTRAINT tube_ibfk_2 FOREIGN KEY (idTube) REFERENCES tube (idTube);


--
-- Contraintes pour la table 'materiauxOutil'
ALTER TABLE materiauxOutil
  ADD CONSTRAINT materiaux_ibfk_1 FOREIGN KEY (idRefOutil) REFERENCES referenceArticle (numArticle),
  ADD CONSTRAINT materiaux_ibfk_2 FOREIGN KEY (idMateriaux) REFERENCES materiaux (idMateriaux);


--
-- Contraintes pour la table 'utilisateur'
ALTER TABLE utilisateur
  ADD CONSTRAINT utilisateur_ibfk_1 FOREIGN KEY (idAtelier) REFERENCES atelier (idAtelier),
  ADD CONSTRAINT utilisateur_ibfk_2 FOREIGN KEY (idRole) REFERENCES role (idRole);


--
-- Contraintes pour la table 'emplacement'
ALTER TABLE emplacement
  ADD CONSTRAINT emplacement_ibfk_1 FOREIGN KEY (idAtelier) REFERENCES atelier (idAtelier),
  ADD CONSTRAINT emplacement_ibfk_2 FOREIGN KEY (idEmplacementParent) REFERENCES emplacement (idEmplacement);


--
-- Contraintes pour la table 'emprunt'
ALTER TABLE emprunt
  ADD CONSTRAINT emprunt_ibfk_1 FOREIGN KEY (idUtilisateur) REFERENCES utilisateur (matricule),
  ADD CONSTRAINT emprunt_ibfk_2 FOREIGN KEY (numOf) REFERENCES of (numOf),
  ADD CONSTRAINT emprunt_ibfk_3 FOREIGN KEY (idEmplacementRetour) REFERENCES emplacement (idEmplacement),
  ADD CONSTRAINT emprunt_ibfk_4 FOREIGN KEY (idUtilisateurRestituteur) REFERENCES utilisateur (matricule);
  

--
-- Contraintes pour la table 'outilEmprunte'
ALTER TABLE outilEmprunte
  ADD CONSTRAINT outilEmprunte_ibfk_1 FOREIGN KEY (idEmprunt) REFERENCES emprunt (idEmprunt),
  ADD CONSTRAINT outilEmprunte_ibfk_2 FOREIGN KEY (idOutil) REFERENCES outil (idOutil);


--
-- Contraintes pour la table 'anomalie'
ALTER TABLE anomalie
  ADD CONSTRAINT anomalie_ibfk_2 FOREIGN KEY (idUtilisateur) REFERENCES utilisateur (matricule);


--
-- Contraintes pour la table 'outilAnomalie'
ALTER TABLE outilAnomalie
  ADD CONSTRAINT outilAnomalie_ibfk_1 FOREIGN KEY (idAnomalie) REFERENCES anomalie (idAnomalie),
  ADD CONSTRAINT outilAnomalie_ibfk_2 FOREIGN KEY (idOutil) REFERENCES outil (idOutil);


--
-- Contraintes pour la table 'manipulation'
ALTER TABLE manipulation
  ADD CONSTRAINT manipulation_ibfk_2 FOREIGN KEY (idUtilisateur) REFERENCES utilisateur (matricule);


--
-- Contraintes pour la table 'outilManipulation'
ALTER TABLE outilManipulation
  ADD CONSTRAINT outilManipulatione_ibfk_1 FOREIGN KEY (idManipulation) REFERENCES manipulation (idManipulation),
  ADD CONSTRAINT outilManipulation_ibfk_2 FOREIGN KEY (idOutil) REFERENCES outil (idOutil);


--
-- Contraintes pour la table 'reservation'
ALTER TABLE reservation
  ADD CONSTRAINT reservation_ibfk_1 FOREIGN KEY (idUtilisateur) REFERENCES utilisateur (matricule);


--
-- Contraintes pour la table 'outilReserve'
ALTER TABLE outilReserve
  ADD CONSTRAINT outilReserve_ibfk_1 FOREIGN KEY (idReservation) REFERENCES reservation (idReservation),
  ADD CONSTRAINT outilReserve_ibfk_2 FOREIGN KEY (idOutil) REFERENCES outil (idOutil);


-- Contraintes pour la table 'outil'
ALTER TABLE outil
  ADD CONSTRAINT outil_ibfk_1 FOREIGN KEY (numArticle) REFERENCES referenceArticle (numArticle),
  ADD CONSTRAINT outil_ibfk_2 FOREIGN KEY (idEmplacement) REFERENCES emplacement (idEmplacement),
  ADD CONSTRAINT outil_ibfk_3 FOREIGN KEY (idAtelier) REFERENCES atelier (idAtelier);


--
-- --------------------------------------------------------
--
-- Index pour les tables déchargées
--

--
-- Index pour la table `Site`
--
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
