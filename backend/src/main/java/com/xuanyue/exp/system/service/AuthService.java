package com.xuanyue.exp.system.service;

import com.xuanyue.exp.system.dto.LoginRequest;
import com.xuanyue.exp.system.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
