package com.blog.web.config.security;

import ch.qos.logback.core.encoder.JsonEscapeUtil;
import com.blog.web.config.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author 苍晖
 * @since 2024/7/8 下午3:28
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtilService jwtUtilService;
    @Autowired
    private HttpServletResponse httpServletResponse;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();

        String jwt = jwtUtilService.createToken(authentication.getName());
        httpServletResponse.setHeader("X-Token", jwt);

        Result result = Result.success("登录成功");
        outputStream.write(JsonEscapeUtil.jsonEscapeString(result.toString()).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }
}
