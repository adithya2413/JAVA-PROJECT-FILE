package kec.app;

import kec.dao.AcademicDAO;
import kec.dao.StudentDAO;
import kec.db.DBConnection;
import kec.engine.EvaluationEngine;
import kec.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Main Application Interface for Kongu Engineering College (Autonomous)
 * Regulation 2024 (R2024) Student Academic & Continuous Evaluation System.
 *
 * Provides a robust, interactive, menu-driven CLI interface for:
 * 1. Student Registration & Course Catalog
 * 2. Course Enrollment & Attendance Tracking
 * 3. Incremental / Phased Marks Entry ("Then and Now")
 * 4. KEC R2024 Evaluation Engine & Attendance Gating
 * 5. Grade Reports, Attendance Defaulter Audit, and Class Performance Summaries.
 */
public class MainApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentDAO studentDAO = new StudentDAO();
    private static final AcademicDAO academicDAO = new AcademicDAO();

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("      KONGU ENGINEERING COLLEGE (AUTONOMOUS) - REGULATION 2024 (R2024)");
        System.out.println("            STUDENT ACADEMIC & CONTINUOUS EVALUATION SYSTEM (JDBC)");
        System.out.println("===============================================================================");

        // Initialize Database
        try {
            DBConnection.initializeDatabase();
            DBConnection.seedSampleDataIfEmpty();
            System.out.println("[DB] Connected to database: " + DBConnection.getCurrentDbType());
            System.out.println("[DB] Database schema and reference tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to initialize database: " + e.getMessage());
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Enter choice (1-10): ");
            System.out.println();

            try {
                switch (choice) {
                    case "1" -> handleRegisterStudent();
                    case "2" -> handleAddCourse();
                    case "3" -> handleEnrollStudent();
                    case "4" -> handleAttendanceManagement();
                    case "5" -> handleIncrementalMarksEntry();
                    case "6" -> handleGenerateStudentMarkSheet();
                    case "7" -> handleAttendanceDefaulterReport();
                    case "8" -> handleClassPerformanceSummary();
                    case "9" -> handleDatabaseConfiguration();
                    case "10" -> {
                        System.out.println("Thank you for using KEC R2024 Academic Evaluation System. Exiting...");
                        running = false;
                    }
                    default -> System.out.println("[WARN] Invalid option. Please select 1 through 10.");
                }
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static void printMainMenu() {
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("                             MAIN NAVIGATION MENU");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println(" [1] Student Registration (Add New Student / View Students)");
        System.out.println(" [2] Course Catalog (Add New Course / View Courses)");
        System.out.println(" [3] Course Enrollment (Enroll Student in Semester Course)");
        System.out.println(" [4] Attendance Tracking (Held/Attended & Condonation Approval)");
        System.out.println(" [5] Incremental Marks Entry [Phased: CIA1/2 -> Assignment -> ESE]");
        System.out.println(" [6] Generate Student Semester Mark Sheet (SGPA & Equivalent %)");
        System.out.println(" [7] Attendance Defaulter Audit Report (< 65% SA / Condonations)");
        System.out.println(" [8] Class Performance & Result Summary (Department Statistics)");
        System.out.println(" [9] Database Configuration (Switch SQLite / MySQL or Re-Seed)");
        System.out.println(" [10] Exit");
        System.out.println("-------------------------------------------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // 1. Student Registration
    // -------------------------------------------------------------------------
    private static void handleRegisterStudent() throws SQLException {
        System.out.println("=== STUDENT REGISTRATION ===");
        System.out.println("1. Register New Student");
        System.out.println("2. View All Registered Students");
        System.out.println("3. View Students by Department");
        String sub = prompt("Select sub-option (1-3): ");

        if ("1".equals(sub)) {
            String rollNo = prompt("Enter Student Roll Number (e.g., 24ITR006): ");
            if (studentDAO.studentExists(rollNo)) {
                System.out.println("[WARN] Student with Roll Number " + rollNo + " already exists!");
                return;
            }
            String name = prompt("Enter Student Full Name: ");
            String dept = prompt("Enter Department (CSE, IT, ECE, MECH, etc.): ");
            int sem = promptInt("Enter Current Semester (1-8): ");

            Student s = new Student(rollNo, name, dept, sem);
            boolean ok = studentDAO.addStudent(s);
            if (ok) {
                System.out.println("[SUCCESS] Student registered successfully: " + s);
            }
        } else if ("2".equals(sub)) {
            List<Student> students = studentDAO.getAllStudents();
            System.out.println("\nRegistered Students (" + students.size() + "):");
            System.out.println("-------------------------------------------------------------------------------");
            for (Student s : students) {
                System.out.println(" " + s);
            }
        } else if ("3".equals(sub)) {
            String dept = prompt("Enter Department code (e.g. IT, CSE): ");
            List<Student> students = studentDAO.getStudentsByDepartment(dept);
            System.out.println("\nStudents in " + dept.toUpperCase() + " (" + students.size() + "):");
            System.out.println("-------------------------------------------------------------------------------");
            for (Student s : students) {
                System.out.println(" " + s);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 2. Course Catalog
    // -------------------------------------------------------------------------
    private static void handleAddCourse() throws SQLException {
        System.out.println("=== COURSE CATALOG MANAGEMENT ===");
        System.out.println("1. Add New Course");
        System.out.println("2. View All Courses");
        String sub = prompt("Select sub-option (1-2): ");

        if ("1".equals(sub)) {
            String code = prompt("Enter Course Code (e.g., 24ITR31): ");
            if (studentDAO.courseExists(code)) {
                System.out.println("[WARN] Course with code " + code + " already exists!");
                return;
            }
            String title = prompt("Enter Course Title: ");
            double credits = promptDouble("Enter Course Credits (e.g., 3.0, 1.5, 4.0): ");
            System.out.println("Select Course Type:");
            System.out.println("  1. THEORY   (CIA 40% | ESE 60% | ESE Min: 27/60 | Pass: 50)");
            System.out.println("  2. PRACTICAL(CIA 60% | ESE 40% | ESE Min: 20/40 | Pass: 50)");
            System.out.println("  3. EMBEDDED (CIA 50% | ESE 50% | ESE Min: 22.5/50 | Pass: 50)");
            String typeOpt = prompt("Course Type (1-3): ");
            CourseType type = switch (typeOpt) {
                case "2" -> CourseType.PRACTICAL;
                case "3" -> CourseType.EMBEDDED;
                default -> CourseType.THEORY;
            };

            Course c = new Course(code, title, credits, type);
            boolean ok = studentDAO.addCourse(c);
            if (ok) {
                System.out.println("[SUCCESS] Course added successfully: " + c);
            }
        } else if ("2".equals(sub)) {
            List<Course> courses = studentDAO.getAllCourses();
            System.out.println("\nCourse Catalog (" + courses.size() + " courses):");
            System.out.println("-------------------------------------------------------------------------------");
            for (Course c : courses) {
                System.out.println(" " + c);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 3. Course Enrollment
    // -------------------------------------------------------------------------
    private static void handleEnrollStudent() throws SQLException {
        System.out.println("=== COURSE ENROLLMENT ===");
        String rollNo = prompt("Enter Student Roll Number: ");
        Student s = studentDAO.getStudent(rollNo);
        if (s == null) {
            System.out.println("[ERROR] Student " + rollNo + " does not exist. Register student first.");
            return;
        }

        String courseCode = prompt("Enter Course Code: ");
        Course c = studentDAO.getCourse(courseCode);
        if (c == null) {
            System.out.println("[ERROR] Course " + courseCode + " does not exist. Add course first.");
            return;
        }

        int semester = promptInt("Enter Semester: ");
        int totalClasses = promptInt("Initial Total Classes Held (e.g. 60): ");
        int attended = promptInt("Initial Classes Attended (e.g. 55): ");
        boolean condonation = promptBoolean("Is Medical Condonation Approved? (y/n): ");

        int enrId = academicDAO.enrollStudent(rollNo, courseCode, semester, totalClasses, attended, condonation);
        System.out.printf("[SUCCESS] Student %s enrolled into course %s (Enrollment ID: #%d)\n", rollNo, courseCode, enrId);
    }

    // -------------------------------------------------------------------------
    // 4. Attendance Management
    // -------------------------------------------------------------------------
    private static void handleAttendanceManagement() throws SQLException {
        System.out.println("=== ATTENDANCE TRACKING & CONDONATION ===");
        int enrId = promptInt("Enter Enrollment ID: ");
        Enrollment enr = academicDAO.getEnrollment(enrId);
        if (enr == null) {
            System.out.println("[ERROR] Enrollment ID #" + enrId + " not found.");
            return;
        }

        System.out.println("\n[THEN] Current Enrollment & Attendance State:");
        System.out.println(" Student   : " + enr.getRollNo() + " - " + enr.getStudentName());
        System.out.println(" Course    : " + enr.getCourseCode() + " - " + enr.getCourseTitle());
        System.out.printf(" Attendance: %d held / %d attended (%.2f%%) -> Status: %s\n",
                enr.getTotalClasses(), enr.getClassesAttended(), enr.getAttendancePercentage(), enr.getAttendanceCategory());
        System.out.println(" Condonation Approved: " + (enr.isCondonationApproved() ? "YES" : "NO"));

        System.out.println("\n[NOW] Enter Updated Attendance Details:");
        int totalClasses = promptInt("Total Classes Held: ");
        int attended = promptInt("Classes Attended: ");
        boolean condonation = promptBoolean("Approve Medical Condonation? (y/n): ");

        academicDAO.updateAttendance(enrId, totalClasses, attended, condonation);
        Enrollment updated = academicDAO.getEnrollment(enrId);

        System.out.println("\n[UPDATED] New Attendance State:");
        System.out.printf(" Attendance: %d held / %d attended (%.2f%%) -> Category: %s\n",
                updated.getTotalClasses(), updated.getClassesAttended(), updated.getAttendancePercentage(), updated.getAttendanceCategory());
        System.out.println(" Eligible for ESE: " + (updated.isAttendanceEligibleForEse() ? "YES (Eligible)" : "NO (Barred / SA)"));
    }

    // -------------------------------------------------------------------------
    // 5. Incremental Marks Entry ("Then and Now")
    // -------------------------------------------------------------------------
    private static void handleIncrementalMarksEntry() throws SQLException {
        System.out.println("=== INCREMENTAL MARKS ENTRY (PHASED / THEN AND NOW) ===");
        System.out.println(" 1. Phase 1 (Mid-Semester)      : Record CIA Test 1 (0-100) and CIA Test 2 (0-100)");
        System.out.println(" 2. Phase 2 (End of Semester)   : Record Assignments / Quizzes / Lab Evaluations (0-100)");
        System.out.println(" 3. Phase 3 (Post Semester ESE) : Record ESE Marks (0-100) or Mark Absence (AB)");
        String phaseChoice = prompt("Select Phase (1-3): ");

        int enrId = promptInt("Enter Enrollment ID to record marks: ");
        Enrollment enr = academicDAO.getEnrollment(enrId);
        if (enr == null) {
            System.out.println("[ERROR] Enrollment ID #" + enrId + " not found.");
            return;
        }

        AssessmentMarks currentMarks = academicDAO.getAssessmentMarks(enrId);
        System.out.println("\n[CONTEXT]");
        System.out.println(" Student: " + enr.getRollNo() + " - " + enr.getStudentName());
        System.out.println(" Course : " + enr.getCourseCode() + " - " + enr.getCourseTitle() + " (" + enr.getCourseType() + ")");
        System.out.printf(" Attendance: %.1f%% (%s)\n", enr.getAttendancePercentage(), enr.getAttendanceCategory());

        System.out.println("\n[THEN - Current Recorded Marks]:");
        if (currentMarks != null) {
            System.out.println(" CIA Test 1       : " + (currentMarks.getCiaTest1() != null ? currentMarks.getCiaTest1() : "Not recorded"));
            System.out.println(" CIA Test 2       : " + (currentMarks.getCiaTest2() != null ? currentMarks.getCiaTest2() : "Not recorded"));
            System.out.println(" Assignment / Lab : " + (currentMarks.getAssignmentMarks() != null ? currentMarks.getAssignmentMarks() : "Not recorded"));
            System.out.println(" ESE Marks        : " + (currentMarks.isAbsent() ? "ABSENT (AB)" : (currentMarks.getEseMarks() != null ? currentMarks.getEseMarks() : "Not recorded")));
        } else {
            System.out.println(" No marks recorded yet.");
        }

        System.out.println("\n[NOW - Enter New Marks]:");
        if ("1".equals(phaseChoice)) {
            double cia1 = promptDouble("Enter CIA Test 1 Marks (0 - 100): ");
            double cia2 = promptDouble("Enter CIA Test 2 Marks (0 - 100): ");
            academicDAO.savePhase1Marks(enrId, cia1, cia2);
            System.out.printf("[SUCCESS] Phase 1 Marks Saved! (CIA 1: %.1f | CIA 2: %.1f | Tests Avg: %.1f)\n",
                    cia1, cia2, (cia1 + cia2) / 2.0);
        } else if ("2".equals(phaseChoice)) {
            double assign = promptDouble("Enter Assignment / Quiz / Lab Evaluation Marks (0 - 100): ");
            academicDAO.savePhase2Marks(enrId, assign);
            System.out.printf("[SUCCESS] Phase 2 Marks Saved! (Assignment/Lab: %.1f)\n", assign);
        } else if ("3".equals(phaseChoice)) {
            // Check attendance eligibility before Phase 3
            if (!enr.isAttendanceEligibleForEse()) {
                System.out.printf("[REJECTED] Student %s is BARRED from ESE due to attendance shortage (%.1f%%)!\n",
                        enr.getRollNo(), enr.getAttendancePercentage());
                System.out.println("As per KEC R2024, the student is barred and awarded 'SA'. ESE marks cannot be recorded.");
                return;
            }

            boolean isAbsent = promptBoolean("Is student ABSENT in ESE? (y/n): ");
            Double eseMarks = null;
            if (!isAbsent) {
                eseMarks = promptDouble("Enter End Semester Examination (ESE) Marks (0 - 100): ");
            }
            academicDAO.savePhase3Marks(enrId, eseMarks, isAbsent);
            System.out.println("[SUCCESS] Phase 3 Marks Saved! (ESE: " + (isAbsent ? "ABSENT (AB)" : eseMarks) + ")");
        } else {
            System.out.println("[WARN] Invalid Phase selection.");
            return;
        }

        // Show updated evaluation result for this single course
        AssessmentMarks updatedMarks = academicDAO.getAssessmentMarks(enrId);
        CourseEvaluationResult eval = EvaluationEngine.evaluate(enr, updatedMarks);
        System.out.println("\n[UPDATED EVALUATION RESULT]");
        System.out.printf(" CIA Scaled : %s | ESE Scaled : %s | Total: %s | Grade: %s (GP: %d) | Status: %s\n",
                eval.getScaledCia() != null ? String.format("%.2f", eval.getScaledCia()) : "--",
                eval.getScaledEse() != null ? String.format("%.2f", eval.getScaledEse()) : "--",
                eval.getTotalMarks100() != null ? String.format("%.2f", eval.getTotalMarks100()) : "--",
                eval.getLetterGrade(),
                eval.getGradePoint(),
                eval.getStatus());
        System.out.println(" Remarks    : " + eval.getRemarks());
    }

    // -------------------------------------------------------------------------
    // 6. Generate Student Semester Mark Sheet
    // -------------------------------------------------------------------------
    private static void handleGenerateStudentMarkSheet() throws SQLException {
        System.out.println("=== GENERATE STUDENT SEMESTER MARK SHEET ===");
        String rollNo = prompt("Enter Student Roll Number (e.g., 24ITR001): ");
        int semester = promptInt("Enter Semester (e.g., 3): ");

        StudentSemesterReport report = academicDAO.getStudentSemesterReport(rollNo, semester);
        ReportPrinter.printStudentMarkSheet(report);
    }

    // -------------------------------------------------------------------------
    // 7. Attendance Defaulter Audit Report
    // -------------------------------------------------------------------------
    private static void handleAttendanceDefaulterReport() throws SQLException {
        System.out.println("=== ATTENDANCE DEFAULTER AUDIT REPORT ===");
        String semInput = prompt("Enter Semester to filter (or press Enter for ALL): ");
        Integer semester = semInput.trim().isEmpty() ? null : Integer.parseInt(semInput.trim());

        String department = prompt("Enter Department to filter (or press Enter for ALL): ");
        if (department.trim().isEmpty()) department = null;

        List<Enrollment> defaulters = academicDAO.getAttendanceDefaulters(semester, department);
        ReportPrinter.printAttendanceDefaulterReport(defaulters, semester, department);
    }

    // -------------------------------------------------------------------------
    // 8. Class Performance & Result Summary
    // -------------------------------------------------------------------------
    private static void handleClassPerformanceSummary() throws SQLException {
        System.out.println("=== CLASS PERFORMANCE & RESULT SUMMARY ===");
        String dept = prompt("Enter Department (e.g. IT, CSE, or press Enter for ALL): ");
        int semester = promptInt("Enter Semester: ");

        ClassPerformanceSummary summary = academicDAO.getClassPerformanceSummary(dept.trim().isEmpty() ? null : dept, semester);
        ReportPrinter.printClassPerformanceSummary(summary);
    }

    // -------------------------------------------------------------------------
    // 9. Database Configuration & Utilities
    // -------------------------------------------------------------------------
    private static void handleDatabaseConfiguration() throws SQLException {
        System.out.println("=== DATABASE CONFIGURATION & UTILITIES ===");
        System.out.println(" Current Active DB: " + DBConnection.getCurrentDbType());
        System.out.println(" 1. Switch to SQLite (Embedded Portable DB - kec_academic.db)");
        System.out.println(" 2. Switch to MySQL / MariaDB (Server at localhost:3306)");
        System.out.println(" 3. Re-seed / Reset Sample KEC Academic Data");
        System.out.println(" 4. Back to Main Menu");
        String opt = prompt("Select option (1-4): ");

        if ("1".equals(opt)) {
            DBConnection.setDatabaseType(DBConnection.DatabaseType.SQLITE);
            DBConnection.initializeDatabase();
            System.out.println("[SUCCESS] Switched to SQLite mode successfully.");
        } else if ("2".equals(opt)) {
            String host = prompt("MySQL Host [localhost]: ");
            if (host.trim().isEmpty()) host = "localhost";
            int port = 3306;
            String user = prompt("MySQL Username [root]: ");
            if (user.trim().isEmpty()) user = "root";
            String pass = prompt("MySQL Password []: ");
            DBConnection.setMysqlConfig(host, port, "kec_academic_db", user, pass);
            DBConnection.setDatabaseType(DBConnection.DatabaseType.MYSQL);
            try {
                DBConnection.initializeDatabase();
                System.out.println("[SUCCESS] Switched to MySQL mode and validated schema!");
            } catch (SQLException e) {
                System.out.println("[WARN] Could not connect to MySQL: " + e.getMessage());
                System.out.println("[REVERT] Reverting back to SQLite embedded database.");
                DBConnection.setDatabaseType(DBConnection.DatabaseType.SQLITE);
            }
        } else if ("3".equals(opt)) {
            try (var conn = DBConnection.getConnection(); var stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM assessment_marks");
                stmt.executeUpdate("DELETE FROM enrollments");
                stmt.executeUpdate("DELETE FROM courses");
                stmt.executeUpdate("DELETE FROM students");
                try {
                    stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name IN ('enrollments', 'assessment_marks')");
                } catch (Exception ignored) {}
                DBConnection.seedData(conn);
                System.out.println("[SUCCESS] Sample demonstration data re-seeded successfully!");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Input Helpers
    // -------------------------------------------------------------------------
    private static String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private static int promptInt(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid integer.");
            }
        }
    }

    private static double promptDouble(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid numerical value.");
            }
        }
    }

    private static boolean promptBoolean(String message) {
        System.out.print(message);
        String input = scanner.nextLine().trim().toLowerCase();
        return input.startsWith("y") || input.equals("1") || input.equals("true");
    }
}
