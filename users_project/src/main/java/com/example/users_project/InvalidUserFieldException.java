package com.example.users_project;

public class InvalidUserFieldException extends RuntimeException {

    public InvalidUserFieldException(String message) {
        super(message);
    }
}

