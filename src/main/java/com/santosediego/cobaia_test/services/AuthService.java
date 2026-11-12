package com.santosediego.cobaia_test.services;

import com.santosediego.cobaia_test.entities.User;
import com.santosediego.cobaia_test.entities.enums.Role;
import com.santosediego.cobaia_test.services.exceptions.ForbiddenException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public User authenticated() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new ForbiddenException("Acesso negado");
        }
        return user;
    }

    // Admin ou o próprio usuário
    public void validUser(Long id) {
        User user = authenticated();
        if (!user.getId().equals(id) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Acesso negado");
        }
    }

    public boolean isAdmin() {
        return authenticated().getRole() == Role.ADMIN;
    }
}
