package kec.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Summary statistics of Class Performance for a Department and Semester.
 */
public class ClassPerformanceSummary {
    private String department;
    private int semester;
    private int totalStudents;
    private int totalCourseAppearances;
    private int totalPassed;
    private int totalRA;
    private int totalSA;
    private int totalAB;
    private double passPercentage;
    private double averageSgpa;
    private Map<String, Integer> gradeDistribution = new HashMap<>();

    public ClassPerformanceSummary() {
        gradeDistribution.put("O", 0);
        gradeDistribution.put("A+", 0);
        gradeDistribution.put("A", 0);
        gradeDistribution.put("B+", 0);
        gradeDistribution.put("B", 0);
        gradeDistribution.put("RA", 0);
        gradeDistribution.put("SA", 0);
        gradeDistribution.put("AB", 0);
    }

    public void incrementGrade(String grade) {
        if (grade != null) {
            gradeDistribution.put(grade, gradeDistribution.getOrDefault(grade, 0) + 1);
        }
    }

    // Getters and Setters
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getTotalCourseAppearances() { return totalCourseAppearances; }
    public void setTotalCourseAppearances(int totalCourseAppearances) { this.totalCourseAppearances = totalCourseAppearances; }

    public int getTotalPassed() { return totalPassed; }
    public void setTotalPassed(int totalPassed) { this.totalPassed = totalPassed; }

    public int getTotalRA() { return totalRA; }
    public void setTotalRA(int totalRA) { this.totalRA = totalRA; }

    public int getTotalSA() { return totalSA; }
    public void setTotalSA(int totalSA) { this.totalSA = totalSA; }

    public int getTotalAB() { return totalAB; }
    public void setTotalAB(int totalAB) { this.totalAB = totalAB; }

    public double getPassPercentage() { return passPercentage; }
    public void setPassPercentage(double passPercentage) { this.passPercentage = passPercentage; }

    public double getAverageSgpa() { return averageSgpa; }
    public void setAverageSgpa(double averageSgpa) { this.averageSgpa = averageSgpa; }

    public Map<String, Integer> getGradeDistribution() { return gradeDistribution; }
}
