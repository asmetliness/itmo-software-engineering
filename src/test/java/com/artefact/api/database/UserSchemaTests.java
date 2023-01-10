package com.artefact.api.database;

import com.artefact.api.consts.Role;
import com.artefact.api.model.User;
import com.artefact.api.model.User;
import com.artefact.api.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
		"POSTGRESQL_DB_HOST=localhost"
})
@ActiveProfiles("test")

class UserSchemaTests {


	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {

		assertThat(userRepository).isNotNull();
	}


	//Каждую проверку нужно в отдельный метод, т.к. каждый вызов Test
	//происходит в отдельной транзакции. Как только там появляется первое исключение,
	//все последующие вызовы выдают ошибки, независимо от того верные они или нет

	@Test()
	void user_email_null() {
		userTestHelper((user) -> {
			user.setEmail(null);
		});
	}

	@Test()
	void user_role_null() {
		userTestHelper((user) -> {
			user.setRole(null);
		});
	}

	@Test()
	void user_passwordHash_null() {
		userTestHelper((user) -> {
			user.setPasswordHash(null);
		});
	}

	@Test
	void user_email_unique() {
		Assertions.assertThrows(DataIntegrityViolationException.class,() -> {
			var user1 = createUser();
			user1.setEmail("email");
			userRepository.save(user1);

			var user2 = createUser();
			user2.setEmail("email");
			userRepository.save(user2);
		});
	}


	@Test
	void user_success() {
		Assertions.assertDoesNotThrow(() -> {
			var user = createUser();
			userRepository.save(user);
		});
	}

	private void userTestHelper(Consumer<User> func) {
		Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
			var user = createUser();
			func.accept(user);
			userRepository.save(user);
		});
	}


	private User createUser() {
		var user = new User();
		user.setEmail("email" + UUID.randomUUID());
		user.setRole(Role.Client);
		user.setPasswordHash("123123");
		return user;
	}

}
