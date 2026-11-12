package com.santosediego.cobaia_test.dto;

import java.io.Serializable;
import java.time.Instant;

import com.santosediego.cobaia_test.entities.User;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	@NotBlank(message = "Nome é obrigatório")
	@Size(min = 3, max = 120, message = "O nome deve ter entre 5 e 120 caracteres.")
	private String name;
	private String username;
	@Email(message = "Email inválido")
	@Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres.")
	private String email;
	private Boolean active = true;
	private String role;
	private Instant createdAt;
	private Instant updatedAt;

	public UserDTO() {
	}

	public UserDTO(Long id, String name, String username, String email, Boolean active, String role, Instant createdAt,
			Instant updatedAt) {
		this.id = id;
		this.name = name;
		this.username = username;
		this.email = email;
		this.active = active;
		this.role = role;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UserDTO(User entity) {
		id = entity.getId();
		name = entity.getName();
		username = entity.getUsername();
		email = entity.getEmail();
		active = entity.getActive();
		role = entity.getRole().getId();
		createdAt = entity.getCreatedAt();
		updatedAt = entity.getUpdatedAt();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = (active != null) ? active : true;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	@PrePersist
	public void prePersist() {
		createdAt = Instant.now();
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = Instant.now();
	}
}
