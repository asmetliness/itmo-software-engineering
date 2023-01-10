package com.artefact.api.database;

import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.Artifact;
import com.artefact.api.model.Order;
import com.artefact.api.model.User;
import com.artefact.api.repository.ArtifactRepository;
import com.artefact.api.repository.OrderRepository;
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

class OrderSchemaTests {

	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ArtifactRepository artifactRepository;

	@Test
	void contextLoads() {

		assertThat(orderRepository).isNotNull();
		assertThat(artifactRepository).isNotNull();
		assertThat(userRepository).isNotNull();
	}


	//Каждую проверку нужно в отдельный метод, т.к. каждый вызов Test
	//происходит в отдельной транзакции. Как только там появляется первое исключение,
	//все последующие вызовы выдают ошибки, независимо от того верные они или нет
	@Test()
	void order_artifactId_null() {
		orderTestHelper((order) -> {
			order.setArtifactId(null);
		});
	}

	@Test
	void order_artifactId_notExists() {
		orderTestHelper((order) -> {
			order.setArtifactId(10000L);
		});
	}

	@Test
	void order_price_null() {
		//price
		orderTestHelper((order) -> {
			order.setPrice(null);
		});
	}

	@Test
	void order_createdUser_null() {
		orderTestHelper((order) -> {
			order.setCreatedUserId(null);
		});
	}

	@Test
	void order_createdUser_notExists() {
		orderTestHelper((order) -> {
			order.setCreatedUserId(1000L);
		});
	}
	@Test
	void order_acceptedUser_notExists() {
		orderTestHelper((order) -> {
			order.setAcceptedUserId(1000L);
		});
	}

	@Test
	void order_assignedUser_notExists() {
		orderTestHelper((order) -> {
			order.setAssignedUserId(1000L);
		});
	}

	@Test
	void order_suggestedUser_notExists() {
		orderTestHelper((order) -> {
			order.setSuggestedUserId(1000L);
		});
	}

	@Test
	void order_acceptedCourierId_notExists() {
		orderTestHelper((order) -> {
			order.setAcceptedCourierId(1000L);
		});
	}


	@Test
	void order_statusId_null() {
		orderTestHelper((order) -> {
			order.setStatusId(null);
		});
	}
	@Test
	void order_statusId_notExists() {
		orderTestHelper((order) -> {
			order.setStatusId(1000L);
		});
	}



	@Test
	void order_success() {
		Assertions.assertDoesNotThrow(() -> {
			var order = createOrder();
			orderRepository.save(order);
		});
	}


	private void orderTestHelper(Consumer<Order> func) {
		Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
			var order = createOrder();
			func.accept(order);
			orderRepository.save(order);
		});
	}

	private Order createOrder() {
		var user = createUser();
		var artifact = createArtifact();
		var order = new Order();
		order.setCreatedUserId(user.getId());
		order.setArtifactId(artifact.getId());
		order.setPrice(new BigDecimal(100));
		order.setStatusId(StatusIds.New);

		return order;

	}


	private User createUser() {
		var user = new User();
		user.setEmail("email" + UUID.randomUUID());
		user.setRole(Role.Client);
		user.setPasswordHash("123123");
		userRepository.save(user);
		return user;
	}

	private Artifact createArtifact() {
		var artifact = new Artifact();
		artifact.setName("test");
		artifact.setPrice(new BigDecimal(100));
		artifactRepository.save(artifact);
		return artifact;
	}

}
