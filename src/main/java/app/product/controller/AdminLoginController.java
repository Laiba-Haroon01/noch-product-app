package app.product.controller;

import app.product.service.AuthService;
import app.product.util.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminLoginController implements Initializable {

    @FXML private Label         subtitleLabel;
    @FXML private Button        customerBtn;
    @FXML private Button        staffBtn;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         emailErrorLabel;
    @FXML private Label         passwordErrorLabel;
    @FXML private Label         footerLabel;
    @FXML private Button        autofillBtn;

    private String currentUserType = "customer";
    private final AuthService authService = AuthService.getInstance();

    private static final String STAFF_EMAIL       = "admin@noch.com";
    private static final String STAFF_PASSWORD    = "staff123";
    private static final String CUSTOMER_EMAIL    = "customer@gmail.com";
    private static final String CUSTOMER_PASSWORD = "customer123";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setUserType("customer");

        // Clear errors as user types in email
        emailField.textProperty().addListener((obs, old, text) -> {
            clearEmailError();
            if (!text.isBlank()) validateEmail(text);
        });

        // Clear password error as user types
        passwordField.textProperty().addListener((obs, old, text) -> {
            if (!text.isBlank()) clearPasswordError();
        });
    }

    @FXML
    private void onCustomerBtn() {
        setUserType("customer");
    }

    @FXML
    private void onStaffBtn() {
        setUserType("staff");
    }

    @FXML
    private void onAutofill() {
        if ("staff".equals(currentUserType)) {
            emailField.setText(STAFF_EMAIL);
            passwordField.setText(STAFF_PASSWORD);
        } else {
            emailField.setText(CUSTOMER_EMAIL);
            passwordField.setText(CUSTOMER_PASSWORD);
        }
        clearEmailError();
        clearPasswordError();
    }

    @FXML
    private void onLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Clear previous errors
        clearEmailError();
        clearPasswordError();

        boolean hasError = false;

        // Check email empty
        if (email.isBlank()) {
            showEmailError("Email address is required.");
            hasError = true;
        } else {
            // Check email format
            String formatError = validateEmailString(email, currentUserType);
            if (!formatError.isEmpty()) {
                showEmailError(formatError);
                hasError = true;
            }
        }

        // Check password empty
        if (password.isBlank()) {
            showPasswordError("Password is required.");
            hasError = true;
        }

        if (hasError) return;

        // Attempt login with exact credential check
        boolean success = authService.login(email, password, currentUserType);

        if (success) {
            try {
                if ("staff".equals(currentUserType)) {
                    SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
                } else {
                    SceneManager.getInstance().switchTo(SceneManager.SHOP);
                }
            } catch (Exception e) {
                showEmailError("Could not load screen: " + e.getMessage());
            }
        } else {
            // Show specific errors for wrong email or password
            if ("staff".equals(currentUserType)) {
                if (!email.equalsIgnoreCase(STAFF_EMAIL)) {
                    showEmailError("Incorrect email address.");
                } else {
                    showPasswordError("Incorrect password.");
                }
            } else {
                if (!email.equalsIgnoreCase(CUSTOMER_EMAIL)) {
                    showEmailError("Incorrect email address.");
                } else {
                    showPasswordError("Incorrect password.");
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setUserType(String type) {
        currentUserType = type;
        clearEmailError();
        clearPasswordError();
        emailField.clear();
        passwordField.clear();

        if ("staff".equals(type)) {
            staffBtn.getStyleClass().setAll("toggle-btn", "toggle-btn-active");
            customerBtn.getStyleClass().setAll("toggle-btn", "toggle-btn-inactive");
            subtitleLabel.setText("STAFF ACCESS");
            footerLabel.setText("Authorized staff only");
            emailField.setPromptText("staff@noch.com");
            autofillBtn.setText("Auto-fill staff credentials");
        } else {
            customerBtn.getStyleClass().setAll("toggle-btn", "toggle-btn-active");
            staffBtn.getStyleClass().setAll("toggle-btn", "toggle-btn-inactive");
            subtitleLabel.setText("CUSTOMER ACCESS");
            footerLabel.setText("Welcome to Noch");
            emailField.setPromptText("customer@email.com");
            autofillBtn.setText("Auto-fill customer credentials");
        }
    }

    private String validateEmailString(String email, String type) {
        if (email == null || email.isBlank()) return "";
        String lower = email.toLowerCase().trim();
        if ("staff".equals(type)) {
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
            if (!valid) return "Please enter a valid email address.";
        }
        return "";
    }

    private void validateEmail(String email) {
        String error = validateEmailString(email, currentUserType);
        if (!error.isEmpty()) showEmailError(error);
        else clearEmailError();
    }

    private void showEmailError(String msg) {
        emailErrorLabel.setText(msg);
        emailErrorLabel.setVisible(true);
        emailErrorLabel.setManaged(true);
        emailField.getStyleClass().remove("field-error");
        emailField.getStyleClass().add("field-error");
    }

    private void clearEmailError() {
        emailErrorLabel.setText("");
        emailErrorLabel.setVisible(false);
        emailErrorLabel.setManaged(false);
        emailField.getStyleClass().remove("field-error");
    }

    private void showPasswordError(String msg) {
        passwordErrorLabel.setText(msg);
        passwordErrorLabel.setVisible(true);
        passwordErrorLabel.setManaged(true);
        passwordField.getStyleClass().remove("field-error");
        passwordField.getStyleClass().add("field-error");
    }

    private void clearPasswordError() {
        passwordErrorLabel.setText("");
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);
        passwordField.getStyleClass().remove("field-error");
    }
}