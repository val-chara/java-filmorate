package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final UserDbStorage userDbStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
        this.userDbStorage = (UserDbStorage) userStorage;
    }

    public User create(User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя пустое, установлен логин: {}", user.getLogin());
        }
        User created = userStorage.add(user);
        log.info("Добавлен новый пользователь: {}", created);
        return created;
    }

    public User update(User user) {
        getByIdOrThrow(user.getId());
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User updated = userStorage.update(user);
        log.info("Обновлён пользователь: {}", updated);
        return updated;
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        return getByIdOrThrow(id);
    }

    public void addFriend(int userId, int friendId) {
        getByIdOrThrow(userId);
        getByIdOrThrow(friendId);
        userDbStorage.addFriend(userId, friendId);
        log.info("Пользователь id={} добавил в друзья id={}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        getByIdOrThrow(userId);
        getByIdOrThrow(friendId);
        userDbStorage.removeFriend(userId, friendId);
        log.info("Пользователь id={} удалил из друзей id={}", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        User user = getByIdOrThrow(userId);
        return userDbStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        getByIdOrThrow(userId);
        getByIdOrThrow(otherId);
        return userDbStorage.getCommonFriends(userId,otherId);
    }

    private User getByIdOrThrow(int id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    private void validateUser(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Логин содержит пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }
}
