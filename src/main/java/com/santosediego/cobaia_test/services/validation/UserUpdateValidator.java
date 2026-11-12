package com.santosediego.cobaia_test.services.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

import com.santosediego.cobaia_test.dto.UserUpdateDTO;
import com.santosediego.cobaia_test.entities.User;
import com.santosediego.cobaia_test.entities.enums.Role;
import com.santosediego.cobaia_test.repositories.UserRepository;
import com.santosediego.cobaia_test.resources.exceptions.FieldMessage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserUpdateValidator implements ConstraintValidator<UserUpdateValid, UserUpdateDTO> {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private UserRepository repository;

	@Override
	public void initialize(UserUpdateValid ann) {
	}

	@Override
	public boolean isValid(UserUpdateDTO dto, ConstraintValidatorContext context) {

		@SuppressWarnings("unchecked")
		var uriVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		long userId = Long.parseLong(uriVars.get("id"));

		List<FieldMessage> list = new ArrayList<>();
		String email = dto.getEmail();

		if (email != null && !email.isBlank()) {
			User user = repository.findByEmail(email);
			if (user != null && userId != user.getId()) {
				list.add(new FieldMessage("email", "Email já existe"));
			}
		}

		if (dto.getRole() != null) {
			boolean roleValida = false;
			for (Role r : Role.values()) {
				if (r.getId().equals(dto.getRole())) {
					roleValida = true;
					break;
				}
			}
			if (!roleValida) {
				list.add(new FieldMessage("role", "Role inválida"));
			}
		}

		for (FieldMessage e : list) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
					.addConstraintViolation();
		}
		return list.isEmpty();
	}
}
