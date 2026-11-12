package com.santosediego.cobaia_test.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.santosediego.cobaia_test.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByEmail(String email);

	User findByUsername(String username);

	long countByRoleAndActive(String role, Boolean active);

	@Query("""
			SELECT u
			FROM User u
			WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')))
			  AND (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
			  AND (:email IS NULL OR :email = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
			  AND (:active IS NULL OR u.active = :active)
			  AND (:role IS NULL OR u.role = :role)
			""")
	Page<User> findUsers(@Param("name") String name, @Param("username") String username, @Param("email") String email,
			@Param("active") Boolean active, @Param("role") String role, Pageable pageable);

}
