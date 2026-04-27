package in.cg.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.cg.main.dto.ForgotPasswordOtpRequest;
import in.cg.main.dto.ForgotPasswordOtpResponse;
import in.cg.main.dto.ForgotPasswordRequest;
import in.cg.main.dto.LoginDTO;
import in.cg.main.dto.LoginResponse;
import in.cg.main.dto.RegisterDTO;
import in.cg.main.exception.ResourceNotFoundException;
import in.cg.main.service.UserServiceImp;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserServiceImp userService;

    public UserController(UserServiceImp userService) {
        this.userService = userService;
    }

    @PostMapping({"/register", "/signup"})
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterDTO register) throws ResourceNotFoundException {
        userService.registerUser(register);
        return ResponseEntity.ok("User Registered Successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginDTO login) throws ResourceNotFoundException {
        return ResponseEntity.ok(userService.loginUser(login));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordOtpResponse> forgotPassword(@Valid @RequestBody ForgotPasswordOtpRequest request)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(userService.requestForgotPasswordOtp(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ForgotPasswordRequest request)
            throws ResourceNotFoundException {
        userService.resetForgottenPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }
}
