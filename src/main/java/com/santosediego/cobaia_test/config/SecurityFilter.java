package com.santosediego.cobaia_test.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.santosediego.cobaia_test.repositories.UserRepository;
import com.santosediego.cobaia_test.services.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter {

	private static Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

	private final TokenService tokenService;
	private final UserRepository userRepository;

	public SecurityFilter(TokenService tokenService, UserRepository userRepository) {
		this.tokenService = tokenService;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = recoverToken(request);

		if (token != null) {
			String username = tokenService.validateToken(token);

			if (username != null) {
				UserDetails user = userRepository.findByUsername(username);

				if (user != null && user.isEnabled()) {
					var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		}

		filterChain.doFilter(request, response);
	}

	private String recoverToken(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			logger.warn("Token ausente ou malformado no header Authorization");
			return null;
		}

		return authHeader.substring(7); // Remove "Bearer "
	}
}
