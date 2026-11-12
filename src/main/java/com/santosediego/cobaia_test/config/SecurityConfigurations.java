package com.santosediego.cobaia_test.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

	@Autowired
	private Environment env;

	@Autowired
	SecurityFilter securityFilter;

	@Value("${cors.origins}")
	private String corsOrigins;

	private static final String[] PUBLIC = { "/auth/login", "/h2-console/**", "/error" };
	private static final String[] OPERATOR = { "/users/*" };
	private static final String[] ADMIN = { "/users/**" };

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

		// Liberar H2 apenas no profile de teste
		if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
			httpSecurity.headers(headers -> headers.frameOptions(frame -> frame.disable()));
		}

		return httpSecurity
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(PUBLIC).permitAll()
						.requestMatchers(HttpMethod.PUT, OPERATOR).hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.GET, OPERATOR).hasAnyRole("USER", "ADMIN")
						.requestMatchers(ADMIN).hasRole("ADMIN")
						.anyRequest().authenticated())
				.addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();

		String[] origins = corsOrigins.split(",");

	    config.setAllowedOriginPatterns(Arrays.asList(origins));
	    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
	    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
	    config.setAllowCredentials(true);

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config);

	    return new CorsFilter(source);
	}
}
