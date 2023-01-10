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

class ArtifactSchemaTests {


	@Autowired
	private ArtifactRepository artifactRepository;

	@Test
	void contextLoads() {

		assertThat(artifactRepository).isNotNull();
	}


	//Каждую проверку нужно в отдельный метод, т.к. каждый вызов Test
	//происходит в отдельной транзакции. Как только там появляется первое исключение,
	//все последующие вызовы выдают ошибки, независимо от того верные они или нет

	@Test()
	void artifact_name_null() {
		artifactTestHelper((artifact) -> {
			artifact.setName(null);
		});
	}

	@Test()
	void artifact_price_null() {
		artifactTestHelper((artifact) -> {
			artifact.setPrice(null);
		});
	}

	@Test
	void artifact_success() {
		Assertions.assertDoesNotThrow(() -> {
			var artifact = createArtifact();
			artifactRepository.save(artifact);
		});
	}

	private void artifactTestHelper(Consumer<Artifact> func) {
		Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
			var artifact = createArtifact();
			func.accept(artifact);
			artifactRepository.save(artifact);
		});
	}


	private Artifact createArtifact() {
		var artifact = new Artifact();
		artifact.setName("test");
		artifact.setPrice(new BigDecimal(100));
		return artifact;
	}

}
