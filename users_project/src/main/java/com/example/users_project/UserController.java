package com.example.users_project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JavaType;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/users")
@Validated
@ConfigurationProperties(prefix = "user")
public class UserController {
    @Value("${minimumAge}")
    private int minimumAge;
    private UserRepository userList = new UserRepository();

    @GetMapping("")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> allUsers = userList.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }
    @GetMapping("/{id}") // getUserByEmail
    public ResponseEntity<User> getUserById(@PathVariable("id") int id) {
        User user = userList.getUserById(id);
        if (user == null) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("")
    public ResponseEntity<String> createUser(HttpServletRequest request) {
        User user = readUserFromRequestBodyFromData(request);

        if (user == null) {
            throw new InvalidUserException("User object cannot be null");
        }

        if (user.getEmail() == null) {
            throw new InvalidUserException("All user fields must be provided");
        }
        if (userList.getUserByEmail(user.getEmail()) != null) {
            throw new InvalidUserException("User with this email already exists");
        }

        if (user.getFirstName() == null ||
                user.getLastName() == null || user.getBirthDate() == null) {
            throw new InvalidUserException("All user fields must be provided (address and phone are optional).");
        }

        if (!UserValidator.isValidEmail(user.getEmail())) {
            throw new InvalidUserException("Invalid email format");
        }
        if (!UserValidator.isValidFirstName(user.getFirstName())) {
            throw new InvalidUserException("Invalid first name format");
        }
        if (!UserValidator.isValidLastName(user.getLastName())) {
            throw new InvalidUserException("Invalid last name format");
        }

        LocalDate currentDate = LocalDate.now();
        if (user.getBirthDate().plusYears(minimumAge).isAfter(currentDate)) {
            throw new InvalidUserException("User must be at least " + minimumAge + " years old.");
        }

        user.setId(IdGenerator.getNextId());
        userList.addUser(user);
        return ResponseEntity.ok("User created successfully.");
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable("id") int id, HttpServletRequest request) {
        User user = readUserFromRequestBodyFromData(request); // the f() has worked out with this
        if (userList.getUserById(id) == null) {
            throw new UserNotFoundException("User not found with id " + id);
        }
        user.setId(id);
        User userWithTheSameEmail = userList.getUserByEmail(user.getEmail());
        if (userWithTheSameEmail != null && user.getId() != userWithTheSameEmail.getId()) {
            throw new InvalidUserException("Such email already exists.");
        }
        userList.updateUser(user);
        return ResponseEntity.ok("User updated successfully.");
    }
    @PatchMapping("/{id}")
    public ResponseEntity<String> updateUserFields(@PathVariable("id") int id, HttpServletRequest request) {
        Map<String, Object> fieldsToUpdate = readFieldsFromRequestBody(request);

        if (fieldsToUpdate == null || fieldsToUpdate.isEmpty()) {
            throw new InvalidUserException("No fields to update have been sent.");
        }

        User existingUser = userList.getUserById(id);
        if (existingUser == null) {
            throw new UserNotFoundException("User not found with id: " + id);
        }

        fieldsToUpdate.forEach((key, value) -> {
            switch (key) {
                case "firstName":
                    existingUser.setFirstName((String) value);
                    break;
                case "lastName":
                    existingUser.setLastName((String) value);
                    break;
                case "email":
                    User userWithTheSameEmail = userList.getUserByEmail((String) value);
                    if (userWithTheSameEmail != null && existingUser.getId() != userWithTheSameEmail.getId()) {
                        throw new InvalidUserException("Such email already exists.");
                    }
                    existingUser.setEmail((String) value);
                    break;
                case "birthDate":
                    existingUser.setBirthDate(LocalDate.parse((String) value));
                    break;
                case "address":
                    existingUser.setAddress((String) value);
                    break;
                case "phoneNumber":
                    existingUser.setPhoneNumber((String) value);
                    break;
                // Другие поля, которые можно обновлять
                default:
                    throw new InvalidUserFieldException("Invalid user field: " + key);
            }
        });

        // Сохранение обновленного пользователя
        User updatedUser = userList.updateUser(existingUser);
        return ResponseEntity.ok("The fields are updated succesfully.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") int id) {
        userList.deleteUser(id);
        return ResponseEntity.ok("User has been deleted successfully.");
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByBirthDateRange(
            @RequestParam("older") LocalDate older,
            @RequestParam("younger") LocalDate younger) {
        if (older.isAfter(younger)) {
            throw new IncorrectDatesForSearchException("The first date must be earlier than the second.");
        }
        List<User> usersInRange = userList.findUsersByBirthDateBetween(older, younger);
        return ResponseEntity.ok(usersInRange);
    }

    private User readUserFromRequestBody(HttpServletRequest request) {
        try {
            BufferedReader reader = request.getReader();
            StringBuilder requestBodyBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBodyBuilder.append(line);
            }

            String requestBody = requestBodyBuilder.toString();
            if (requestBody == null || requestBody.trim().isEmpty()) {
                throw new InvalidUserException("Request body cannot be empty");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(requestBody, User.class);
        } catch (IOException e) {
            throw new InvalidUserException("Error reading request body");
        }
    }
    private User readUserFromRequestBodyFromData(HttpServletRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            JsonNode rootNode = objectMapper.readTree(request.getInputStream());

            JsonNode dataNode = rootNode.get("data");

            if (dataNode != null) {
                return objectMapper.treeToValue(dataNode, User.class);
            } else {
                throw new InvalidUserException("No user data.");
            }

        } catch (IOException e) {
            throw new InvalidUserException("Error reading request body");
        }
    }
    private Map<String, Object> readFieldsFromRequestBody(HttpServletRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            JsonNode rootNode = objectMapper.readTree(request.getInputStream());

            JsonNode dataNode = rootNode.get("data");

            if (dataNode != null) {
                JavaType mapType = objectMapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class);
                return objectMapper.convertValue(dataNode, mapType);
            } else {
                throw new InvalidUserException("No user data.");
            }

        } catch (IOException e) {
            throw new InvalidUserException("Error reading request body");
        }
    }
}
