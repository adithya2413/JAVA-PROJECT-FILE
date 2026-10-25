package kec.app;

import kec.model.*;

import java.util.List;

/**
 * Report Formatting Utility for Kongu Engineering College R2024.
 * Generates formal ASCII Grade Reports, Attendance Defaulter Reports,
 * and Class Performance Summaries.
 */
public class ReportPrinter {

    public static void printStudentMarkSheet(StudentSemesterReport report) {
        if (report == null || report.getStudent() == null) {
            System.out.println("No report available.");
            return;
        }

        Student s = report.getStudent();
        System.out.println();
        System.out.println("======================================================================================================================================");
        System.out.println("                                        KONGU ENGINEERING COLLEGE (AUTONOMOUS)");
        System.out.println("                                        PERUNDURAI, ERODE - 638060, TAMIL NADU");
        System.out.println("                                      OFFICE OF THE CONTROLLER OF EXAMINATIONS");
        System.out.println("                                 STUDENT SEMESTER GRADE REPORT - REGULATION 2024 (R2024)");
        System.out.println("======================================================================================================================================");
        System.out.printf(" Roll Number : %-15s | Name       : %-30s\n", s.getRollNo(), s.getName());
        System.out.printf(" Department  : %-15s | Semester   : %-30d\n", s.getDepartment(), report.getSemester());
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-9s | %-28s | %-9s | %-4s | %-6s | %-5s | %-5s | %-6s | %-7s | %-6s | %-6s | %-5s | %-5s | %-2s | %-12s\n",
                "Course", "Course Title", "Type", "Cred", "Att %", "CIA 1", "CIA 2", "Assign", "CIA Tot", "ESE Rw", "ESE Sc", "Total", "Grade", "GP", "Result");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");

        for (CourseEvaluationResult r : report.getCourseResults()) {
            String cia1Str = r.getCiaTest1() != null ? String.format("%5.1f", r.getCiaTest1()) : "  -- ";
            String cia2Str = r.getCiaTest2() != null ? String.format("%5.1f", r.getCiaTest2()) : "  -- ";
            String assignStr = r.getAssignmentMarks() != null ? String.format("%5.1f", r.getAssignmentMarks()) : "  -- ";
            String scaledCiaStr = r.getScaledCia() != null ? String.format("%6.2f", r.getScaledCia()) : "   --  ";

            String eseRawStr;
            String eseScaledStr;
            if (r.isAbsent()) {
                eseRawStr = "   AB ";
                eseScaledStr = "   AB ";
            } else if ("SA".equals(r.getLetterGrade())) {
                eseRawStr = "   SA ";
                eseScaledStr = "   SA ";
            } else if (r.getRawEse() != null) {
                eseRawStr = String.format("%6.1f", r.getRawEse());
                eseScaledStr = String.format("%6.2f", r.getScaledEse());
            } else {
                eseRawStr = "  --  ";
                eseScaledStr = "  --  ";
            }

            String totalStr = r.getTotalMarks100() != null ? String.format("%5.1f", r.getTotalMarks100()) : " -- ";
            String courseTitle = r.getCourseTitle();
            if (courseTitle != null && courseTitle.length() > 28) {
                courseTitle = courseTitle.substring(0, 25) + "...";
            }

            System.out.printf("%-9s | %-28s | %-9s | %4.1f | %5.1f%% | %s | %s | %s | %s | %s | %s | %s | %-5s | %2d | %-12s\n",
                    r.getCourseCode(),
                    courseTitle != null ? courseTitle : "",
                    r.getCourseType(),
                    r.getCredits(),
                    r.getAttendancePercentage(),
                    cia1Str,
                    cia2Str,
                    assignStr,
                    scaledCiaStr,
                    eseRawStr,
                    eseScaledStr,
                    totalStr,
                    r.getLetterGrade(),
                    r.getGradePoint(),
                    r.getStatus()
            );
        }

        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf(" Total Registered Credits : %-6.1f | Total Earned Credits : %-6.1f | Total Grade Points Earned : %-6.2f\n",
                report.getTotalEnrolledCredits(), report.getTotalEarnedCredits(), report.getTotalGradePointsEarned());
        System.out.printf(" SGPA                     : %-6.2f | Equivalent Percentage: %-6.2f%% | Overall Semester Result   : %-15s\n",
                report.getSgpa(), report.getEquivalentPercentage(), report.getOverallResult());
        System.out.println("======================================================================================================================================");
        System.out.println(" Evaluation Notes (KEC R2024):");
        System.out.println("   * THEORY   : CIA 40% + ESE 60% | ESE Min: 27/60 (45%) | Pass Min: 50/100");
        System.out.println("   * PRACTICAL: CIA 60% + ESE 40% | ESE Min: 20/40 (50%) | Pass Min: 50/100");
        System.out.println("   * EMBEDDED : CIA 50% + ESE 50% | ESE Min: 22.5/50 (45%) | Pass Min: 50/100");
        System.out.println("   * Grades: O(91-100)=10, A+(81-90)=9, A(71-80)=8, B+(61-70)=7, B(50-60)=6, RA/SA/AB=0 GP");
        System.out.println("======================================================================================================================================\n");
    }

    public static void printAttendanceDefaulterReport(List<Enrollment> defaulters, Integer semester, String department) {
        System.out.println();
        System.out.println("========================================================================================================================");
        System.out.println("                                      KONGU ENGINEERING COLLEGE (AUTONOMOUS)");
        System.out.println("                                  CONTINUOUS EVALUATION & ATTENDANCE DEFAULTER AUDIT");
        System.out.printf("                                  Filter: Semester: %s | Department: %s\n",
                semester != null ? semester : "ALL", department != null && !department.isEmpty() ? department : "ALL");
        System.out.println("========================================================================================================================");
        System.out.printf("%-10s | %-20s | %-8s | %-25s | %-6s | %-8s | %-7s | %-12s | %-14s\n",
                "Roll No", "Student Name", "Course", "Course Title", "Held", "Attended", "Att %", "Condonation", "Barred / Action");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");

        if (defaulters.isEmpty()) {
            System.out.println(" No attendance defaulters found. All students meet the minimum attendance threshold (>= 75% or Condoned).");
        } else {
            for (Enrollment e : defaulters) {
                String cTitle = e.getCourseTitle();
                if (cTitle != null && cTitle.length() > 25) {
                    cTitle = cTitle.substring(0, 22) + "...";
                }
                String condonation = e.isCondonationApproved() ? "APPROVED" : (e.getAttendancePercentage() >= 65.0 ? "REQUIRED" : "NOT ELIGIBLE");
                String action = e.getAttendancePercentage() < 65.0 ? "SA (Barred <65%)" : "SA (No Condonation)";

                System.out.printf("%-10s | %-20s | %-8s | %-25s | %-6d | %-8d | %5.1f%% | %-12s | %-14s\n",
                        e.getRollNo(),
                        e.getStudentName() != null ? e.getStudentName() : "",
                        e.getCourseCode(),
                        cTitle != null ? cTitle : "",
                        e.getTotalClasses(),
                        e.getClassesAttended(),
                        e.getAttendancePercentage(),
                        condonation,
                        action
                );
            }
        }
        System.out.println("========================================================================================================================");
        System.out.println(" Policy Notice: As per KEC R2024, students with Attendance < 65% are strictly barred and awarded 'SA'.");
        System.out.println("                 Students with Attendance 65% - 74.9% require formal Medical Condonation approval to appear for ESE.");
        System.out.println("========================================================================================================================\n");
    }

    public static void printClassPerformanceSummary(ClassPerformanceSummary summary) {
        System.out.println();
        System.out.println("=========================================================================================");
        System.out.println("                              KONGU ENGINEERING COLLEGE (AUTONOMOUS)");
        System.out.println("                       CLASS ACADEMIC PERFORMANCE & RESULT SUMMARY (R2024)");
        System.out.println("=========================================================================================");
        System.out.printf(" Department              : %-20s | Semester : %d\n",
                summary.getDepartment() != null && !summary.getDepartment().isEmpty() ? summary.getDepartment() : "ALL DEPARTMENTS",
                summary.getSemester());
        System.out.printf(" Total Students Evaluated: %-20d | Total Course Appearances: %d\n",
                summary.getTotalStudents(), summary.getTotalCourseAppearances());
        System.out.printf(" Total Passed Courses    : %-20d | Total Re-Appearances(RA): %d\n",
                summary.getTotalPassed(), summary.getTotalRA());
        System.out.printf(" Shortage of Att. (SA)   : %-20d | Absentees (AB)          : %d\n",
                summary.getTotalSA(), summary.getTotalAB());
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf(" OVERALL PASS PERCENTAGE : %6.2f%%\n", summary.getPassPercentage());
        System.out.printf(" CLASS AVERAGE SGPA      : %6.2f  (Equivalent Class Percentage: %.2f%%)\n",
                summary.getAverageSgpa(), summary.getAverageSgpa() * 10.0);
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println(" Letter Grade Distribution:");
        var dist = summary.getGradeDistribution();
        System.out.printf("   [O ] Outstanding (91-100) : %3d\n", dist.getOrDefault("O", 0));
        System.out.printf("   [A+] Excellent   (81-90)  : %3d\n", dist.getOrDefault("A+", 0));
        System.out.printf("   [A ] Very Good   (71-80)  : %3d\n", dist.getOrDefault("A", 0));
        System.out.printf("   [B+] Good        (61-70)  : %3d\n", dist.getOrDefault("B+", 0));
        System.out.printf("   [B ] Average     (50-60)  : %3d\n", dist.getOrDefault("B", 0));
        System.out.printf("   [RA] Re-Appear   (<50/Min): %3d\n", dist.getOrDefault("RA", 0));
        System.out.printf("   [SA] Shortage of Attend.  : %3d\n", dist.getOrDefault("SA", 0));
        System.out.printf("   [AB] Absent in ESE        : %3d\n", dist.getOrDefault("AB", 0));
        System.out.println("=========================================================================================\n");
    }
}
