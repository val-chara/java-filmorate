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

class FilmTest {
    private Validator validator;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void shouldCreateValidFilm() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Валидный фильм не должен иметь нарушений валидации");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        validFilm.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Фильм с пустым названием должен иметь нарушения валидации");
    }

    @Test
    void shouldFailWhenNameIsNull() {
        validFilm.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Фильм с null названием должен иметь нарушения валидации");
    }

    @Test
    void shouldFailWhenDescriptionIsTooLong() {
        validFilm.setDescription("A".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Фильм с описанием длиннее 200 символов должен иметь нарушения валидации");
    }

    @Test
    void shouldPassWhenDescriptionIsExactly200Chars() {
        validFilm.setDescription("A".repeat(200));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Фильм с описанием длиной 200 символов должен быть валидным");
    }

    @Test
    void shouldPassWhenReleaseDateIsExactlyMinDate() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Фильм с датой релиза 28.12.1895 должен быть валидным");
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        validFilm.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Фильм с отрицательной продолжительностью должен иметь нарушения валидации");
    }

    @Test
    void shouldFailWhenDurationIsZero() {
        validFilm.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Фильм с нулевой продолжительностью должен иметь нарушения валидации");
    }
}
