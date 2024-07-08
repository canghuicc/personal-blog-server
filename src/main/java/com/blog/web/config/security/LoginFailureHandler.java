package com.blog.web.config.security;

import ch.qos.logback.core.encoder.JsonEscapeUtil;
import com.blog.web.config.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 苍晖
 * @since 2024/7/8 下午3:31
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    private HttpServletResponse httpServletResponse;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();

        String errorMessage = "用户名或密码错误";
        Result result = Result.error(errorMessage);
        outputStream.write(JsonEscapeUtil.jsonEscapeString(result.toString()).getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }
}
