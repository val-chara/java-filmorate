package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().intValue());
        updateGenres(film);
        log.debug("Добавлен фильм в БД: {}", film);
        return getById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
        jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?",
                film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null, film.getId());
        updateGenres(film);
        log.debug("Обновлён фильм в БД: {}", film);
        return getById(film.getId()).orElse(film);
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public List<Film> getAll() {
        List<Film> films = jdbcTemplate.query("SELECT f.*, m.name AS mpa_name FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id", this::mapRowToFilm);
        films.forEach(f -> {
            f.setGenres(getGenresByFilmId(f.getId()));
            f.getLikes().addAll(getLikesByFilmId(f.getId()));
        });
        return films;
    }

    @Override
    public Optional<Film> getById(int id) {
        List<Film> result = jdbcTemplate.query(
                "SELECT f.*, m.name AS mpa_name FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?",
                this::mapRowToFilm, id);
        if (result.isEmpty()) return Optional.empty();
        Film film = result.get(0);
        film.setGenres(getGenresByFilmId(id));
        film.getLikes().addAll(getLikesByFilmId(id));
        return Optional.of(film);
    }

    public void addLike(int filmId, int userId) {
        jdbcTemplate.update("MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    public List<Film> getPopular(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id ORDER BY COUNT(fl.user_id) DESC LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        films.forEach(f -> {
            f.setGenres(getGenresByFilmId(f.getId()));
            f.getLikes().addAll(getLikesByFilmId(f.getId()));
        });
        return films;
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update("MERGE INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId());
            }
        }
    }

    private List<Genre> getGenresByFilmId(int filmId) {
        return jdbcTemplate.query(
                "SELECT g.* FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id",
                (rs, rn) -> new Genre(rs.getInt("id"), rs.getString("name")), filmId);
    }

    private List<Integer> getLikesByFilmId(int filmId) {
        return jdbcTemplate.query(
                "SELECT user_id FROM film_likes WHERE film_id = ?",
                (rs, rn) -> rs.getInt("user_id"), filmId);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        int mpaId = rs.getInt("mpa_id");
        if (!rs.wasNull()) {
            film.setMpa(new Mpa(mpaId, rs.getString("mpa_name")));
        }
        return film;
    }
}
