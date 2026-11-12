package com.santosediego.cobaia_test.services;

import java.util.Optional;

import com.santosediego.cobaia_test.services.exceptions.DomainValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.santosediego.cobaia_test.dto.UserDTO;
import com.santosediego.cobaia_test.dto.UserInsertDTO;
import com.santosediego.cobaia_test.dto.UserUpdateDTO;
import com.santosediego.cobaia_test.entities.User;
import com.santosediego.cobaia_test.entities.enums.Role;
import com.santosediego.cobaia_test.repositories.UserRepository;
import com.santosediego.cobaia_test.services.exceptions.DatabaseException;
import com.santosediego.cobaia_test.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService implements UserDetailsService {

	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository repository;

	@Autowired
	private AuthService authService;

	@Transactional(readOnly = true)
	public Page<UserDTO> findAllPaged(Pageable pageable, String name, String username, String email, Boolean active,
			String role) {

		String n = (name != null) ? name.trim() : null;
		String u = (username != null) ? username.trim() : null;
		String e = (email != null) ? email.trim() : null;
		String r = (role != null && !role.isBlank()) ? role.trim() : null;

		Page<User> list = repository.findUsers(n, u, e, active, r, pageable);
		return list.map(x -> new UserDTO(x));
	}

	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		Optional<User> obj = repository.findById(id);
		User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new UserDTO(entity);
	}

	@Transactional
	public UserDTO insert(UserInsertDTO dto) {
		User entity = new User();
		copyDtoToEntity(dto, entity);
		entity.setUsername(dto.getUsername());
		entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		entity.setRole(Role.fromId(dto.getRole()));
		entity.setActive(dto.getActive());
		entity = repository.save(entity);
		return new UserDTO(entity);
	}

	@Transactional
	public UserDTO update(Long id, UserUpdateDTO dto) {
		try {
			authService.validUser(id);
			User entity = repository.getReferenceById(id);
			if (authService.isAdmin()) {
				applyAdminChanges(dto, entity);
			}
			copyDtoToEntity(dto, entity);
			entity = repository.saveAndFlush(entity);
			return new UserDTO(entity);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
	}

	@Transactional
	public void delete(Long id) {
		User target = repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Id not found " + id));
		validateNotLastActiveAdmin(target);
		try {
			repository.delete(target);
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}

	private void copyDtoToEntity(UserDTO dto, User entity) {
		entity.setName(dto.getName());
		if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
			entity.setEmail(dto.getEmail());
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = repository.findByUsername(username);
		if (user == null) {
			logger.error("User not found: " + username);
			throw new UsernameNotFoundException("Email not found");
		}
		logger.info("User found: " + username);
		return user;
	}

	private void validateNotLastActiveAdmin(User target) {
		boolean isActiveAdmin = target.getRole() == Role.ADMIN && Boolean.TRUE.equals(target.getActive());
		if (isActiveAdmin && repository.countByRoleAndActive(Role.ADMIN.getId(), true) <= 1) {
			throw new DomainValidationException("There must be at least one active administrator");
		}
	}

	private void applyAdminChanges(UserUpdateDTO dto, User entity) {
		boolean demoting = dto.getRole() != null && Role.fromId(dto.getRole()) != Role.ADMIN;
		boolean deactivating = Boolean.FALSE.equals(dto.getActive());

		if (demoting || deactivating) {
			validateNotLastActiveAdmin(entity);
		}
		if (dto.getRole() != null) entity.setRole(Role.fromId(dto.getRole()));
		if (dto.getActive() != null) entity.setActive(dto.getActive());
	}
}
