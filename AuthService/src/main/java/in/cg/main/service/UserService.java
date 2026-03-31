package in.cg.main.service;

import java.util.Map;

import in.cg.main.dto.LoginDTO;
import in.cg.main.dto.LoginResponse;
import in.cg.main.dto.RegisterDTO;
import in.cg.main.entities.User;
import in.cg.main.exception.ResourceNotFoundException;

public interface UserService {
	
	void registerUser(RegisterDTO user) throws ResourceNotFoundException;
	 LoginResponse loginUser(LoginDTO login) throws ResourceNotFoundException;
}
