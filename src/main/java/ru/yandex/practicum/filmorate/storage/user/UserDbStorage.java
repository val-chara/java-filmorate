package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.context.annotation.Primary;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@Primary
@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().intValue());
        log.debug("Добавлен пользователь в БД: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?",
                user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        log.debug("Обновлён пользователь в БД: {}", user);
        return user;
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public List<User> getAll() {
        List<User> users = jdbcTemplate.query("SELECT * FROM users", this::mapRowToUser);
        users.forEach(u -> u.getFriends().addAll(getFriendIds(u.getId())));
        return users;
    }

    @Override
    public Optional<User> getById(int id) {
        List<User> result = jdbcTemplate.query("SELECT * FROM users WHERE id = ?", this::mapRowToUser, id);
        if (result.isEmpty()) return Optional.empty();
        User user = result.get(0);
        user.getFriends().addAll(getFriendIds(id));
        return Optional.of(user);
    }

    public void addFriend(int userId, int friendId) {
        jdbcTemplate.update(
                "MERGE INTO friendships (user_id, friend_id, confirmed) VALUES (?, ?, ?)",
                userId, friendId, false);
    }

    public void removeFriend(int userId, int friendId) {
        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM users u JOIN friendships f ON u.id = f.friend_id WHERE f.user_id = ?";
        List<User> friends = jdbcTemplate.query(sql, this::mapRowToUser, userId);
        friends.forEach(u -> u.getFriends().addAll(getFriendIds(u.getId())));
        return friends;
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id AND f1.user_id = ? " +
                "JOIN friendships f2 ON u.id = f2.friend_id AND f2.user_id = ?";
        List<User> friends = jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
        friends.forEach(u -> u.getFriends().addAll(getFriendIds(u.getId())));
        return friends;
    }

    private List<Integer> getFriendIds(int userId) {
        return jdbcTemplate.query(
                "SELECT friend_id FROM friendships WHERE user_id = ?",
                (rs, rn) -> rs.getInt("friend_id"), userId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        Date birthday = rs.getDate("birthday");
        if (birthday != null) user.setBirthday(birthday.toLocalDate());
        return user;
    }
}
