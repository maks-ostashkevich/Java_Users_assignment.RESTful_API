package com.example.users_project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    //passed
    @Test
    void testCreateUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users");
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setContent("{\"data\": {\"firstName\":\"John\", \"lastName\":\"Doe\", \"birthDate\":\"1990-01-01\", \"email\":\"john@example.com\"}}".getBytes());

        ResponseEntity<String> response = userController.createUser(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User created successfully.", response.getBody());

        verify(userRepository, times(1)).addUser(any(User.class)); // Проверяем только что пользователь был добавлен
    }

    // с несколькими элементами проверить
    // чтобы проверяло, что нельзя по той же почте создать другого пользователя
    //passed
    @Test
    void testUpdateUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users");
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setContent("{\"data\": {\"firstName\":\"John\", \"lastName\":\"Doe\", \"birthDate\":\"1990-01-01\", \"email\":\"john@example.com\"}}".getBytes());

        User user = new User();
        user.setId(1);
        user.setEmail("max@gmail.com");
        user.setFirstName("Max");
        user.setLastName("Ost");
        user.setBirthDate(LocalDate.parse("1996-12-01"));

        userRepository = new UserRepository();
        userController = new UserController();

        userRepository.addUser(user);
        userController.setUserRepository(userRepository);

        ResponseEntity<String> response = userController.updateUser(1, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User updated successfully.", response.getBody());

        // verify(userRepository, times(1)).addUser(any(User.class)); // Проверяем только что пользователь был добавлен
    }

    //passed
    @Test
    void testUpdateUserFields() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users/{1}");
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setContent("{\"data\": {\"firstName\":\"John\"}}".getBytes());

        User user = new User();
        user.setId(1);
        user.setEmail("max@gmail.com");
        user.setFirstName("Max");
        user.setLastName("Ost");
        user.setBirthDate(LocalDate.parse("1996-12-01"));

        userRepository = new UserRepository();
        userController = new UserController();

        userRepository.addUser(user);
        userController.setUserRepository(userRepository);

        ResponseEntity<String> response = userController.updateUserFields(1, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("The fields are updated succesfully.", response.getBody());
    }

    //passed
    @Test
    void testGetAllUsers() {
        userRepository = new UserRepository();
        userController = new UserController();

        // userRepository.addUser(user);
        userController.setUserRepository(userRepository);

        ResponseEntity<List<User>> response = userController.getAllUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new ArrayList<User>(), response.getBody());
    }

    //passed
    @Test
    void testGetUserWhenOneElement() {
        User user = new User();
        user.setId(1);
        user.setEmail("max@gmail.com");
        user.setFirstName("Max");
        user.setLastName("Ost");
        user.setBirthDate(LocalDate.parse("1996-12-01"));

        userRepository = new UserRepository();
        userController = new UserController();

        userRepository.addUser(user);
        userController.setUserRepository(userRepository);

        ResponseEntity<User> response = userController.getUserById(1);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    //passed
    @Test
    void testGetFirstUserWhenEmpty() {
        userRepository = new UserRepository();
        userController = new UserController();

        userController.setUserRepository(userRepository);

        assertThrows(UserNotFoundException.class, () -> {
            ResponseEntity<User> response = userController.getUserById(1);
        });
    }
}