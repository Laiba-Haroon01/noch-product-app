package app.product.service;

public class AuthService {

    private static AuthService instance;
    private String currentUserType = null;

    // Valid credentials — only these will work
    private static final String STAFF_EMAIL       = "admin@noch.com";
    private static final String STAFF_PASSWORD    = "staff123";
    private static final String CUSTOMER_EMAIL    = "customer@gmail.com";
    private static final String CUSTOMER_PASSWORD = "customer123";

    private AuthService() {}

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Validates email format only (for real-time error display).
     * Staff:    must contain @noch.com
     * Customer: must be a valid-looking email ending in .com
     */
    public String validateEmail(String email, String userType) {
        if (email == null || email.isBlank()) return "";
        String lower = email.toLowerCase().trim();

        if ("staff".equals(userType)) {
            if (!lower.contains("@noch.com")) {
                return "Staff email must use a @noch.com address.";
            }
        } else {
            int atIndex  = lower.indexOf('@');
            int dotIndex = lower.lastIndexOf('.');
            boolean valid = atIndex > 0
                    && dotIndex > atIndex + 1
                    && dotIndex < lower.length() - 1
                    && lower.endsWith(".com")
                    && !lower.contains(" ");
            if (!valid) {
                return "Please enter a valid email address.";
            }
        }
        return "";
    }

    /**
     * Login — checks credentials against hardcoded valid values only.
     * Wrong email or password will return false.
     */
    public boolean login(String email, String password, String userType) {
        if (email == null || password == null) return false;

        String emailTrimmed    = email.trim();
        String passwordTrimmed = password.trim();

        if ("staff".equals(userType)) {
            if (emailTrimmed.equalsIgnoreCase(STAFF_EMAIL)
                    && passwordTrimmed.equals(STAFF_PASSWORD)) {
                currentUserType = "staff";
                return true;
            }
        } else {
            if (emailTrimmed.equalsIgnoreCase(CUSTOMER_EMAIL)
                    && passwordTrimmed.equals(CUSTOMER_PASSWORD)) {
                currentUserType = "customer";
                return true;
            }
        }

        return false;
    }

    public void logout() {
        currentUserType = null;
    }

    public String getCurrentUserType() {
        return currentUserType;
    }

    public boolean isLoggedIn() {
        return currentUserType != null;
    }
}