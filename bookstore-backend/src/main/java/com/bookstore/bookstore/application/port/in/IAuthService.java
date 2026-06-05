package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.LoginCommand;
import com.bookstore.bookstore.application.command.RegisterCommand;
import com.bookstore.bookstore.application.result.LoginResult;
import com.bookstore.bookstore.application.result.RegisterResult;

public interface IAuthService {

    RegisterResult register(RegisterCommand command);

    LoginResult login(LoginCommand command);
}
