package com.softsquare.report.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtBearerTokenAuthenticationConverter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableAutoConfiguration
public class SecureConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    String jwkSetUri;

    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(HttpMethod.OPTIONS)
                .antMatchers("/resources/**")
                .antMatchers("/report/guest/**")
                .antMatchers("/actuator/**");
        // // .antMatchers("/swagger**")
        // // .antMatchers("/swagger-resources/**")
        // // .antMatchers("/v2/api-docs")
        // // .antMatchers("/localize/**");
        // web.ignoring().antMatchers(HttpMethod.OPTIONS).antMatchers("/list/report/**").antMatchers("/report/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

// @formatter:off
        http
        .authorizeRequests(authorizeRequests ->
            authorizeRequests
             .antMatchers("/login", "/error", "/localize/**", "/actuator/**").permitAll()
//            .antMatchers("/**").permitAll()
//                  .antMatchers("/**","/test","/**/jsonString").hasAuthority("SCOPE_api")
                  .anyRequest().authenticated()
        )
        .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and().csrf().disable()
// disable anonymous will also disallow the /login above
//            .anonymous().disable()
;
// @formatter:on
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider(jwtDecoder());
        authenticationProvider.setJwtAuthenticationConverter(new JwtBearerTokenAuthenticationConverter());
        return authenticationProvider::authenticate;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        log.info("jwtDecoder() {}", jwkSetUri);
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).jwsAlgorithm(SignatureAlgorithm.ES256)
                .jwsAlgorithm(SignatureAlgorithm.RS256).build();
    }
}
