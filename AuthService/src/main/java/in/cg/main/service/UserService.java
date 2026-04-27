package in.cg.main.service;

import in.cg.main.dto.ForgotPasswordOtpRequest;
import in.cg.main.dto.ForgotPasswordOtpResponse;
import in.cg.main.dto.ForgotPasswordRequest;
import in.cg.main.dto.LoginDTO;
import in.cg.main.dto.LoginResponse;
import in.cg.main.dto.RegisterDTO;
import in.cg.main.exception.ResourceNotFoundException;

public interface UserService {

    void registerUser(RegisterDTO user) throws ResourceNotFoundException;

    LoginResponse loginUser(LoginDTO login) throws ResourceNotFoundException;

    ForgotPasswordOtpResponse requestForgotPasswordOtp(ForgotPasswordOtpRequest request)
            throws ResourceNotFoundException;

    void resetForgottenPassword(ForgotPasswordRequest request) throws ResourceNotFoundException;
}
