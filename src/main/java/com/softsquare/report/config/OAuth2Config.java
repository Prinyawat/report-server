package com.softsquare.report.config;
//package com.softsquare.ssru.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//
//// @Configuration
//// @EnableResourceServer
//@Slf4j
//@EnableWebSecurity
//public class OAuth2Config /*extends WebSecurityConfigurerAdapter */ {
//
//    // @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
//    // String jwkSetUri;
//
//    // @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        // @formatter:off
//		http
//			.authorizeRequests(authorizeRequests ->
//                authorizeRequests
//                  .antMatchers("/login", "/error", "/localize/**").permitAll()
////                  .antMatchers("/**").authenticated()
////                  .antMatchers("/**","/test","/**/jsonString").hasAuthority("SCOPE_api")
//                  .anyRequest().authenticated()
//            )
//			.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
//            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//        .and().csrf().disable()
//// disable anonymous will also disallow the /login above
////            .anonymous().disable()
//        ;
//		// @formatter:on
//    }
//
//    // @Bean
//    // public AuthenticationManager authenticationManager() {
//    //     JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider(jwtDecoder());
//    //     authenticationProvider.setJwtAuthenticationConverter(new JwtBearerTokenAuthenticationConverter());
//    //     return authenticationProvider::authenticate;
//    // }
//
//    // @Bean
//    // JwtDecoder jwtDecoder() {
//    //     log.info("jwtDecoder() {}", jwkSetUri);
//    //     return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
//    // }
//    // @Override
//    // public void configure(HttpSecurity http) throws Exception {       
//    //     http
//    // 	.antMatcher("/**")
//    // 	.authorizeRequests()
//    // 	.anyRequest()
//    //     .authenticated()
//    //     .and().csrf().disable().httpBasic().and()
//    // 	.exceptionHandling().accessDeniedPage("/403")
//    //     .and().exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
//    // }
//
//}
