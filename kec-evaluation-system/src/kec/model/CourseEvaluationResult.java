package kec.model;

/**
 * Detailed evaluation outcome for a course enrollment under KEC R2024.
 */
public class CourseEvaluationResult {
    private String rollNo;
    private String studentName;
    private String department;
    private int semester;

    private String courseCode;
    private String courseTitle;
    private double credits;
    private CourseType courseType;

    // Attendance
    private int totalClasses;
    private int classesAttended;
    private double attendancePercentage;
    private boolean condonationApproved;
    private boolean attendanceEligible;
    private String attendanceStatus;

    // Raw Marks
    private Double ciaTest1;
    private Double ciaTest2;
    private Double testsAverage;
    private Double assignmentMarks;
    private Double ciaAggregate100;
    private Double scaledCia;

    private Double rawEse;
    private Double scaledEse;
    private boolean isAbsent;

    // Totals & Rules
    private Double totalMarks100;
    private double eseMinRequired;
    private boolean passedEseMin;
    private boolean passedOverallMin;

    // Final Outcome
    private String letterGrade;   // O, A+, A, B+, B, RA, SA, AB
    private int gradePoint;       // 10, 9, 8, 7, 6, 0
    private String status;        // PASS, FAIL (RA), BARRED (SA), ABSENT (AB), INCOMPLETE
    private double earnedCredits;
    private double creditPoints;  // credits * gradePoint
    private String remarks;

    public CourseEvaluationResult() {}

    // Getters and Setters
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public double getCredits() { return credits; }
    public void setCredits(double credits) { this.credits = credits; }

    public CourseType getCourseType() { return courseType; }
    public void setCourseType(CourseType courseType) { this.courseType = courseType; }

    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }

    public int getClassesAttended() { return classesAttended; }
    public void setClassesAttended(int classesAttended) { this.classesAttended = classesAttended; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public boolean isCondonationApproved() { return condonationApproved; }
    public void setCondonationApproved(boolean condonationApproved) { this.condonationApproved = condonationApproved; }

    public boolean isAttendanceEligible() { return attendanceEligible; }
    public void setAttendanceEligible(boolean attendanceEligible) { this.attendanceEligible = attendanceEligible; }

    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    public Double getCiaTest1() { return ciaTest1; }
    public void setCiaTest1(Double ciaTest1) { this.ciaTest1 = ciaTest1; }

    public Double getCiaTest2() { return ciaTest2; }
    public void setCiaTest2(Double ciaTest2) { this.ciaTest2 = ciaTest2; }

    public Double getTestsAverage() { return testsAverage; }
    public void setTestsAverage(Double testsAverage) { this.testsAverage = testsAverage; }

    public Double getAssignmentMarks() { return assignmentMarks; }
    public void setAssignmentMarks(Double assignmentMarks) { this.assignmentMarks = assignmentMarks; }

    public Double getCiaAggregate100() { return ciaAggregate100; }
    public void setCiaAggregate100(Double ciaAggregate100) { this.ciaAggregate100 = ciaAggregate100; }

    public Double getScaledCia() { return scaledCia; }
    public void setScaledCia(Double scaledCia) { this.scaledCia = scaledCia; }

    public Double getRawEse() { return rawEse; }
    public void setRawEse(Double rawEse) { this.rawEse = rawEse; }

    public Double getScaledEse() { return scaledEse; }
    public void setScaledEse(Double scaledEse) { this.scaledEse = scaledEse; }

    public boolean isAbsent() { return isAbsent; }
    public void setAbsent(boolean absent) { isAbsent = absent; }

    public Double getTotalMarks100() { return totalMarks100; }
    public void setTotalMarks100(Double totalMarks100) { this.totalMarks100 = totalMarks100; }

    public double getEseMinRequired() { return eseMinRequired; }
    public void setEseMinRequired(double eseMinRequired) { this.eseMinRequired = eseMinRequired; }

    public boolean isPassedEseMin() { return passedEseMin; }
    public void setPassedEseMin(boolean passedEseMin) { this.passedEseMin = passedEseMin; }

    public boolean isPassedOverallMin() { return passedOverallMin; }
    public void setPassedOverallMin(boolean passedOverallMin) { this.passedOverallMin = passedOverallMin; }

    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }

    public int getGradePoint() { return gradePoint; }
    public void setGradePoint(int gradePoint) { this.gradePoint = gradePoint; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getEarnedCredits() { return earnedCredits; }
    public void setEarnedCredits(double earnedCredits) { this.earnedCredits = earnedCredits; }

    public double getCreditPoints() { return creditPoints; }
    public void setCreditPoints(double creditPoints) { this.creditPoints = creditPoints; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
