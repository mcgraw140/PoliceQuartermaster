package com.quartermaster.service;

import com.quartermaster.auth.AuthenticatedUser;
import com.quartermaster.dao.UserDao;
import com.quartermaster.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public Optional<AuthenticatedUser> authenticate(String username, String password) {
        Optional<User> userOptional = userDao.findByUsername(username);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            return Optional.empty();
        }

        return Optional.of(new AuthenticatedUser(user.getUserId(), user.getUsername(), user.getRole(), user.getOfficerId()));
    }
}
