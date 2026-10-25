-- ============================================================================
-- Kongu Engineering College (Autonomous), Perundurai, Erode - 638060
-- Regulation 2024 (R2024) - Student Academic & Continuous Evaluation System
-- Database Schema Definition (kec_schema.sql)
-- Compatible with MySQL / MariaDB and ANSI SQL standards
-- ============================================================================

DROP DATABASE IF EXISTS kec_academic_db;
CREATE DATABASE kec_academic_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kec_academic_db;

-- ----------------------------------------------------------------------------
-- Table 1: students
-- Stores fundamental student demographic and academic profile data
-- ----------------------------------------------------------------------------
CREATE TABLE students (
    roll_no VARCHAR(20) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(20) NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 2: courses
-- Stores academic course catalog with credit weights and course delivery types
-- Course Types: THEORY, PRACTICAL, EMBEDDED
-- ----------------------------------------------------------------------------
CREATE TABLE courses (
    course_code VARCHAR(20) NOT NULL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    credits DOUBLE NOT NULL CHECK (credits > 0),
    course_type VARCHAR(20) NOT NULL CHECK (course_type IN ('THEORY', 'PRACTICAL', 'EMBEDDED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 3: enrollments
-- Manages course registration and continuous attendance records per semester
-- ----------------------------------------------------------------------------
CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    total_classes INT NOT NULL DEFAULT 0 CHECK (total_classes >= 0),
    classes_attended INT NOT NULL DEFAULT 0 CHECK (classes_attended >= 0),
    condonation_approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enroll_student FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE,
    CONSTRAINT fk_enroll_course FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
    CONSTRAINT uq_student_course_sem UNIQUE (roll_no, course_code, semester),
    CONSTRAINT chk_attendance_valid CHECK (classes_attended <= total_classes)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 4: assessment_marks
-- Tracks incremental/phased assessment marks throughout the semester:
--   Phase 1: CIA Test 1 (0-100) and CIA Test 2 (0-100)
--   Phase 2: Assignments / Quizzes / Lab Evaluations (0-100)
--   Phase 3: End Semester Examination (ESE) Marks (0-100) or Absence (AB)
-- ----------------------------------------------------------------------------
CREATE TABLE assessment_marks (
    mark_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL UNIQUE,
    cia_test1 DOUBLE DEFAULT NULL CHECK (cia_test1 IS NULL OR (cia_test1 >= 0 AND cia_test1 <= 100)),
    cia_test2 DOUBLE DEFAULT NULL CHECK (cia_test2 IS NULL OR (cia_test2 >= 0 AND cia_test2 <= 100)),
    assignment_marks DOUBLE DEFAULT NULL CHECK (assignment_marks IS NULL OR (assignment_marks >= 0 AND assignment_marks <= 100)),
    ese_marks DOUBLE DEFAULT NULL CHECK (ese_marks IS NULL OR (ese_marks >= 0 AND ese_marks <= 100)),
    is_absent BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_marks_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Indexes for performance optimization
-- ----------------------------------------------------------------------------
CREATE INDEX idx_students_dept ON students(department);
CREATE INDEX idx_enroll_roll ON enrollments(roll_no);
CREATE INDEX idx_enroll_course ON enrollments(course_code);
CREATE INDEX idx_enroll_sem ON enrollments(semester);

-- ============================================================================
-- SEED DATA: Realistic Kongu Engineering College (R2024) Department Data
-- ============================================================================

-- Courses: Information Technology & Computer Science (Semester 3)
INSERT INTO courses (course_code, title, credits, course_type) VALUES
('24ITT31', 'Data Structures and Algorithms', 3.0, 'THEORY'),
('24ITP31', 'Data Structures Laboratory', 1.5, 'PRACTICAL'),
('24ITE31', 'Object Oriented Programming with Java', 4.0, 'EMBEDDED'),
('24MAT31', 'Discrete Mathematics and Linear Algebra', 4.0, 'THEORY'),
('24ITT32', 'Database Management Systems', 3.0, 'THEORY'),
('24ITP32', 'Database Management Systems Laboratory', 1.5, 'PRACTICAL');

-- Students: 2024 Batch IT & CSE
INSERT INTO students (roll_no, name, department, semester) VALUES
('24ITR001', 'Aadhavan K', 'IT', 3),
('24ITR002', 'Bavithra S', 'IT', 3),
('24ITR003', 'Chandru M', 'IT', 3),
('24ITR004', 'Dharani R', 'IT', 3),
('24ITR005', 'Elango P', 'IT', 3),
('24CSR010', 'Gokulnath V', 'CSE', 3);

-- Enrollments for 24ITR001 (High Performer)
INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, total_classes, classes_attended, condonation_approved) VALUES
(1, '24ITR001', '24ITT31', 3, 60, 56, FALSE), -- 93.3%
(2, '24ITR001', '24ITP31', 3, 30, 29, FALSE), -- 96.7%
(3, '24ITR001', '24ITE31', 3, 75, 71, FALSE), -- 94.7%
(4, '24ITR001', '24MAT31', 3, 60, 57, FALSE); -- 95.0%

-- Enrollments for 24ITR002 (Good Performer with Condonation Approved in one course)
INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, total_classes, classes_attended, condonation_approved) VALUES
(5, '24ITR002', '24ITT31', 3, 60, 48, FALSE), -- 80.0%
(6, '24ITR002', '24ITP31', 3, 30, 21, TRUE),  -- 70.0% (Conditionally Eligible -> Condonation Approved)
(7, '24ITR002', '24ITE31', 3, 75, 62, FALSE), -- 82.7%
(8, '24ITR002', '24MAT31', 3, 60, 50, FALSE); -- 83.3%

-- Enrollments for 24ITR003 (Attendance Defaulter < 65% in one course, Condonation unapproved in another)
INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, total_classes, classes_attended, condonation_approved) VALUES
(9,  '24ITR003', '24ITT31', 3, 60, 32, FALSE), -- 53.3% (< 65% -> Defaulter / SA)
(10, '24ITR003', '24ITP31', 3, 30, 20, FALSE), -- 66.7% (65-74.9% but no condonation -> SA)
(11, '24ITR003', '24ITE31', 3, 75, 60, FALSE), -- 80.0%
(12, '24ITR003', '24MAT31', 3, 60, 46, FALSE); -- 76.7%

-- Enrollments for 24ITR004 (Marginal / Failed ESE threshold in Theory)
INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, total_classes, classes_attended, condonation_approved) VALUES
(13, '24ITR004', '24ITT31', 3, 60, 52, FALSE), -- 86.7%
(14, '24ITR004', '24ITP31', 3, 30, 26, FALSE), -- 86.7%
(15, '24ITR004', '24ITE31', 3, 75, 65, FALSE), -- 86.7%
(16, '24ITR004', '24MAT31', 3, 60, 51, FALSE); -- 85.0%

-- Enrollments for 24ITR005 (Absent in ESE in one course)
INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, total_classes, classes_attended, condonation_approved) VALUES
(17, '24ITR005', '24ITT31', 3, 60, 54, FALSE), -- 90.0%
(18, '24ITR005', '24ITP31', 3, 30, 28, FALSE); -- 93.3%

-- Assessment Marks Data:
-- Student 24ITR001: Excellent Marks across all phases
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES
(1, 92.0, 94.0, 96.0, 90.0, FALSE), -- THEORY: CIA=94.2 (37.68) + ESE=90 (54.0) = 91.68 -> O
(2, 95.0, 92.0, 98.0, 94.0, FALSE), -- PRACTICAL: CIA=95.3 (57.18) + ESE=94 (37.6) = 94.78 -> O
(3, 88.0, 90.0, 92.0, 86.0, FALSE), -- EMBEDDED: CIA=90.0 (45.0) + ESE=86 (43.0) = 88.00 -> A+
(4, 90.0, 88.0, 95.0, 92.0, FALSE); -- THEORY: CIA=91.4 (36.56) + ESE=92 (55.2) = 91.76 -> O

-- Student 24ITR002: Good marks, condonation granted on practical course
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES
(5, 78.0, 82.0, 85.0, 75.0, FALSE), -- THEORY: CIA=82.0 (32.8) + ESE=75 (45.0) = 77.8 -> A
(6, 85.0, 80.0, 88.0, 82.0, FALSE), -- PRACTICAL: CIA=84.7 (50.82) + ESE=82 (32.8) = 83.62 -> A+
(7, 72.0, 76.0, 80.0, 70.0, FALSE), -- EMBEDDED: CIA=76.0 (38.0) + ESE=70 (35.0) = 73.0 -> A
(8, 68.0, 72.0, 75.0, 65.0, FALSE); -- THEORY: CIA=72.0 (28.8) + ESE=65 (39.0) = 67.8 -> B+

-- Student 24ITR003: Attendance shortages
-- Course 1 (enr 9): attendance 53.3% -> SA (Barred from ESE, ese_marks null)
-- Course 2 (enr 10): attendance 66.7% no condonation -> SA
-- Course 3 (enr 11): normal attendance -> B grade
-- Course 4 (enr 12): normal attendance -> B+ grade
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES
(9,  60.0, 65.0, 70.0, NULL, FALSE), -- SA awarded due to attendance < 65%
(10, 65.0, 70.0, 75.0, NULL, FALSE), -- SA awarded due to unapproved condonation
(11, 62.0, 58.0, 65.0, 54.0, FALSE), -- EMBEDDED: CIA=61.6 (30.8) + ESE=54 (27.0) = 57.8 -> B
(12, 70.0, 68.0, 72.0, 64.0, FALSE); -- THEORY: CIA=70.0 (28.0) + ESE=64 (38.4) = 66.4 -> B+

-- Student 24ITR004: Fails ESE Minimum Threshold in Theory (ESE raw = 38 < 45 threshold, scaled = 22.8 < 27)
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES
(13, 80.0, 82.0, 85.0, 38.0, FALSE), -- THEORY: ESE raw 38 (<45) -> RA (Fail) despite CIA=82.6!
(14, 75.0, 78.0, 80.0, 72.0, FALSE), -- PRACTICAL: PASS -> A
(15, 65.0, 68.0, 70.0, 60.0, FALSE), -- EMBEDDED: PASS -> B+
(16, 70.0, 75.0, 75.0, 68.0, FALSE); -- THEORY: PASS -> B+

-- Student 24ITR005: Absent in ESE (AB) for 24ITT31, passed 24ITP31
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES
(17, 75.0, 78.0, 80.0, NULL, TRUE),  -- THEORY: Marked ABSENT -> Grade AB
(18, 88.0, 90.0, 92.0, 85.0, FALSE); -- PRACTICAL: PASS -> A+
