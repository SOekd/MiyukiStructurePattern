package app.miyuki.miyukistructurepattern.util.number;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.math.BigDecimal;

@UtilityClass
public class NumberHelper {

    public boolean isInteger(String amount) {
        try {
            Integer.parseInt(amount);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public boolean isLong(String amount) {
        try {
            Long.parseLong(amount);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public boolean isDouble(String amount) {
        try {
            val number = Double.parseDouble(amount);
            return !Double.isInfinite(number) && !Double.isNaN(number);
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public boolean isBigDecimal(String amount) {
        try {
            new BigDecimal(amount);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

}
