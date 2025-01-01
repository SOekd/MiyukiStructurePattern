package app.miyuki.miyukistructurepattern.util.math;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtils {

    public int roundToNearestMultiple(int value, int multiplier) {
        if (multiplier <= 0) {
            throw new IllegalArgumentException("Multiplier must be positive");
        }
        return Math.round((float) value / multiplier) * multiplier;
    }

}
