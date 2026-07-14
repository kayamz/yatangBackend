package com.kaya.yatang.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuthFrontendRedirectResolver oAuthFrontendRedirectResolver;

    @Value("${yatang.security.allowed-origins:}")
    private String allowedOrigins;

    @Value("${yatang.security.swagger-enabled:false}")
    private boolean swaggerEnabled;

    // authenticationManager를 Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        for (String origin : allowedOrigins.split(",")) {
            String trimmed = origin.trim();
            if (!trimmed.isEmpty()) {
                configuration.addAllowedOriginPattern(trimmed);
            }
        }
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .httpBasic().disable() // rest api 만을 고려하여 기본설정 해제
//                .csrf().disable()
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 토큰 기반 인증이므로 세션 사용 안함
//                .and()
//                .authorizeRequests() // 요청에 대한 사용 권한 체크
//                .requestMatchers("/admin/**").hasRole("ADMIN")
//                .requestMatchers("/api/post/**").authenticated()
//                .anyRequest().permitAll() // 그외 나머지 요청은 누구나 접근 가능
//                .and()
//                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
//        // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 전에 넣음
//        return http.build();
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // OAuth 실패 시 Spring 기본값이 /login 인데, /login도 authenticated면 무한 리다이렉트됨
        final String signInUrl = "http://jibbabbuja.duckdns.org/signin";

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    String path = request.getRequestURI() == null ? "" : request.getRequestURI();
                    // /login , /login?error 등으로 오면 SPA 로그인으로 보내서 루프 차단
                    if (path.equals("/login") || path.startsWith("/login?")) {
                        response.sendRedirect(signInUrl);
                        return;
                    }
                    String accept = request.getHeader("Accept");
                    if (accept != null && accept.contains("text/html")) {
                        response.sendRedirect(signInUrl);
                    } else {
                        response.sendError(401, "Unauthorized");
                    }
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/ingredients-catalog", "GET"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/ingredients-catalog/icon-map", "GET"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/public/**"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/oauth2/**"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/login/oauth2/**"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/login", "GET"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/error"))
                        .permitAll()
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/login"),
                                new AntPathRequestMatcher("/api/auth/refresh", "POST"),
                                new AntPathRequestMatcher("/api/auth/logout", "POST"),
                                new AntPathRequestMatcher("/api/register"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/users/signup", "POST"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/users/check-*", "GET"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/recipes/suggest", "POST"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/recipes/suggest-quota", "GET"))
                        .permitAll()
                        .requestMatchers(request -> swaggerEnabled && (
                                new AntPathRequestMatcher("/swagger-ui/**").matches(request)
                                        || new AntPathRequestMatcher("/swagger-ui.html").matches(request)
                                        || new AntPathRequestMatcher("/api-docs/**").matches(request)))
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            String msg = exception.getMessage() == null ? "oauth_failed" : exception.getMessage();
                            response.sendRedirect(oAuthFrontendRedirectResolver.errorUrl(request, msg));
                        }))
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

//    @Bean               // before adjust JWT authorization at swagger ui
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .httpBasic().disable() // rest api 만을 고려하여 기본설정 해제
//                .csrf().disable()
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 토큰 기반 인증이므로 세션 사용 안함
//                .and()
//                .authorizeRequests() // 요청에 대한 사용 권한 체크
//                .requestMatchers(new AntPathRequestMatcher("/api/**")
//                , new AntPathRequestMatcher("/admin/**")
//                , new AntPathRequestMatcher("/api/post/**")).permitAll()
//                .and()
//                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
//        // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 전에 넣음
//        return http.build();
//    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .httpBasic().disable()
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and()
//                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
//            .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
//                    .requestMatchers(new AntPathRequestMatcher("/api/hello")
//                            , new AntPathRequestMatcher("/api/register")
//                            , new AntPathRequestMatcher("/api/login")
//                            , new AntPathRequestMatcher("/**")).permitAll()
////                    .anyRequest().authenticated()
//                    .anyRequest().permitAll());
//        return http.build();
//    }



}