package com.qingclass.bigbay.tool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class DecimalCalculationUtils {
    public DecimalCalculationUtils() {
    }

    public static double add(double augend, double addend) {
        BigDecimal bgAugend = new BigDecimal(augend + "");
        BigDecimal bgAddend = new BigDecimal(addend + "");
        return bgAugend.add(bgAddend).doubleValue();
    }

    public static double scienToDigital(double digital) {
        return (new BigDecimal(digital)).doubleValue();
    }

    public static double subtract(double minuend, double meiosis) {
        BigDecimal bgMinuend = new BigDecimal(minuend + "");
        BigDecimal bgMeiosis = new BigDecimal(meiosis + "");
        return bgMinuend.subtract(bgMeiosis).doubleValue();
    }

    public static double multiply(double faciend, double multiplier) {
        BigDecimal bgFaciend = new BigDecimal(faciend + "");
        BigDecimal bgMultiplier = new BigDecimal(multiplier + "");
        return bgFaciend.multiply(bgMultiplier).doubleValue();
    }

    public static double multiplyScale2RoundHalfUp(double faciend, double multiplier) {
        return multiply(faciend, multiplier, 2, 4);
    }

    public static double multiply(double faciend, double multiplier, int scale, int roundingMode) {
        BigDecimal bgFaciend = new BigDecimal(faciend + "");
        BigDecimal bgMultiplier = new BigDecimal(multiplier + "");
        return divide(bgFaciend.multiply(bgMultiplier).doubleValue(), 1.0D, scale, roundingMode);
    }

    public static float divideToFloat(double dividend, double divisor) {
        double doubleValue = (new BigDecimal(dividend)).divide(new BigDecimal(divisor)).doubleValue();
        DecimalFormat df1 = new DecimalFormat("###0.00");
        return Float.valueOf(df1.format(doubleValue));
    }
    
    public static int divideToInt(double dividend, double divisor) {
        double doubleValue = (new BigDecimal(dividend)).divide(new BigDecimal(divisor)).setScale(0, RoundingMode.DOWN).doubleValue();
        DecimalFormat df1 = new DecimalFormat("###0");
        return Integer.valueOf(df1.format(doubleValue));
    }

    public static String divideIntToStr(int dividend, int divisor) {
        double doubleValue = (new BigDecimal(dividend)).divide(new BigDecimal(divisor)).doubleValue();
        DecimalFormat df1 = new DecimalFormat("###0.000000");
        return df1.format(doubleValue);
    }

    public static double divide(double dividend, double divisor, int scale, int roundingMode) {
        if (divisor == 0.0D) {
            return 0.0D;
        } else {
            BigDecimal bgDividend = new BigDecimal(dividend + "");
            BigDecimal bgDivisor = new BigDecimal(divisor + "");
            BigDecimal divide = bgDividend.divide(bgDivisor).setScale(scale, roundingMode);
            return divide.doubleValue();
        }
    }

    public static double divideNoRound(double dividend, double divisor, int scale) {
        if (divisor == 0.0D) {
            return 0.0D;
        } else {
            BigDecimal bgDividend = new BigDecimal(dividend + "");
            BigDecimal bgDivisor = new BigDecimal(divisor + "");
            BigDecimal divide = bgDividend.divide(bgDivisor).setScale(scale, 4);
            return divide.doubleValue();
        }
    }

    public static Float divideNoRound(float dividend, float divisor, int scale) {
        if ((double)divisor == 0.0D) {
            return 0.0F;
        } else {
            BigDecimal bgDividend = new BigDecimal(dividend + "");
            BigDecimal bgDivisor = new BigDecimal(divisor + "");
            BigDecimal divide = bgDividend.divide(bgDivisor).setScale(scale, 4);
            return divide.floatValue();
        }
    }

    public static void main(String[] args) {
System.out.println("==="+DecimalCalculationUtils.divideToFloat(DecimalCalculationUtils
					.multiply(Double.valueOf(80), 4), 100));
    	System.out.println(divideToInt(36, 10));
        System.out.println(multiplyScale2RoundHalfUp(0.4D, 4.4545D));
    }

    public static double string2double(String number) {
        BigDecimal bg = new BigDecimal(number);
        return bg.doubleValue();
    }

    public static boolean comparisonSizeForBig(double before, double after) {
        BigDecimal before1 = new BigDecimal(before);
        BigDecimal after1 = new BigDecimal(after);
        return before1.compareTo(after1) > 1;
    }

    public static boolean comparisonEqual(double before, double after) {
        BigDecimal before1 = new BigDecimal(before);
        BigDecimal after1 = new BigDecimal(after);
        return before1.compareTo(after1) == 0;
    }

    public static boolean comparisonSizeForSmall(double before, double after) {
        BigDecimal before1 = new BigDecimal(before);
        BigDecimal after1 = new BigDecimal(after);
        return before1.compareTo(after1) < 0;
    }
}
