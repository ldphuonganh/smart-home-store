package vn.edu.crs.auth_service.service;

import vn.edu.crs.auth_service.dto.UserDTO;
import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
}