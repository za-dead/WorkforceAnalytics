-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 21, 2026 at 08:35 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `workforce_analytics`
--

-- --------------------------------------------------------

--
-- Table structure for table `department`
--

CREATE TABLE `department` (
  `Department_ID` int(11) NOT NULL,
  `Department_Name` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `department`
--

INSERT INTO `department` (`Department_ID`, `Department_Name`) VALUES
(1, 'Software Engineering'),
(2, 'Artificial Intelligence'),
(3, 'Cybersecurity'),
(4, 'Data Science'),
(5, 'Cloud Computing');

-- --------------------------------------------------------

--
-- Table structure for table `employee`
--

CREATE TABLE `employee` (
  `Employee_ID` int(11) NOT NULL,
  `First_Name` varchar(50) DEFAULT NULL,
  `Last_Name` varchar(50) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `Password` varchar(100) DEFAULT NULL,
  `Role` varchar(50) DEFAULT NULL,
  `Status` varchar(50) DEFAULT NULL,
  `Department_ID` int(11) DEFAULT NULL,
  `Project_ID` int(11) DEFAULT NULL,
  `Supervisor_ID` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `employee`
--

INSERT INTO `employee` (`Employee_ID`, `First_Name`, `Last_Name`, `Email`, `Password`, `Role`, `Status`, `Department_ID`, `Project_ID`, `Supervisor_ID`) VALUES
(1, 'Tony', 'Stark', 't.stark@starkindustries.com', 'ironman', 'Engineering Manager', 'Active', 1, 101, NULL),
(2, 'Peter', 'Parker', 'p.parker@starkindustries.com', 'spiderman', 'Junior Developer', 'Active', 1, 101, 1),
(3, 'Bruce', 'Banner', 'b.banner@starkindustries.com', 'hulksmash', 'Backend Developer', 'Active', 1, 101, 1),
(4, 'Scott', 'Lang', 's.lang@starkindustries.com', 'antman', 'QA Tester', 'Active', 1, 101, 1),
(5, 'Stan', 'Lee', 's.lee@starkindustries.com', 'creator', 'AI Director', 'Active', 2, 102, NULL),
(6, 'Rocket', 'Raccoon', 'rocket@starkindustries.com', 'trashpanda', 'Machine Learning Eng', 'Active', 2, 102, 5),
(7, 'Groot', 'Tree', 'groot@starkindustries.com', 'iamgroot', 'AI Researcher', 'Active', 2, 102, 5),
(8, 'Wanda', 'Maximoff', 'w.maximoff@starkindustries.com', 'scarlet', 'Neural Network Eng', 'Active', 2, 102, 5),
(9, 'Steve', 'Rogers', 's.rogers@starkindustries.com', 'captain', 'Security Chief', 'Active', 3, 103, NULL),
(10, 'Natasha', 'Romanoff', 'n.romanoff@starkindustries.com', 'blackwidow', 'Security Tester', 'Active', 3, 103, 9),
(11, 'Bucky', 'Barnes', 'b.barnes@starkindustries.com', 'wintersoldier', 'Security Analyst', 'On Leave', 3, 103, 9),
(12, 'Nick', 'Fury', 'n.fury@starkindustries.com', 'director', 'Threat Analyst', 'Active', 3, 103, 9),
(13, 'Stephen', 'Strange', 's.strange@starkindustries.com', 'timestone', 'Lead Data Scientist', 'Active', 4, 104, NULL),
(14, 'Thanos', 'Titan', 'thanos@starkindustries.com', 'balanced', 'Data Specialist', 'Active', 4, 104, 13),
(15, 'Black', 'Panther', 'tchalla@starkindustries.com', 'wakandaforever', 'Data Analyst', 'Active', 4, 104, 13),
(16, 'Peter', 'Quill', 'p.quill@starkindustries.com', 'starlord', 'Data Engineer', 'Active', 4, 104, 13),
(17, 'Thor', 'Odinson', 'thor@starkindustries.com', 'pointbreak', 'Cloud Architect', 'Active', 5, 105, NULL),
(18, 'Loki', 'Laufeyson', 'loki@starkindustries.com', 'mischief', 'DevOps Engineer', 'Active', 5, 105, 17),
(19, 'Clint', 'Barton', 'c.barton@starkindustries.com', 'hawkeye', 'Reliability Eng', 'Active', 5, 105, 17),
(20, 'Wade', 'Wilson', 'deadpool@starkindustries.com', 'deadpool', 'Server Guardian', 'Active', 5, 105, 17),
(21, 'Sam', 'Wilson', 's.wilson@starkindustries.com', 'falcon', 'Aero Engineer', 'Active', 1, 101, 1),
(22, 'Jane', 'Foster', 'j.foster@starkindustries.com', 'lovethor', 'Cloud Infrastructure Analyst', 'Active', 5, 105, 17),
(23, 'Phil', 'Coulson', 'p.coulson@starkindustries.com', 'tahiti', 'Security Agent', 'Active', 3, 103, 9),
(24, 'Maria', 'Hill', 'm.hill@starkindustries.com', 'maria', 'Operations Commander', 'Active', 3, 103, 9),
(25, 'Ned', 'Leeds', 'n.leeds@starkindustries.com', 'multiverse', 'Data Portal Specialist', 'Active', 4, 104, 13),
(26, 'Pepper', 'Potts', 'p.potts@starkindustries.com', 'omelette', 'Operations Manager', 'Active', 1, 101, 1);

-- --------------------------------------------------------

--
-- Table structure for table `employment_history`
--

CREATE TABLE `employment_history` (
  `Department_ID` int(11) NOT NULL,
  `Employment_ID` int(11) NOT NULL,
  `History_ID` int(11) NOT NULL,
  `Transfer_Date` date DEFAULT NULL,
  `Past_Role` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `employment_history`
--

INSERT INTO `employment_history` (`Department_ID`, `Employment_ID`, `History_ID`, `Transfer_Date`, `Past_Role`) VALUES
(1, 11, 4, '2026-03-20', 'Systems Tester'),
(2, 10, 1, '2025-06-15', 'AI Researcher'),
(3, 18, 2, '2025-10-01', 'Network Security'),
(4, 2, 3, '2026-01-05', 'Data Intern');

-- --------------------------------------------------------

--
-- Table structure for table `project`
--

CREATE TABLE `project` (
  `Project_ID` int(11) NOT NULL,
  `Project_Name` varchar(100) DEFAULT NULL,
  `Start_Date` date DEFAULT NULL,
  `Department_ID` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `project`
--

INSERT INTO `project` (`Project_ID`, `Project_Name`, `Start_Date`, `Department_ID`) VALUES
(101, 'Next-Gen OS Engine', '2026-01-15', 1),
(102, 'Project Ultron Vision', '2026-02-20', 2),
(103, 'S.H.I.E.L.D. Firewall', '2026-03-10', 3),
(104, 'Multiverse Data Pipeline', '2026-04-05', 4),
(105, 'Asgardian Server Migration', '2026-05-12', 5);

-- --------------------------------------------------------

--
-- Table structure for table `task`
--

CREATE TABLE `task` (
  `Task_ID` int(11) NOT NULL,
  `Project_ID` int(11) DEFAULT NULL,
  `Task_Name` varchar(100) DEFAULT NULL,
  `Status` varchar(50) DEFAULT NULL,
  `Deadline` date DEFAULT NULL,
  `Employee_ID` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `task`
--

INSERT INTO `task` (`Task_ID`, `Project_ID`, `Task_Name`, `Status`, `Deadline`, `Employee_ID`) VALUES
(1, 101, 'Build Core Logic', 'Completed', '2026-09-01', 1),
(2, 101, 'Design Web Interface', 'In Progress', '2026-09-20', 2),
(3, 102, 'Train Neural Network', 'Pending', '2026-08-05', 5),
(4, 102, 'Integrate Vision Protocols', 'In Progress', '2026-09-15', 6),
(5, 103, 'Run Vulnerability Scan', 'Completed', '2026-07-20', 10),
(6, 103, 'Update Shield Firewalls', 'Pending', '2026-08-10', 9),
(7, 104, 'Scan The Multiverse', 'Completed', '2026-08-15', 13),
(8, 104, 'Cleanse Multiverse Data', 'In Progress', '2026-09-25', 14),
(9, 105, 'Deploy Asgard Clusters', 'Completed', '2026-08-12', 17),
(10, 105, 'Configure Bifrost API', 'Pending', '2026-08-18', 18),
(11, 101, 'Hardware Integration', 'Pending', '2026-09-15', 4),
(12, 102, 'Optimize Chaos Magic Logic', 'In Progress', '2026-09-13', 8),
(13, 103, 'Review Surveillance Feeds', 'Completed', '2026-07-15', 12),
(14, 104, 'Analyze Anomaly Logs', 'Pending', '2026-09-10', 16),
(15, 105, 'Monitor Gateway Uptime', 'Completed', '2026-08-01', 20);

-- --------------------------------------------------------

--
-- Table structure for table `time_log`
--

CREATE TABLE `time_log` (
  `Log_ID` int(11) NOT NULL,
  `Log_Date` date DEFAULT NULL,
  `Hours_Worked` decimal(5,2) DEFAULT NULL,
  `Employee_ID` int(11) DEFAULT NULL,
  `Task_ID` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `time_log`
--

INSERT INTO `time_log` (`Log_ID`, `Log_Date`, `Hours_Worked`, `Employee_ID`, `Task_ID`) VALUES
(1, '2026-03-25', 6.50, 1, 1),
(2, '2026-03-26', 7.00, 1, 1),
(3, '2026-08-15', 8.00, 2, 2),
(4, '2026-08-16', 5.50, 8, 11),
(5, '2026-08-17', 4.00, 5, 3),
(6, '2026-08-18', 8.00, 6, 4),
(7, '2026-08-19', 8.00, 16, 13),
(8, '2026-07-18', 3.00, 9, 6),
(9, '2026-07-19', 7.50, 10, 5),
(10, '2026-08-18', 6.50, 14, 7),
(11, '2026-08-19', 7.00, 18, 9),
(12, '2026-07-14', 8.00, 12, 12),
(13, '2026-07-31', 8.50, 20, 14);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `department`
--
ALTER TABLE `department`
  ADD PRIMARY KEY (`Department_ID`);

--
-- Indexes for table `employee`
--
ALTER TABLE `employee`
  ADD PRIMARY KEY (`Employee_ID`),
  ADD KEY `Department_ID` (`Department_ID`),
  ADD KEY `Project_ID` (`Project_ID`),
  ADD KEY `Supervisor_ID` (`Supervisor_ID`);

--
-- Indexes for table `employment_history`
--
ALTER TABLE `employment_history`
  ADD PRIMARY KEY (`Department_ID`,`Employment_ID`,`History_ID`),
  ADD KEY `Employment_ID` (`Employment_ID`);

--
-- Indexes for table `project`
--
ALTER TABLE `project`
  ADD PRIMARY KEY (`Project_ID`),
  ADD KEY `Department_ID` (`Department_ID`);

--
-- Indexes for table `task`
--
ALTER TABLE `task`
  ADD PRIMARY KEY (`Task_ID`),
  ADD KEY `Project_ID` (`Project_ID`),
  ADD KEY `Employee_ID` (`Employee_ID`);

--
-- Indexes for table `time_log`
--
ALTER TABLE `time_log`
  ADD PRIMARY KEY (`Log_ID`),
  ADD KEY `Task_ID` (`Task_ID`),
  ADD KEY `Employee_ID` (`Employee_ID`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `employee`
--
ALTER TABLE `employee`
  ADD CONSTRAINT `employee_ibfk_1` FOREIGN KEY (`Department_ID`) REFERENCES `department` (`Department_ID`),
  ADD CONSTRAINT `employee_ibfk_2` FOREIGN KEY (`Project_ID`) REFERENCES `project` (`Project_ID`),
  ADD CONSTRAINT `employee_ibfk_3` FOREIGN KEY (`Supervisor_ID`) REFERENCES `employee` (`Employee_ID`);

--
-- Constraints for table `employment_history`
--
ALTER TABLE `employment_history`
  ADD CONSTRAINT `employment_history_ibfk_1` FOREIGN KEY (`Department_ID`) REFERENCES `department` (`Department_ID`),
  ADD CONSTRAINT `employment_history_ibfk_2` FOREIGN KEY (`Employment_ID`) REFERENCES `employee` (`Employee_ID`);

--
-- Constraints for table `project`
--
ALTER TABLE `project`
  ADD CONSTRAINT `project_ibfk_1` FOREIGN KEY (`Department_ID`) REFERENCES `department` (`Department_ID`);

--
-- Constraints for table `task`
--
ALTER TABLE `task`
  ADD CONSTRAINT `task_ibfk_1` FOREIGN KEY (`Project_ID`) REFERENCES `project` (`Project_ID`),
  ADD CONSTRAINT `task_ibfk_2` FOREIGN KEY (`Employee_ID`) REFERENCES `employee` (`Employee_ID`);

--
-- Constraints for table `time_log`
--
ALTER TABLE `time_log`
  ADD CONSTRAINT `time_log_ibfk_1` FOREIGN KEY (`Task_ID`) REFERENCES `task` (`Task_ID`),
  ADD CONSTRAINT `time_log_ibfk_2` FOREIGN KEY (`Employee_ID`) REFERENCES `employee` (`Employee_ID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
