package itcr.utilities;

public class Validations {

    public static boolean isInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(str.trim()); 
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
