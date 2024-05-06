package com.example.users_project;

import java.util.regex.Pattern;
import java.time.LocalDate;

public class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    public static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidFirstName(String firstName) {
        return firstName != null && !firstName.isEmpty() && firstName.matches("[a-zA-Z]+");
    }

    public static boolean isValidLastName(String lastName) {
        return lastName != null && !lastName.isEmpty() && lastName.matches("[a-zA-Z]+");
    }
}
