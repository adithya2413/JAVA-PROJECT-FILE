package kec.model;

/**
 * Model representing a registered Student in KEC.
 */
public class Student {
    private String rollNo;
    private String name;
    private String department;
    private int semester;

    public Student() {}

    public Student(String rollNo, String name, String department, int semester) {
        this.rollNo = rollNo != null ? rollNo.trim().toUpperCase() : null;
        this.name = name != null ? name.trim() : null;
        this.department = department != null ? department.trim().toUpperCase() : null;
        this.semester = semester;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo != null ? rollNo.trim().toUpperCase() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim().toUpperCase() : null;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s | Dept: %-6s | Sem: %d", rollNo, name, department, semester);
    }
}
