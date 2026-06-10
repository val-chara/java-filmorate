package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final FilmDbStorage filmDbStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
        this.filmDbStorage = (FilmDbStorage) filmStorage;
    }

    public Film create(Film film) {
        validateFilm(film);
        Film created = filmStorage.add(film);
        log.info("Добавлен новый фильм: {}", created);
        return created;
    }

    public Film update(Film film) {
        getByIdOrThrow(film.getId());
        validateFilm(film);
        Film updated = filmStorage.update(film);
        log.info("Обновлён фильм: {}", updated);
        return updated;
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        return getByIdOrThrow(id);
    }

    public void addLike(int filmId, int userId) {
        getByIdOrThrow(filmId);
        filmDbStorage.addLike(filmId, userId);
        log.info("Пользователь id={} поставил лайк фильму id={}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        getByIdOrThrow(filmId);
        filmDbStorage.removeLike(filmId, userId);
        log.info("Пользователь id={} удалил лайк с фильма id={}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        return filmDbStorage.getPopular(count);
    }

    private Film getByIdOrThrow(int id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза {} раньше минимальной допустимой {}", film.getReleaseDate(), MIN_RELEASE_DATE);
            throw new ValidationException("Дата релиза не может быть раньше " + MIN_RELEASE_DATE);
        }
    }
}
