package com.artefact.api;

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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.UUID;

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

	@Test
	void contextLoads() {
		assertThat(orderRepository).isNotNull();
	}


	@Test()
	void order_tests() {

		var user = createUser();


		Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
			var order = new Order();
			order.setCreatedUserId(user.getId());
			order.setArtifactId(10000L);
			order.setPrice(new BigDecimal(100));
			orderRepository.save(order);
		});

		Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
			var order = new Order();
			order.setCreatedUserId(user.getId());
			order.setPrice(new BigDecimal(100));
			order.setArtifactId(null);
			orderRepository.save(order);
		});
	}


	private User createUser() {
		var user = new User();
		user.setEmail("email" + UUID.randomUUID());
		userRepository.save(user);
		return user;
	}

	private Artifact createArtifact() {
		var artifact = new Artifact();
		artifact.setName("test");
		artifactRepository.save(artifact);
		return artifact;
	}

}
