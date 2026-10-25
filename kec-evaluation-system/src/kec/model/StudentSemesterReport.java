package kec.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Formal Semester Grade Report for a Student under KEC Regulation 2024.
 * Computes SGPA and Equivalent Percentage according to R2024 guidelines:
 * - SGPA = Sum(Course Credits * Grade Points Earned) / Sum(Total Enrolled Credits)
 * - Equivalent Percentage = SGPA * 10
 */
public class StudentSemesterReport {
    private Student student;
    private int semester;
    private List<CourseEvaluationResult> courseResults = new ArrayList<>();

    private double totalEnrolledCredits;
    private double totalEarnedCredits;
    private double totalGradePointsEarned;
    private double sgpa;
    private double equivalentPercentage;
    private String overallResult; // "PASS", "RA (RE-APPEARANCE)", "SA (ATTENDANCE SHORTAGE)"

    public StudentSemesterReport() {}

    public StudentSemesterReport(Student student, int semester) {
        this.student = student;
        this.semester = semester;
    }

    public void addCourseResult(CourseEvaluationResult result) {
        courseResults.add(result);
        recalculate();
    }

    public void recalculate() {
        totalEnrolledCredits = 0.0;
        totalEarnedCredits = 0.0;
        totalGradePointsEarned = 0.0;
        boolean hasFailures = false;
        boolean hasSA = false;
        boolean hasIncomplete = false;

        for (CourseEvaluationResult r : courseResults) {
            totalEnrolledCredits += r.getCredits();
            totalGradePointsEarned += r.getCreditPoints();
            totalEarnedCredits += r.getEarnedCredits();

            if ("SA".equals(r.getLetterGrade())) {
                hasSA = true;
            } else if ("RA".equals(r.getLetterGrade()) || "AB".equals(r.getLetterGrade())) {
                hasFailures = true;
            } else if ("INCOMPLETE".equals(r.getStatus())) {
                hasIncomplete = true;
            }
        }

        if (totalEnrolledCredits > 0.0) {
            this.sgpa = Math.round((totalGradePointsEarned / totalEnrolledCredits) * 100.0) / 100.0;
            this.equivalentPercentage = Math.round((this.sgpa * 10.0) * 100.0) / 100.0;
        } else {
            this.sgpa = 0.0;
            this.equivalentPercentage = 0.0;
        }

        if (hasIncomplete) {
            this.overallResult = "INCOMPLETE (ASSESSMENTS PENDING)";
        } else if (hasSA) {
            this.overallResult = "FAIL (SHORTAGE OF ATTENDANCE)";
        } else if (hasFailures) {
            this.overallResult = "FAIL (RE-APPEARANCE REQUIRED)";
        } else {
            this.overallResult = "PASS (ALL COURSES CLEARED)";
        }
    }

    // Getters and Setters
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public List<CourseEvaluationResult> getCourseResults() { return courseResults; }
    public void setCourseResults(List<CourseEvaluationResult> courseResults) {
        this.courseResults = courseResults != null ? courseResults : new ArrayList<>();
        recalculate();
    }

    public double getTotalEnrolledCredits() { return totalEnrolledCredits; }
    public double getTotalEarnedCredits() { return totalEarnedCredits; }
    public double getTotalGradePointsEarned() { return totalGradePointsEarned; }
    public double getSgpa() { return sgpa; }
    public double getEquivalentPercentage() { return equivalentPercentage; }
    public String getOverallResult() { return overallResult; }
}
