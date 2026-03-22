package app.product.util;

public class ValidationUtil {
	 
    private ValidationUtil() {}
 
    public static boolean isPositiveDouble(String value) {
        try {
            return Double.parseDouble(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
 
    public static boolean isNonNegativeInt(String value) {
        try {
            int v = Integer.parseInt(value);
            return v >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
 
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
