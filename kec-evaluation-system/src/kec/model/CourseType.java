package kec.model;

/**
 * Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
 * Course Type Definition with academic weightages and passing thresholds.
 */
public enum CourseType {
    THEORY(0.40, 0.60, 27.0, 45.0, 50.0),
    PRACTICAL(0.60, 0.40, 20.0, 50.0, 50.0),
    EMBEDDED(0.50, 0.50, 22.5, 45.0, 50.0);

    private final double ciaWeight;
    private final double eseWeight;
    private final double eseMinThresholdScaled;
    private final double eseMinThresholdRaw;
    private final double overallPassMin;

    CourseType(double ciaWeight, double eseWeight, double eseMinThresholdScaled,
               double eseMinThresholdRaw, double overallPassMin) {
        this.ciaWeight = ciaWeight;
        this.eseWeight = eseWeight;
        this.eseMinThresholdScaled = eseMinThresholdScaled;
        this.eseMinThresholdRaw = eseMinThresholdRaw;
        this.overallPassMin = overallPassMin;
    }

    public double getCiaWeight() {
        return ciaWeight;
    }

    public double getEseWeight() {
        return eseWeight;
    }

    public double getEseMinThresholdScaled() {
        return eseMinThresholdScaled;
    }

    public double getEseMinThresholdRaw() {
        return eseMinThresholdRaw;
    }

    public double getOverallPassMin() {
        return overallPassMin;
    }

    public static CourseType fromString(String typeStr) {
        if (typeStr == null) return THEORY;
        String clean = typeStr.trim().toUpperCase();
        for (CourseType ct : values()) {
            if (ct.name().equals(clean)) {
                return ct;
            }
        }
        throw new IllegalArgumentException("Unknown course type: " + typeStr + ". Valid types: THEORY, PRACTICAL, EMBEDDED");
    }
}
