package com.artefact.api.database;

import com.artefact.api.consts.Role;
import com.artefact.api.model.Notification;
import com.artefact.api.model.User;
import com.artefact.api.repository.NotificationRepository;
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

class NotificationSchemaTests {


	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {

		assertThat(notificationRepository).isNotNull();
	}


	//Каждую проверку нужно в отдельный метод, т.к. каждый вызов Test
	//происходит в отдельной транзакции. Как только там появляется первое исключение,
	//все последующие вызовы выдают ошибки, независимо от того верные они или нет

	@Test()
	void notification_message_null() {
		notificationTestHelper((notification) -> {
			notification.setMessage(null);
		});
	}

	@Test()
	void notification_userId_null() {
		notificationTestHelper((notification) -> {
			notification.setUserId(null);
		});
	}

	@Test()
	void notification_userId_notExists() {
		notificationTestHelper((notification) -> {
			notification.setUserId(1000l);
		});
	}

	@Test()
	void notification_orderId_notExists() {
		notificationTestHelper((notification) -> {
			notification.setOrderId(1000l);
		});
	}


	@Test()
	void notification_weaponOrderId_notExists() {
		notificationTestHelper((notification) -> {
			notification.setWeaponOrderId(1000l);
		});
	}

	@Test()
	void notification_informationOrderId_notExists() {
		notificationTestHelper((notification) -> {
			notification.setInformationOrderId(1000l);
		});
	}

	@Test
	void notification_success() {
		Assertions.assertDoesNotThrow(() -> {
			var notification = createNotification();
			notificationRepository.save(notification);
		});
	}

	private void notificationTestHelper(Consumer<Notification> func) {
		Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
			var notification = createNotification();
			func.accept(notification);
			notificationRepository.save(notification);
		});
	}


	private Notification createNotification() {
		var user = createUser();

		var notification = new Notification();
		notification.setMessage("test");
		notification.setUserId(user.getId());
		return notification;
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
