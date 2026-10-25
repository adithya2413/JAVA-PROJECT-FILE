package kec.model;

/**
 * Model representing a Student's Enrollment in a Course for a Semester,
 * along with continuous attendance tracking.
 */
public class Enrollment {
    private int enrollmentId;
    private String rollNo;
    private String courseCode;
    private int semester;
    private int totalClasses;
    private int classesAttended;
    private boolean condonationApproved;

    // Joined fields for display convenience
    private String studentName;
    private String department;
    private String courseTitle;
    private double credits;
    private CourseType courseType;

    public Enrollment() {}

    public Enrollment(int enrollmentId, String rollNo, String courseCode, int semester,
                      int totalClasses, int classesAttended, boolean condonationApproved) {
        this.enrollmentId = enrollmentId;
        this.rollNo = rollNo;
        this.courseCode = courseCode;
        this.semester = semester;
        this.totalClasses = totalClasses;
        this.classesAttended = classesAttended;
        this.condonationApproved = condonationApproved;
    }

    public double getAttendancePercentage() {
        if (totalClasses <= 0) return 100.0;
        return (classesAttended * 100.0) / totalClasses;
    }

    /**
     * KEC R2024 Attendance Rules:
     * - >= 75%: Eligible for ESE
     * - 65% to 74.9%: Conditionally eligible (Requires Medical Condonation approval)
     * - < 65%: Shortage of Attendance (SA). Barred from ESE.
     */
    public boolean isAttendanceEligibleForEse() {
        double pct = getAttendancePercentage();
        if (pct >= 75.0) {
            return true;
        } else if (pct >= 65.0) {
            return condonationApproved;
        } else {
            return false;
        }
    }

    public String getAttendanceCategory() {
        double pct = getAttendancePercentage();
        if (pct >= 75.0) {
            return "ELIGIBLE (>= 75%)";
        } else if (pct >= 65.0) {
            return condonationApproved ? "CONDONED (Approved)" : "CONDONATION REQUIRED (Pending)";
        } else {
            return "DEFAULTER (< 65% SA)";
        }
    }

    // Getters and Setters
    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }

    public int getClassesAttended() { return classesAttended; }
    public void setClassesAttended(int classesAttended) { this.classesAttended = classesAttended; }

    public boolean isCondonationApproved() { return condonationApproved; }
    public void setCondonationApproved(boolean condonationApproved) { this.condonationApproved = condonationApproved; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public double getCredits() { return credits; }
    public void setCredits(double credits) { this.credits = credits; }

    public CourseType getCourseType() { return courseType; }
    public void setCourseType(CourseType courseType) { this.courseType = courseType; }

    @Override
    public String toString() {
        return String.format("Enrollment #%d | %s (%s) -> %s (%s) | Att: %d/%d (%.1f%%) | %s",
                enrollmentId, rollNo, studentName != null ? studentName : "",
                courseCode, courseTitle != null ? courseTitle : "",
                classesAttended, totalClasses, getAttendancePercentage(), getAttendanceCategory());
    }
}
