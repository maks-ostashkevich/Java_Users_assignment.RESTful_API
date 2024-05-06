package com.example.users_project;

public class IdGenerator {
    private static long currentId = 0;

    public static synchronized long getNextId() {
        return ++currentId;
    }
}

