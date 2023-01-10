package com.artefact.api.database;

import com.artefact.api.model.Status;
import com.artefact.api.repository.StatusRepository;
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

import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
		"POSTGRESQL_DB_HOST=localhost"
})
@ActiveProfiles("test")

class StatusSchemaTests {


	@Autowired
	private StatusRepository statusRepository;

	@Test
	void contextLoads() {

		assertThat(statusRepository).isNotNull();
	}


	//Каждую проверку нужно в отдельный метод, т.к. каждый вызов Test
	//происходит в отдельной транзакции. Как только там появляется первое исключение,
	//все последующие вызовы выдают ошибки, независимо от того верные они или нет

	@Test()
	void status_name_null() {
		statusTestHelper((status) -> {
			status.setName(null);
		});
	}




	private void statusTestHelper(Consumer<Status> func) {
		Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
			var status = createStatus();
			func.accept(status);
			statusRepository.save(status);
		});
	}


	private Status createStatus() {
		var status = new Status();
		status.setId(1000L);
		status.setName("test");
		return status;
	}

}
