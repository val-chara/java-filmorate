package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private Validator validator;
    private User validUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validUser = new User();
        validUser.setEmail("user@example.com");
        validUser.setLogin("validlogin");
        validUser.setName("Valid Name");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldCreateValidUser() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Валидный пользователь не должен иметь нарушений валидации");
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        validUser.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Пользователь с пустой почтой должен иметь нарушения валидации");
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        validUser.setEmail("invalid-email");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Пользователь с невалидной почтой должен иметь нарушения валидации");
    }

    @Test
    void shouldFailWhenLoginIsBlank() {
        validUser.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Пользователь с пустым логином должен иметь нарушения валидации");
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        validUser.setLogin("login with spaces");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Пользователь с логином содержащим пробелы должен иметь нарушения валидации");
    }

    @Test
    void shouldUseLoginWhenNameIsBlank() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Пользователь с пустым именем должен быть валидным (имя подставится в контроллере)");
    }

    @Test
    void shouldFailWhenBirthdayIsInFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Пользователь с датой рождения в будущем должен иметь нарушения валидации");
    }

    @Test
    void shouldPassWhenBirthdayIsToday() {
        validUser.setBirthday(LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Пользователь с сегодняшней датой рождения должен иметь нарушения (только прошлые даты разрешены)");
    }

    @Test
    void shouldPassWhenBirthdayIsYesterday() {
        validUser.setBirthday(LocalDate.now().minusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Пользователь со вчерашней датой рождения должен быть валидным");
    }
}
