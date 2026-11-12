package com.santosediego.cobaia_test.dto;

import com.santosediego.cobaia_test.services.validation.UserInsertValid;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@UserInsertValid
public class UserInsertDTO extends UserDTO {

	private static final long serialVersionUID = 1L;

	@Size(min = 2, max = 120, message = "Nome de usuário deve ter entre 2 e 120 caracteres.")
	@NotBlank(message = "Nome de usuário é obrigatório")
	private String username;
	@NotBlank(message = "Senha é obrigatória")
	private String password;
	@NotBlank(message = "Preenchimento obrigatório!")
	private String role;

	public UserInsertDTO() {
		super();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
