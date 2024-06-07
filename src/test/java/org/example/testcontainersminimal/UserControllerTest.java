package org.example.testcontainersminimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

  @Autowired private UserRepository userRepository;

  @LocalServerPort private Integer port;

  private static PostgreSQLContainer<?> container;

  @BeforeAll
  public static void beforeAll() {
    TestcontainersConfiguration configuration = new TestcontainersConfiguration();
    container = configuration.postgresContainer();
    container.start();
  }

  @AfterAll
  static void afterAll() {
    container.stop();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", container::getJdbcUrl);
    registry.add("spring.datasource.username", container::getUsername);
    registry.add("spring.datasource.password", container::getPassword);
  }

  @BeforeEach
  public void beforeEach() {
    RestAssured.baseURI = "http://localhost:" + port;
    userRepository.deleteAll();
  }

  @Test
  void shouldFindAllUsers() {
    List<User> users =
        List.of(new User("John", "john@mail.com"), new User("Dennis", "dennis@mail.com"));
    userRepository.saveAll(users);

    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/api/users")
        .then()
        .statusCode(200)
        .body(".", hasSize(2));
  }
}
