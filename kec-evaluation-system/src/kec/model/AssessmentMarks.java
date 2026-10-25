package kec.model;

/**
 * Model representing phased assessment marks for an enrollment:
 * Phase 1: CIA Test 1 (0-100) and CIA Test 2 (0-100)
 * Phase 2: Assignments / Quizzes / Lab Evaluations (0-100)
 * Phase 3: End Semester Examination (ESE) Marks (0-100) and Absence status
 */
public class AssessmentMarks {
    private int markId;
    private int enrollmentId;
    private Double ciaTest1;
    private Double ciaTest2;
    private Double assignmentMarks;
    private Double eseMarks;
    private boolean isAbsent;

    public AssessmentMarks() {}

    public AssessmentMarks(int markId, int enrollmentId, Double ciaTest1, Double ciaTest2,
                           Double assignmentMarks, Double eseMarks, boolean isAbsent) {
        this.markId = markId;
        this.enrollmentId = enrollmentId;
        this.ciaTest1 = ciaTest1;
        this.ciaTest2 = ciaTest2;
        this.assignmentMarks = assignmentMarks;
        this.eseMarks = eseMarks;
        this.isAbsent = isAbsent;
    }

    public boolean hasPhase1() {
        return ciaTest1 != null && ciaTest2 != null;
    }

    public boolean hasPhase2() {
        return assignmentMarks != null;
    }

    public boolean hasPhase3() {
        return isAbsent || eseMarks != null;
    }

    public int getMarkId() { return markId; }
    public void setMarkId(int markId) { this.markId = markId; }

    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    public Double getCiaTest1() { return ciaTest1; }
    public void setCiaTest1(Double ciaTest1) { this.ciaTest1 = ciaTest1; }

    public Double getCiaTest2() { return ciaTest2; }
    public void setCiaTest2(Double ciaTest2) { this.ciaTest2 = ciaTest2; }

    public Double getAssignmentMarks() { return assignmentMarks; }
    public void setAssignmentMarks(Double assignmentMarks) { this.assignmentMarks = assignmentMarks; }

    public Double getEseMarks() { return eseMarks; }
    public void setEseMarks(Double eseMarks) { this.eseMarks = eseMarks; }

    public boolean isAbsent() { return isAbsent; }
    public void setAbsent(boolean absent) { isAbsent = absent; }

    @Override
    public String toString() {
        return String.format("Marks [CIA1: %s, CIA2: %s, Assign: %s, ESE: %s, Absent: %b]",
                ciaTest1 != null ? String.format("%.1f", ciaTest1) : "N/A",
                ciaTest2 != null ? String.format("%.1f", ciaTest2) : "N/A",
                assignmentMarks != null ? String.format("%.1f", assignmentMarks) : "N/A",
                eseMarks != null ? String.format("%.1f", eseMarks) : (isAbsent ? "AB" : "N/A"),
                isAbsent);
    }
}
