package kec.model;

/**
 * Model representing a Course in the KEC Catalog.
 */
public class Course {
    private String courseCode;
    private String title;
    private double credits;
    private CourseType courseType;

    public Course() {}

    public Course(String courseCode, String title, double credits, CourseType courseType) {
        this.courseCode = courseCode != null ? courseCode.trim().toUpperCase() : null;
        this.title = title != null ? title.trim() : null;
        this.credits = credits;
        this.courseType = courseType;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode != null ? courseCode.trim().toUpperCase() : null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public void setCourseType(CourseType courseType) {
        this.courseType = courseType;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-35s | Credits: %3.1f | Type: %-9s", courseCode, title, credits, courseType);
    }
}
