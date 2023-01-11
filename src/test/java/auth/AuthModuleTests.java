package auth;

import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.controller.AuthController;
import com.artefact.api.request.RegisterRequest;
import com.artefact.api.response.AuthResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = ApiApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "POSTGRESQL_DB_HOST=localhost"
})
@ActiveProfiles("test")
public class AuthModuleTests {
    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void testRegistration_basic() {
        var request = new RegisterRequest(
                "email@mail.ru",
                "password",
                Role.Client
        );

        var response = this.restTemplate
                .postForEntity("/api/auth/register", request, AuthResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(response.getBody().getToken());
        Assertions.assertEquals(request.getEmail(), response.getBody().getUser().getEmail());
        Assertions.assertEquals(request.getRole(), response.getBody().getUser().getRole());
    }


}
