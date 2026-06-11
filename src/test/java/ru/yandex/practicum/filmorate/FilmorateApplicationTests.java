package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserDbStorage.class)
class FilmorateApplicationTests {
	private final UserDbStorage userStorage;

	@Test
	public void testCreateAndFindUserById() {
		User user = new User();
		user.setEmail("test@test.com");
		user.setLogin("testlogin");
		user.setName("Test Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		User created = userStorage.add(user);

		Optional<User> userOptional = userStorage.getById(created.getId());

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(u ->
						assertThat(u).hasFieldOrPropertyWithValue("login", "testlogin")
				);
	}

	@Test
	public void testUpdateUser() {
		User user = new User();
		user.setEmail("update@test.com");
		user.setLogin("updatelogin");
		user.setName("Before");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		User created = userStorage.add(user);
		created.setName("After");
		userStorage.update(created);

		Optional<User> updated = userStorage.getById(created.getId());
		assertThat(updated).isPresent()
				.hasValueSatisfying(u -> assertThat(u.getName()).isEqualTo("After"));
	}

	@Test
	public void testGetAllUsers() {
		User user = new User();
		user.setEmail("all@test.com");
		user.setLogin("alllogin");
		user.setName("All");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		userStorage.add(user);

		assertThat(userStorage.getAll()).isNotEmpty();
	}
}
