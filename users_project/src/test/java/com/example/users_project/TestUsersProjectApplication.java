package com.example.users_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestUsersProjectApplication {

    public static void main(String[] args) {
        SpringApplication.from(UsersProjectApplication::main).with(TestUsersProjectApplication.class).run(args);
    }

}
