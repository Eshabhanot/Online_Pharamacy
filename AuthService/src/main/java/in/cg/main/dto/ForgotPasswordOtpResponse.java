package in.cg.main.dto;

public class ForgotPasswordOtpResponse {

    private final String message;

    public ForgotPasswordOtpResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
