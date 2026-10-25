package kec.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC Connection Manager for Kongu Engineering College Academic Evaluation System.
 * Supports dual-mode:
 * 1. Embedded SQLite (Zero-configuration, instant execution, persisted in local kec_academic.db)
 * 2. MySQL / MariaDB (Production RDBMS, port 3306)
 */
public class DBConnection {

    public enum DatabaseType {
        SQLITE,
        MYSQL
    }

    private static DatabaseType currentDbType = DatabaseType.SQLITE;

    // MySQL configuration defaults
    private static String mysqlHost = "localhost";
    private static int mysqlPort = 3306;
    private static String mysqlDatabase = "kec_academic_db";
    private static String mysqlUser = "root";
    private static String mysqlPassword = "";

    // SQLite configuration
    private static final String SQLITE_DB_FILE = "kec_academic.db";

    static {
        // Register drivers
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {}

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}
    }

    public static DatabaseType getCurrentDbType() {
        return currentDbType;
    }

    public static void setDatabaseType(DatabaseType type) {
        currentDbType = type;
    }

    public static void setMysqlConfig(String host, int port, String database, String user, String password) {
        mysqlHost = host;
        mysqlPort = port;
        mysqlDatabase = database;
        mysqlUser = user;
        mysqlPassword = password;
    }

    /**
     * Obtains a new JDBC Connection according to the currently active database mode.
     */
    public static Connection getConnection() throws SQLException {
        if (currentDbType == DatabaseType.MYSQL) {
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    mysqlHost, mysqlPort, mysqlDatabase);
            return DriverManager.getConnection(url, mysqlUser, mysqlPassword);
        } else {
            String url = "jdbc:sqlite:" + SQLITE_DB_FILE;
            Connection conn = DriverManager.getConnection(url);
            // Enable SQLite Foreign Key constraints
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        }
    }

    /**
     * Initializes database tables and constraints if they do not exist.
     */
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if (currentDbType == DatabaseType.SQLITE) {
                // Create SQLite tables
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                        "roll_no TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "department TEXT NOT NULL, " +
                        "semester INTEGER NOT NULL CHECK (semester BETWEEN 1 AND 8), " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP);");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS courses (" +
                        "course_code TEXT PRIMARY KEY, " +
                        "title TEXT NOT NULL, " +
                        "credits REAL NOT NULL CHECK (credits > 0), " +
                        "course_type TEXT NOT NULL CHECK (course_type IN ('THEORY', 'PRACTICAL', 'EMBEDDED')), " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP);");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS enrollments (" +
                        "enrollment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "roll_no TEXT NOT NULL REFERENCES students(roll_no) ON DELETE CASCADE, " +
                        "course_code TEXT NOT NULL REFERENCES courses(course_code) ON DELETE CASCADE, " +
                        "semester INTEGER NOT NULL, " +
                        "total_classes INTEGER NOT NULL DEFAULT 0 CHECK (total_classes >= 0), " +
                        "classes_attended INTEGER NOT NULL DEFAULT 0 CHECK (classes_attended >= 0), " +
                        "condonation_approved INTEGER NOT NULL DEFAULT 0, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "UNIQUE(roll_no, course_code, semester));");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS assessment_marks (" +
                        "mark_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "enrollment_id INTEGER NOT NULL UNIQUE REFERENCES enrollments(enrollment_id) ON DELETE CASCADE, " +
                        "cia_test1 REAL DEFAULT NULL, " +
                        "cia_test2 REAL DEFAULT NULL, " +
                        "assignment_marks REAL DEFAULT NULL, " +
                        "ese_marks REAL DEFAULT NULL, " +
                        "is_absent INTEGER NOT NULL DEFAULT 0, " +
                        "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP);");
            } else {
                // MySQL database tables
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                        "roll_no VARCHAR(20) NOT NULL PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "department VARCHAR(20) NOT NULL, " +
                        "semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB;");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS courses (" +
                        "course_code VARCHAR(20) NOT NULL PRIMARY KEY, " +
                        "title VARCHAR(150) NOT NULL, " +
                        "credits DOUBLE NOT NULL CHECK (credits > 0), " +
                        "course_type VARCHAR(20) NOT NULL CHECK (course_type IN ('THEORY', 'PRACTICAL', 'EMBEDDED')), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB;");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS enrollments (" +
                        "enrollment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "roll_no VARCHAR(20) NOT NULL, " +
                        "course_code VARCHAR(20) NOT NULL, " +
                        "semester INT NOT NULL, " +
                        "total_classes INT NOT NULL DEFAULT 0 CHECK (total_classes >= 0), " +
                        "classes_attended INT NOT NULL DEFAULT 0 CHECK (classes_attended >= 0), " +
                        "condonation_approved BOOLEAN NOT NULL DEFAULT FALSE, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE, " +
                        "FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE, " +
                        "UNIQUE KEY uq_student_course_sem (roll_no, course_code, semester)) ENGINE=InnoDB;");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS assessment_marks (" +
                        "mark_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "enrollment_id INT NOT NULL UNIQUE, " +
                        "cia_test1 DOUBLE DEFAULT NULL, " +
                        "cia_test2 DOUBLE DEFAULT NULL, " +
                        "assignment_marks DOUBLE DEFAULT NULL, " +
                        "ese_marks DOUBLE DEFAULT NULL, " +
                        "is_absent BOOLEAN NOT NULL DEFAULT FALSE, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE) ENGINE=InnoDB;");
            }
        }
    }

    /**
     * Seeds initial sample academic data if the students table is currently empty.
     */
    public static void seedSampleDataIfEmpty() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
            if (rs.next() && rs.getInt(1) == 0) {
                seedData(conn);
            }
        }
    }

    /**
     * Populates comprehensive sample data reflecting KEC R2024 departments, courses,
     * attendance condonations, shortage of attendance, and phased marks.
     */
    public static void seedData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Courses
            stmt.executeUpdate("INSERT INTO courses (course_code, title, credits, course_type) VALUES " +
                    "('24ITT31', 'Data Structures and Algorithms', 3.0, 'THEORY')," +
                    "('24ITP31', 'Data Structures Laboratory', 1.5, 'PRACTICAL')," +
                    "('24ITE31', 'Object Oriented Programming with Java', 4.0, 'EMBEDDED')," +
                    "('24MAT31', 'Discrete Mathematics and Linear Algebra', 4.0, 'THEORY');");

            // Students
            stmt.executeUpdate("INSERT INTO students (roll_no, name, department, semester) VALUES " +
                    "('24ITR001', 'Aadhavan K', 'IT', 3)," +
                    "('24ITR002', 'Bavithra S', 'IT', 3)," +
                    "('24ITR003', 'Chandru M', 'IT', 3)," +
                    "('24ITR004', 'Dharani R', 'IT', 3)," +
                    "('24ITR005', 'Elango P', 'IT', 3);");

            // Enrollments with explicit IDs to guarantee foreign key integrity
            // 24ITR001: High Performer (> 75% attendance)
            stmt.executeUpdate("INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, total_classes, classes_attended, condonation_approved) VALUES " +
                    "(1, '24ITR001', '24ITT31', 3, 60, 56, 0)," +
                    "(2, '24ITR001', '24ITP31', 3, 30, 29, 0)," +
                    "(3, '24ITR001', '24ITE31', 3, 75, 71, 0)," +
                    "(4, '24ITR001', '24MAT31', 3, 60, 57, 0);");

            // 24ITR002: Good Performer with Condonation Approved on Practical course (70%)
            stmt.executeUpdate("INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, total_classes, classes_attended, condonation_approved) VALUES " +
                    "(5, '24ITR002', '24ITT31', 3, 60, 48, 0)," +
                    "(6, '24ITR002', '24ITP31', 3, 30, 21, 1)," +
                    "(7, '24ITR002', '24ITE31', 3, 75, 62, 0)," +
                    "(8, '24ITR002', '24MAT31', 3, 60, 50, 0);");

            // 24ITR003: Attendance Defaulter (< 65% in 24ITT31 -> SA; Condonation unapproved in 24ITP31 -> SA)
            stmt.executeUpdate("INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, total_classes, classes_attended, condonation_approved) VALUES " +
                    "(9, '24ITR003', '24ITT31', 3, 60, 32, 0)," +
                    "(10, '24ITR003', '24ITP31', 3, 30, 20, 0)," +
                    "(11, '24ITR003', '24ITE31', 3, 75, 60, 0)," +
                    "(12, '24ITR003', '24MAT31', 3, 60, 46, 0);");

            // 24ITR004: Normal attendance, Fails ESE Minimum Threshold in Theory (ESE raw = 38 < 45 min)
            stmt.executeUpdate("INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, total_classes, classes_attended, condonation_approved) VALUES " +
                    "(13, '24ITR004', '24ITT31', 3, 60, 52, 0)," +
                    "(14, '24ITR004', '24ITP31', 3, 30, 26, 0)," +
                    "(15, '24ITR004', '24ITE31', 3, 75, 65, 0)," +
                    "(16, '24ITR004', '24MAT31', 3, 60, 51, 0);");

            // 24ITR005: Absent in ESE (AB)
            stmt.executeUpdate("INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, total_classes, classes_attended, condonation_approved) VALUES " +
                    "(17, '24ITR005', '24ITT31', 3, 60, 54, 0)," +
                    "(18, '24ITR005', '24ITP31', 3, 30, 28, 0);");

            // Assessment Marks (mapped to enrollment_ids 1 through 18)
            // 24ITR001
            stmt.executeUpdate("INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES " +
                    "(1, 92.0, 94.0, 96.0, 90.0, 0)," +
                    "(2, 95.0, 92.0, 98.0, 94.0, 0)," +
                    "(3, 88.0, 90.0, 92.0, 86.0, 0)," +
                    "(4, 90.0, 88.0, 95.0, 92.0, 0);");

            // 24ITR002
            stmt.executeUpdate("INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES " +
                    "(5, 78.0, 82.0, 85.0, 75.0, 0)," +
                    "(6, 85.0, 80.0, 88.0, 82.0, 0)," +
                    "(7, 72.0, 76.0, 80.0, 70.0, 0)," +
                    "(8, 68.0, 72.0, 75.0, 65.0, 0);");

            // 24ITR003: 1 & 2 barred due to SA; 3 & 4 cleared
            stmt.executeUpdate("INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES " +
                    "(9, 60.0, 65.0, 70.0, NULL, 0)," +
                    "(10, 65.0, 70.0, 75.0, NULL, 0)," +
                    "(11, 62.0, 58.0, 65.0, 54.0, 0)," +
                    "(12, 70.0, 68.0, 72.0, 64.0, 0);");

            // 24ITR004: Fails ESE threshold on 13 (raw=38 < 45 req)
            stmt.executeUpdate("INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES " +
                    "(13, 80.0, 82.0, 85.0, 38.0, 0)," +
                    "(14, 75.0, 78.0, 80.0, 72.0, 0)," +
                    "(15, 65.0, 68.0, 70.0, 60.0, 0)," +
                    "(16, 70.0, 75.0, 75.0, 68.0, 0);");

            // 24ITR005: Absent in ESE for 17
            stmt.executeUpdate("INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES " +
                    "(17, 75.0, 78.0, 80.0, NULL, 1)," +
                    "(18, 88.0, 90.0, 92.0, 85.0, 0);");
        }
    }
}
