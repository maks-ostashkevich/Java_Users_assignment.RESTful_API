package com.example.users_project;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class UserRepository {

    private static List<User> userList = new ArrayList<>();

    // Добавление нового пользователя
    public void addUser(User user) {
        userList.add(user);
    }

    // Обновление существующего пользователя
    public User updateUser(User updatedUser) {
        for (User user : userList) {
            if (user.getId() == updatedUser.getId()) {
                user.setEmail(updatedUser.getEmail());
                user.setFirstName(updatedUser.getFirstName());
                user.setLastName(updatedUser.getLastName());
                user.setBirthDate(updatedUser.getBirthDate());
                user.setAddress(updatedUser.getAddress());
                user.setPhoneNumber(updatedUser.getPhoneNumber());
                return user;
            }
        }
        return null;
    }

    public void deleteUser(long id) {
        boolean removalResult = userList.removeIf(user -> user.getId() == id);
        if (!removalResult)
            throw new UserNotFoundException("User not found with id " + id);
    }

    public User getUserByEmail(String email) {
        for (User user : userList) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public User getUserById(long id) {
        for (User user: userList) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        return userList;
    }

    public List<User> findUsersByBirthDateBetween(LocalDate from, LocalDate to) {
        List<User> usersInRange = new ArrayList<>();
        for (User user : userList) {
            if (user.getBirthDate().compareTo(from) >= 0 && user.getBirthDate().compareTo(to) <= 0) {
                usersInRange.add(user);
            }
        }
        return usersInRange;
    }
}
