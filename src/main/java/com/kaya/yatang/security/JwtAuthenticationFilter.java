package com.kaya.yatang.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        // 헤더에서 JWT를 받아옴
//        String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);
//        // 유효한 토큰인지 확인
//        if (token != null && jwtTokenProvider.validateToken(token)) {
//            // 토큰이 유효하면 토큰으로부터 유저 정보를 받아옴
//            Authentication authentication = jwtTokenProvider.getAuthentication(token);
//            // SecurityContext에 Authentication 객체를 저장
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        }
//        chain.doFilter(request, response);
//    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);

        System.out.println("=== JWT Filter ===");
        System.out.println("token: " + token);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("authentication 성공: " + authentication.getName());
        } else {
            System.out.println("token null 이거나 validateToken 실패");
        }

        chain.doFilter(request, response);
    }

}
