package com.artefact.api.database;

import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.Weapon;
import com.artefact.api.model.User;
import com.artefact.api.repository.WeaponRepository;
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

class WeaponSchemaTests {

	@Autowired
	private WeaponRepository weaponRepository;
	@Autowired
	private UserRepository userRepository;


	@Test
	void contextLoads() {

		assertThat(weaponRepository).isNotNull();
		assertThat(userRepository).isNotNull();
	}


	//Каждую проверку нужно в отдельный метод, т.к. каждый вызов Test
	//происходит в отдельной транзакции. Как только там появляется первое исключение,
	//все последующие вызовы выдают ошибки, независимо от того верные они или нет
	@Test()
	void weapon_title_null() {
		weaponTestHelper((weapon) -> {
			weapon.setTitle(null);
		});
	}



	@Test
	void weapon_price_null() {
		//price
		weaponTestHelper((weapon) -> {
			weapon.setPrice(null);
		});
	}

	@Test
	void weapon_creationDate_null() {
		//price
		weaponTestHelper((weapon) -> {
			weapon.setCreationDate(null);
		});
	}

	@Test
	void weapon_createdUser_null() {
		weaponTestHelper((weapon) -> {
			weapon.setCreatedUserId(null);
		});
	}

	@Test
	void weapon_createdUser_notExists() {
		weaponTestHelper((weapon) -> {
			weapon.setCreatedUserId(1000L);
		});
	}
	@Test
	void weapon_requestedUser_notExists() {
		weaponTestHelper((weapon) -> {
			weapon.setRequestedUserId(1000L);
		});
	}

	@Test
	void weapon_acquiredUser_notExists() {
		weaponTestHelper((weapon) -> {
			weapon.setAcquiredUserId(1000L);
		});
	}


	@Test
	void weapon_suggestedCourier_notExists() {
		weaponTestHelper((weapon) -> {
			weapon.setSuggestedCourierId(1000L);
		});
	}

	@Test
	void weapon_acceptedCourier_notExists() {
		weaponTestHelper((weapon) -> {
			weapon.setAcceptedCourierId(1000L);
		});
	}

	@Test
	void weapon_statusId_null() {
		weaponTestHelper((weapon) -> {
			weapon.setStatusId(null);
		});
	}
	@Test
	void weapon_statusId_notExists() {
		weaponTestHelper((weapon) -> {
			weapon.setStatusId(1000L);
		});
	}

	@Test
	void weapon_success() {
		Assertions.assertDoesNotThrow(() -> {
			var weapon = createWeapon();
			weaponRepository.save(weapon);
		});
	}


	private void weaponTestHelper(Consumer<Weapon> func) {
		Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
			var weapon = createWeapon();
			func.accept(weapon);
			weaponRepository.save(weapon);
		});
	}

	private Weapon createWeapon() {
		var user = createUser();

		var weapon = new Weapon();
		weapon.setTitle("test");
		weapon.setDescription("test");
		weapon.setPrice(new BigDecimal(100));
		weapon.setCreationDate(new Date());
		weapon.setCreatedUserId(user.getId());
		weapon.setStatusId(StatusIds.New);
		return weapon;

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
