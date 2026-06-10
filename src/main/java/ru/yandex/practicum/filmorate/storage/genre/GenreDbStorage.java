package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAll() {
        return jdbcTemplate.query("SELECT * FROM genres ORDER BY id", this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> getById(int id) {
        List<Genre> result = jdbcTemplate.query(
                "SELECT * FROM genres WHERE id = ?", this::mapRowToGenre, id);
        return result.stream().findFirst();
    }

    @Override
    public List<Genre> getByFilmId(int filmId) {
        return jdbcTemplate.query(
                "SELECT g.* FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id",
                this::mapRowToGenre, filmId);
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("id"), rs.getString("name"));
    }
}
