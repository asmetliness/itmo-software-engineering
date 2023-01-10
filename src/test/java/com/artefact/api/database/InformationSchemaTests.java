package com.artefact.api.database;

import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.Artifact;
import com.artefact.api.model.Information;
import com.artefact.api.model.User;
import com.artefact.api.repository.ArtifactRepository;
import com.artefact.api.repository.InformationRepository;
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
import java.util.Date;
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

class InformationSchemaTests {

	@Autowired
	private InformationRepository informationRepository;
	@Autowired
	private UserRepository userRepository;


	@Test
	void contextLoads() {

		assertThat(informationRepository).isNotNull();
		assertThat(userRepository).isNotNull();
	}


	//Каждую проверку нужно в отдельный метод, т.к. каждый вызов Test
	//происходит в отдельной транзакции. Как только там появляется первое исключение,
	//все последующие вызовы выдают ошибки, независимо от того верные они или нет
	@Test()
	void information_title_null() {
		informationTestHelper((information) -> {
			information.setTitle(null);
		});
	}

	@Test
	void information_description_null() {
		informationTestHelper((information) -> {
			information.setDescription(null);
		});
	}

	@Test
	void information_information_null() {
		//price
		informationTestHelper((information) -> {
			information.setInformation(null);
		});
	}

	@Test
	void information_price_null() {
		//price
		informationTestHelper((information) -> {
			information.setPrice(null);
		});
	}

	@Test
	void information_creationDate_null() {
		//price
		informationTestHelper((information) -> {
			information.setCreationDate(null);
		});
	}

	@Test
	void information_createdUser_null() {
		informationTestHelper((information) -> {
			information.setCreatedUserId(null);
		});
	}

	@Test
	void information_createdUser_notExists() {
		informationTestHelper((information) -> {
			information.setCreatedUserId(1000L);
		});
	}
	@Test
	void information_requestedUser_notExists() {
		informationTestHelper((information) -> {
			information.setRequestedUserId(1000L);
		});
	}

	@Test
	void information_acquiredUser_notExists() {
		informationTestHelper((information) -> {
			information.setAcquiredUserId(1000L);
		});
	}


	@Test
	void information_statusId_null() {
		informationTestHelper((information) -> {
			information.setStatusId(null);
		});
	}
	@Test
	void information_statusId_notExists() {
		informationTestHelper((information) -> {
			information.setStatusId(1000L);
		});
	}

	@Test
	void information_success() {
		Assertions.assertDoesNotThrow(() -> {
			var information = createInformation();
			informationRepository.save(information);
		});
	}


	private void informationTestHelper(Consumer<Information> func) {
		Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
			var information = createInformation();
			func.accept(information);
			informationRepository.save(information);
		});
	}

	private Information createInformation() {
		var user = createUser();

		var information = new Information();
		information.setTitle("test");
		information.setDescription("test");
		information.setInformation("test");
		information.setPrice(new BigDecimal(100));
		information.setCreationDate(new Date());
		information.setCreatedUserId(user.getId());
		information.setStatusId(StatusIds.New);
		return information;

	}


	private User createUser() {
		var user = new User();
		user.setEmail("email" + UUID.randomUUID());
		user.setRole(Role.Client);
		user.setPasswordHash("123123");
		userRepository.save(user);
		return user;
	}


}
