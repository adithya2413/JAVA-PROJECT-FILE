package kec.engine;

import kec.model.*;

/**
 * Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
 * Academic Evaluation & Weightage Engine.
 *
 * Implements:
 * 1. Attendance filtering & condonation checks (<65% -> SA, 65-74.9% -> conditional, >=75% -> eligible).
 * 2. Phased Internal marks aggregation (CIA Tests + Continuous Assignments/Labs).
 * 3. Course Type Weightage:
 *    - THEORY:   CIA 40% | ESE 60% | ESE Min Threshold 27/60 (45%) | Overall Pass 50
 *    - PRACTICAL: CIA 60% | ESE 40% | ESE Min Threshold 20/40 (50%) | Overall Pass 50
 *    - EMBEDDED:  CIA 50% | ESE 50% | ESE Min Threshold 22.5/50 (45%) | Overall Pass 50
 * 4. 10-Point Grading System (O, A+, A, B+, B, RA, SA, AB).
 * 5. SGPA & Equivalent Percentage Formulas.
 */
public class EvaluationEngine {

    /**
     * Evaluates a student's enrollment and marks for a specific course.
     */
    public static CourseEvaluationResult evaluate(Enrollment enrollment, AssessmentMarks marks) {
        CourseEvaluationResult result = new CourseEvaluationResult();

        // Populate basic course and student info
        result.setRollNo(enrollment.getRollNo());
        result.setStudentName(enrollment.getStudentName());
        result.setDepartment(enrollment.getDepartment());
        result.setSemester(enrollment.getSemester());

        result.setCourseCode(enrollment.getCourseCode());
        result.setCourseTitle(enrollment.getCourseTitle());
        result.setCredits(enrollment.getCredits());
        CourseType type = enrollment.getCourseType() != null ? enrollment.getCourseType() : CourseType.THEORY;
        result.setCourseType(type);

        // 1. Attendance Evaluation
        int total = enrollment.getTotalClasses();
        int attended = enrollment.getClassesAttended();
        result.setTotalClasses(total);
        result.setClassesAttended(attended);
        double attPct = enrollment.getAttendancePercentage();
        result.setAttendancePercentage(round2(attPct));
        result.setCondonationApproved(enrollment.isCondonationApproved());

        boolean eligible = enrollment.isAttendanceEligibleForEse();
        result.setAttendanceEligible(eligible);
        result.setAttendanceStatus(enrollment.getAttendanceCategory());

        // Check if Shortage of Attendance (SA)
        if (!eligible) {
            result.setLetterGrade("SA");
            result.setGradePoint(0);
            result.setStatus("BARRED (SA)");
            result.setEarnedCredits(0.0);
            result.setCreditPoints(0.0);
            if (attPct < 65.0) {
                result.setRemarks("Shortage of Attendance (< 65%). Barred from ESE. Awarded SA.");
            } else {
                result.setRemarks("Attendance 65-74.9% without Condonation approval. Barred from ESE. Awarded SA.");
            }
            // Populate partial internal marks if available for record
            if (marks != null) {
                populateInternalMarks(result, marks, type);
            }
            return result;
        }

        // 2. Continuous Internal Assessment (CIA) Calculations
        if (marks != null) {
            populateInternalMarks(result, marks, type);
            result.setAbsent(marks.isAbsent());

            // 3. Absence in ESE
            if (marks.isAbsent()) {
                result.setLetterGrade("AB");
                result.setGradePoint(0);
                result.setStatus("ABSENT (AB)");
                result.setEarnedCredits(0.0);
                result.setCreditPoints(0.0);
                result.setRemarks("Absent in End Semester Examination. Awarded AB.");
                return result;
            }

            // 4. End Semester Examination (ESE) Evaluation
            if (marks.getEseMarks() != null) {
                double rawEse = marks.getEseMarks();
                result.setRawEse(round2(rawEse));

                double scaledEse = round2(rawEse * type.getEseWeight());
                result.setScaledEse(scaledEse);

                double totalMarks = round2((result.getScaledCia() != null ? result.getScaledCia() : 0.0) + scaledEse);
                result.setTotalMarks100(totalMarks);

                double eseMinReq = type.getEseMinThresholdScaled();
                result.setEseMinRequired(eseMinReq);

                boolean passedEse = scaledEse >= (eseMinReq - 0.001);
                boolean passedOverall = totalMarks >= type.getOverallPassMin();

                result.setPassedEseMin(passedEse);
                result.setPassedOverallMin(passedOverall);

                if (!passedEse) {
                    result.setLetterGrade("RA");
                    result.setGradePoint(0);
                    result.setStatus("FAIL (RA)");
                    result.setEarnedCredits(0.0);
                    result.setCreditPoints(0.0);
                    result.setRemarks(String.format("Failed ESE minimum threshold (Scaled: %.2f / Req: %.1f). Awarded RA.",
                            scaledEse, eseMinReq));
                } else if (!passedOverallMin(totalMarks, type)) {
                    result.setLetterGrade("RA");
                    result.setGradePoint(0);
                    result.setStatus("FAIL (RA)");
                    result.setEarnedCredits(0.0);
                    result.setCreditPoints(0.0);
                    result.setRemarks(String.format("Failed overall pass minimum (Total: %.2f / Req: %.1f). Awarded RA.",
                            totalMarks, type.getOverallPassMin()));
                } else {
                    // Passed - Determine 10-Point Grade
                    determineGrade(result, totalMarks);
                    result.setStatus("PASS");
                    result.setEarnedCredits(result.getCredits());
                    result.setCreditPoints(round2(result.getCredits() * result.getGradePoint()));
                    result.setRemarks("Successfully cleared course.");
                }
            } else {
                // ESE not yet recorded
                result.setStatus("INCOMPLETE");
                result.setLetterGrade("--");
                result.setGradePoint(0);
                result.setEarnedCredits(0.0);
                result.setCreditPoints(0.0);
                result.setRemarks("Phase 3 (ESE) marks pending.");
            }
        } else {
            result.setStatus("INCOMPLETE");
            result.setLetterGrade("--");
            result.setGradePoint(0);
            result.setEarnedCredits(0.0);
            result.setCreditPoints(0.0);
            result.setRemarks("No assessment marks recorded yet.");
        }

        return result;
    }

    private static void populateInternalMarks(CourseEvaluationResult result, AssessmentMarks marks, CourseType type) {
        result.setCiaTest1(marks.getCiaTest1());
        result.setCiaTest2(marks.getCiaTest2());

        if (marks.getCiaTest1() != null && marks.getCiaTest2() != null) {
            double avg = (marks.getCiaTest1() + marks.getCiaTest2()) / 2.0;
            result.setTestsAverage(round2(avg));
        }

        result.setAssignmentMarks(marks.getAssignmentMarks());

        // Aggregate Internal Mark on 0-100 scale:
        // Tests average and Continuous evaluations
        if (result.getTestsAverage() != null && marks.getAssignmentMarks() != null) {
            double agg;
            if (type == CourseType.THEORY) {
                agg = (result.getTestsAverage() * 0.60) + (marks.getAssignmentMarks() * 0.40);
            } else if (type == CourseType.PRACTICAL) {
                agg = (result.getTestsAverage() * 0.40) + (marks.getAssignmentMarks() * 0.60);
            } else { // EMBEDDED
                agg = (result.getTestsAverage() * 0.50) + (marks.getAssignmentMarks() * 0.50);
            }
            result.setCiaAggregate100(round2(agg));
            double scaledCia = round2(agg * type.getCiaWeight());
            result.setScaledCia(scaledCia);
        } else if (result.getTestsAverage() != null) {
            // Only Phase 1 recorded so far
            result.setCiaAggregate100(result.getTestsAverage());
            result.setScaledCia(round2(result.getTestsAverage() * type.getCiaWeight()));
        }
    }

    private static boolean passedOverallMin(double totalMarks, CourseType type) {
        return totalMarks >= (type.getOverallPassMin() - 0.001);
    }

    /**
     * KEC R2024 10-Point Grading System:
     * O  : 91 - 100 -> Grade Point 10
     * A+ : 81 - 90  -> Grade Point 9
     * A  : 71 - 80  -> Grade Point 8
     * B+ : 61 - 70  -> Grade Point 7
     * B  : 50 - 60  -> Grade Point 6
     * RA : < 50     -> Grade Point 0
     */
    private static void determineGrade(CourseEvaluationResult result, double totalMarks) {
        // Standard academic rounding to nearest integer or direct range
        long roundedMark = Math.round(totalMarks);
        if (roundedMark >= 91) {
            result.setLetterGrade("O");
            result.setGradePoint(10);
        } else if (roundedMark >= 81) {
            result.setLetterGrade("A+");
            result.setGradePoint(9);
        } else if (roundedMark >= 71) {
            result.setLetterGrade("A");
            result.setGradePoint(8);
        } else if (roundedMark >= 61) {
            result.setLetterGrade("B+");
            result.setGradePoint(7);
        } else if (roundedMark >= 50) {
            result.setLetterGrade("B");
            result.setGradePoint(6);
        } else {
            result.setLetterGrade("RA");
            result.setGradePoint(0);
        }
    }

    private static double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
