package kec.dao;

import kec.db.DBConnection;
import kec.engine.EvaluationEngine;
import kec.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Academic Enrollments, Attendance Tracking,
 * Incremental Assessment Marks Entry, and Formal Performance Reports.
 */
public class AcademicDAO {

    private final StudentDAO studentDAO = new StudentDAO();

    /**
     * Enrolls a student in a course for a semester and initializes empty marks record.
     */
    public int enrollStudent(String rollNo, String courseCode, int semester,
                             int totalClasses, int attended, boolean condonationApproved) throws SQLException {
        if (!studentDAO.studentExists(rollNo)) {
            throw new IllegalArgumentException("Student with Roll Number " + rollNo + " does not exist.");
        }
        if (!studentDAO.courseExists(courseCode)) {
            throw new IllegalArgumentException("Course with Code " + courseCode + " does not exist.");
        }
        if (attended > totalClasses) {
            throw new IllegalArgumentException("Attended classes (" + attended + ") cannot exceed total classes (" + totalClasses + ").");
        }

        String insertEnrollSql = "INSERT INTO enrollments (roll_no, course_code, semester, total_classes, classes_attended, condonation_approved) VALUES (?, ?, ?, ?, ?, ?)";
        int enrollmentId = -1;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertEnrollSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rollNo.trim().toUpperCase());
            ps.setString(2, courseCode.trim().toUpperCase());
            ps.setInt(3, semester);
            ps.setInt(4, totalClasses);
            ps.setInt(5, attended);
            ps.setInt(6, condonationApproved ? 1 : 0);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    enrollmentId = rs.getInt(1);
                }
            }

            // Also ensure assessment_marks entry exists
            if (enrollmentId > 0) {
                String marksSql = "INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES (?, NULL, NULL, NULL, NULL, 0)";
                try (PreparedStatement psMarks = conn.prepareStatement(marksSql)) {
                    psMarks.setInt(1, enrollmentId);
                    psMarks.executeUpdate();
                }
            }
        }
        return enrollmentId;
    }

    /**
     * Updates attendance numbers and condonation approval.
     */
    public boolean updateAttendance(int enrollmentId, int totalClasses, int attended, boolean condonationApproved) throws SQLException {
        if (attended > totalClasses) {
            throw new IllegalArgumentException("Attended classes (" + attended + ") cannot exceed total classes (" + totalClasses + ").");
        }
        String sql = "UPDATE enrollments SET total_classes = ?, classes_attended = ?, condonation_approved = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, totalClasses);
            ps.setInt(2, attended);
            ps.setInt(3, condonationApproved ? 1 : 0);
            ps.setInt(4, enrollmentId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Approves or revokes medical condonation for an enrollment.
     */
    public boolean setCondonationApproval(int enrollmentId, boolean approved) throws SQLException {
        String sql = "UPDATE enrollments SET condonation_approved = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, approved ? 1 : 0);
            ps.setInt(2, enrollmentId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves an enrollment with student and course metadata.
     */
    public Enrollment getEnrollment(int enrollmentId) throws SQLException {
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.total_classes, e.classes_attended, " +
                "e.condonation_approved, s.name AS student_name, s.department, c.title AS course_title, c.credits, c.course_type " +
                "FROM enrollments e " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "WHERE e.enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEnrollment(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves an enrollment by student roll number, course code, and semester.
     */
    public Enrollment getEnrollmentByStudentAndCourse(String rollNo, String courseCode, int semester) throws SQLException {
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.total_classes, e.classes_attended, " +
                "e.condonation_approved, s.name AS student_name, s.department, c.title AS course_title, c.credits, c.course_type " +
                "FROM enrollments e " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "WHERE e.roll_no = ? AND e.course_code = ? AND e.semester = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo != null ? rollNo.trim().toUpperCase() : "");
            ps.setString(2, courseCode != null ? courseCode.trim().toUpperCase() : "");
            ps.setInt(3, semester);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEnrollment(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all enrollments for a specific student and semester.
     */
    public List<Enrollment> getEnrollmentsForStudent(String rollNo, int semester) throws SQLException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.total_classes, e.classes_attended, " +
                "e.condonation_approved, s.name AS student_name, s.department, c.title AS course_title, c.credits, c.course_type " +
                "FROM enrollments e " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "WHERE e.roll_no = ? AND e.semester = ? " +
                "ORDER BY e.course_code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo != null ? rollNo.trim().toUpperCase() : "");
            ps.setInt(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEnrollment(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all enrollments.
     */
    public List<Enrollment> getAllEnrollments() throws SQLException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.total_classes, e.classes_attended, " +
                "e.condonation_approved, s.name AS student_name, s.department, c.title AS course_title, c.credits, c.course_type " +
                "FROM enrollments e " +
                "JOIN students s ON e.roll_no = s.roll_no " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "ORDER BY e.semester, e.roll_no, e.course_code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapEnrollment(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves assessment marks for an enrollment.
     */
    public AssessmentMarks getAssessmentMarks(int enrollmentId) throws SQLException {
        String sql = "SELECT mark_id, enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent " +
                "FROM assessment_marks WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Double cia1 = rs.getObject("cia_test1") != null ? rs.getDouble("cia_test1") : null;
                    Double cia2 = rs.getObject("cia_test2") != null ? rs.getDouble("cia_test2") : null;
                    Double assign = rs.getObject("assignment_marks") != null ? rs.getDouble("assignment_marks") : null;
                    Double ese = rs.getObject("ese_marks") != null ? rs.getDouble("ese_marks") : null;
                    boolean absent = rs.getInt("is_absent") == 1;
                    return new AssessmentMarks(rs.getInt("mark_id"), enrollmentId, cia1, cia2, assign, ese, absent);
                }
            }
        }
        return null;
    }

    private void ensureMarksRowExists(int enrollmentId) throws SQLException {
        if (getAssessmentMarks(enrollmentId) == null) {
            String insert = "INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_marks, ese_marks, is_absent) VALUES (?, NULL, NULL, NULL, NULL, 0)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, enrollmentId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Incremental Marks Entry - Phase 1 (Mid-Semester):
     * Record CIA Test 1 (0-100) and CIA Test 2 (0-100).
     */
    public boolean savePhase1Marks(int enrollmentId, double cia1, double cia2) throws SQLException {
        if (cia1 < 0 || cia1 > 100 || cia2 < 0 || cia2 > 100) {
            throw new IllegalArgumentException("CIA Test marks must be in the range 0 to 100.");
        }
        ensureMarksRowExists(enrollmentId);

        String sql = "UPDATE assessment_marks SET cia_test1 = ?, cia_test2 = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, cia1);
            ps.setDouble(2, cia2);
            ps.setInt(3, enrollmentId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Incremental Marks Entry - Phase 2 (End of Semester - CIA):
     * Record Assignments / Quizzes / Lab Evaluations (0-100).
     */
    public boolean savePhase2Marks(int enrollmentId, double assignmentMarks) throws SQLException {
        if (assignmentMarks < 0 || assignmentMarks > 100) {
            throw new IllegalArgumentException("Assignment / Lab evaluation marks must be in the range 0 to 100.");
        }
        ensureMarksRowExists(enrollmentId);

        String sql = "UPDATE assessment_marks SET assignment_marks = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, assignmentMarks);
            ps.setInt(2, enrollmentId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Incremental Marks Entry - Phase 3 (Post Semester):
     * Record End Semester Examination (ESE) Marks (0-100) or Mark Absence (AB).
     * Enforces attendance eligibility: Barred students (SA) cannot have ESE marks entered.
     */
    public boolean savePhase3Marks(int enrollmentId, Double eseMarks, boolean isAbsent) throws SQLException {
        Enrollment enrollment = getEnrollment(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment ID " + enrollmentId + " does not exist.");
        }

        // Attendance eligibility check
        if (!enrollment.isAttendanceEligibleForEse()) {
            throw new IllegalStateException("Student " + enrollment.getRollNo() + " is BARRED from ESE due to Shortage of Attendance ("
                    + String.format("%.1f", enrollment.getAttendancePercentage()) + "%). Final grade is SA.");
        }

        if (!isAbsent) {
            if (eseMarks == null) {
                throw new IllegalArgumentException("ESE marks must be provided when student is not absent.");
            }
            if (eseMarks < 0 || eseMarks > 100) {
                throw new IllegalArgumentException("ESE marks must be in the range 0 to 100.");
            }
        }

        ensureMarksRowExists(enrollmentId);

        String sql = "UPDATE assessment_marks SET ese_marks = ?, is_absent = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (isAbsent) {
                ps.setNull(1, Types.DOUBLE);
                ps.setInt(2, 1);
            } else {
                ps.setDouble(1, eseMarks);
                ps.setInt(2, 0);
            }
            ps.setInt(3, enrollmentId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Generates formal Student Semester Mark Sheet with calculated SGPA and Percentage.
     */
    public StudentSemesterReport getStudentSemesterReport(String rollNo, int semester) throws SQLException {
        Student student = studentDAO.getStudent(rollNo);
        if (student == null) {
            throw new IllegalArgumentException("Student with Roll Number " + rollNo + " does not exist.");
        }

        StudentSemesterReport report = new StudentSemesterReport(student, semester);
        List<Enrollment> enrollments = getEnrollmentsForStudent(rollNo, semester);

        for (Enrollment enr : enrollments) {
            AssessmentMarks marks = getAssessmentMarks(enr.getEnrollmentId());
            CourseEvaluationResult evalResult = EvaluationEngine.evaluate(enr, marks);
            report.addCourseResult(evalResult);
        }

        return report;
    }

    /**
     * Identifies attendance defaulters and condonation cases.
     */
    public List<Enrollment> getAttendanceDefaulters(Integer semester, String department) throws SQLException {
        List<Enrollment> allEnrollments = getAllEnrollments();
        List<Enrollment> defaulters = new ArrayList<>();

        for (Enrollment e : allEnrollments) {
            if (semester != null && e.getSemester() != semester) continue;
            if (department != null && !department.trim().isEmpty() &&
                    !department.equalsIgnoreCase(e.getDepartment())) continue;

            double pct = e.getAttendancePercentage();
            // Defaulters: < 65% OR (65-74.9% with condonation not approved)
            if (pct < 65.0 || (pct < 75.0 && !e.isCondonationApproved())) {
                defaulters.add(e);
            }
        }
        return defaulters;
    }

    /**
     * Calculates Class Performance & Result Summary.
     */
    public ClassPerformanceSummary getClassPerformanceSummary(String department, int semester) throws SQLException {
        ClassPerformanceSummary summary = new ClassPerformanceSummary();
        summary.setDepartment(department);
        summary.setSemester(semester);

        List<Student> students;
        if (department != null && !department.trim().isEmpty()) {
            students = studentDAO.getStudentsByDepartment(department);
        } else {
            students = studentDAO.getAllStudents();
        }

        int registeredStudents = 0;
        int totalAppearances = 0;
        int totalPassed = 0;
        int totalRA = 0;
        int totalSA = 0;
        int totalAB = 0;
        double sumSgpa = 0.0;
        int sgpaStudentCount = 0;

        for (Student s : students) {
            if (s.getSemester() == semester) {
                registeredStudents++;
                StudentSemesterReport report = getStudentSemesterReport(s.getRollNo(), semester);

                if (!report.getCourseResults().isEmpty()) {
                    sumSgpa += report.getSgpa();
                    sgpaStudentCount++;

                    for (CourseEvaluationResult r : report.getCourseResults()) {
                        totalAppearances++;
                        String grade = r.getLetterGrade();
                        summary.incrementGrade(grade);

                        if ("PASS".equals(r.getStatus())) {
                            totalPassed++;
                        } else if ("SA".equals(grade)) {
                            totalSA++;
                        } else if ("AB".equals(grade)) {
                            totalAB++;
                        } else if ("RA".equals(grade)) {
                            totalRA++;
                        }
                    }
                }
            }
        }

        summary.setTotalStudents(registeredStudents);
        summary.setTotalCourseAppearances(totalAppearances);
        summary.setTotalPassed(totalPassed);
        summary.setTotalRA(totalRA);
        summary.setTotalSA(totalSA);
        summary.setTotalAB(totalAB);

        if (totalAppearances > 0) {
            summary.setPassPercentage(Math.round(((totalPassed * 100.0) / totalAppearances) * 100.0) / 100.0);
        } else {
            summary.setPassPercentage(0.0);
        }

        if (sgpaStudentCount > 0) {
            summary.setAverageSgpa(Math.round((sumSgpa / sgpaStudentCount) * 100.0) / 100.0);
        } else {
            summary.setAverageSgpa(0.0);
        }

        return summary;
    }

    private Enrollment mapEnrollment(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment(
                rs.getInt("enrollment_id"),
                rs.getString("roll_no"),
                rs.getString("course_code"),
                rs.getInt("semester"),
                rs.getInt("total_classes"),
                rs.getInt("classes_attended"),
                rs.getInt("condonation_approved") == 1
        );
        e.setStudentName(rs.getString("student_name"));
        e.setDepartment(rs.getString("department"));
        e.setCourseTitle(rs.getString("course_title"));
        e.setCredits(rs.getDouble("credits"));
        e.setCourseType(CourseType.fromString(rs.getString("course_type")));
        return e;
    }
}
