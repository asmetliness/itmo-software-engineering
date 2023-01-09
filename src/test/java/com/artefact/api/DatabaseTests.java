package com.artefact.api;

import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.Artifact;
import com.artefact.api.model.Order;
import com.artefact.api.model.User;
import com.artefact.api.repository.ArtifactRepository;
import com.artefact.api.repository.OrderRepository;
import com.artefact.api.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
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

class DatabaseTests {

	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ArtifactRepository artifactRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void contextLoads() {
		assertThat(orderRepository).isNotNull();
	}


	//Каждую проверку нужно в отдельный метод, т.к. каждый вызов Test
	//происходит в отдельной транзакции. Как только там появляется первое исключение,
	//все последующие вызовы выдают ошибки, независимо от того верные они или нет
	@Test()
	void order_artifactId_null() {
		orderTestHelper(DataIntegrityViolationException.class, (order) -> {
			order.setArtifactId(null);
		});
	}

	@Test
	void order_artifactId_notExists() {
		orderTestHelper(DataIntegrityViolationException.class, (order) -> {
			order.setArtifactId(10000L);
		});
	}

	@Test
	void order_price_null() {
		//price
		orderTestHelper(DataIntegrityViolationException.class, (order) -> {
			order.setPrice(null);
		});
	}

	@Test
	void order_success() {
		Assertions.assertDoesNotThrow(() -> {
			var order = createOrder();
			orderRepository.save(order);
		});
	}


	private <T extends Throwable> void orderTestHelper(Class<T> expected, Consumer<Order> func) {
		Assertions.assertThrows(expected, () -> {
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
