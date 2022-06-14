package pl.edu.icm.pdyn2.model;

public enum AgeRange {
    RANGE_0_10(0, 10),
    RANGE_10_20(10, 20),
    RANGE_20_30(20, 30),
    RANGE_30_40(30, 40),
    RANGE_40_50(40, 50),
    RANGE_50_60(50, 60),
    RANGE_60_70(60, 70),
    RANGE_70_80(70, 80),
    RANGE_80_90(80, 90),
    RANGE_90_100(90, 100),
    RANGE_100_110(100, 110),
    RANGE_110_120(110, 120),
    RANGE_120_130(120, 130);

    private final int ageFrom;
    private final int ageTo;

    AgeRange(int ageFrom, int ageTo) {
        this.ageFrom = ageFrom;
        this.ageTo = ageTo;
    }

    public static AgeRange ofRange(int ageFrom, int ageTo) {
        for (AgeRange value : values()) {
            if (value.ageFrom == ageFrom && value.ageTo == ageTo) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported age range: " + ageFrom + "-" + ageTo);
    }

    public static AgeRange of(int age) {
        for (AgeRange value : values()) {
            if (age >= value.ageFrom && age < value.ageTo) {
                return value;
            }
        }
        throw new IllegalArgumentException("Age outside of any range: " + age);
    }
}
