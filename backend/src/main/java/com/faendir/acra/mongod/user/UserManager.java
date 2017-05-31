package com.faendir.acra.mongod.user;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.mongod.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Component
public class UserManager {
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    private final UserRepository userRepository;
    private final DataManager dataManager;
    private final String defaultUser;
    private final String defaultPassword;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserManager(UserRepository userRepository, DataManager dataManager, @Value("${security.user.name}") String defaultUser, @Value("${security.user.password}") String defaultPassword) {
        this.userRepository = userRepository;
        this.dataManager = dataManager;
        this.defaultUser = defaultUser;
        this.defaultPassword = defaultPassword;
        passwordEncoder = new BCryptPasswordEncoder();
    }

    private void ensureValidPermissions(User user) {
        user.getPermissions().removeIf(permission -> dataManager.getApp(permission.getApp()) == null);
        dataManager.getApps().stream().filter(app -> user.getPermissions().stream().noneMatch(permission -> permission.getApp().equals(app.getId())))
                .forEach(app -> user.getPermissions().add(new Permission(app.getId(), user.getRoles().contains(ROLE_ADMIN) ? Permission.Level.ADMIN : Permission.Level.NONE)));
    }

    public User getUser(String username) {
        User user = userRepository.findOne(username);
        if (user == null && defaultUser.equals(username)) {
            user = getDefaultUser();
        }
        if(user != null) {
            ensureValidPermissions(user);
        }
        return user;
    }

    public boolean createUser(String username, String password) {
        if (userRepository.exists(username)) {
            return false;
        }
        User user = new User(username, passwordEncoder.encode(password), Collections.singleton(ROLE_USER));
        ensureValidPermissions(user);
        userRepository.save(user);
        return true;
    }

    public boolean checkPassword(User user, String password) {
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }

    public boolean changePassword(User user, String oldPassword, String newPassword) {
        if (checkPassword(user, oldPassword)) {
            user.setPassword(newPassword);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void setAdmin(User user, boolean admin) {
        if (admin) user.getRoles().add(ROLE_ADMIN);
        else user.getRoles().remove(ROLE_ADMIN);
        userRepository.save(user);
    }

    public User setPermission(User user, String app, Permission.Level level) {
        Optional<Permission> permission = user.getPermissions().stream().filter(p -> p.getApp().equals(app)).findAny();
        if (permission.isPresent()) {
            permission.get().setLevel(level);
        } else {
            user.getPermissions().add(new Permission(app, level));
        }
        return userRepository.save(user);
    }

    public boolean hasPermission(User user, String app, Permission.Level level) {
        ensureValidPermissions(user);
        Optional<Permission> optional = user.getPermissions().stream().filter(permission -> permission.getApp().equals(app)).findAny();
        return optional.isPresent() && optional.get().getLevel().ordinal() >= level.ordinal();
    }

    public List<User> getUsers() {
        List<User> users = userRepository.findAll();
        if (users.stream().noneMatch(user -> user.getUsername().equals(defaultUser))) {
            users.add(getDefaultUser());
        }
        users.forEach(this::ensureValidPermissions);
        return users;
    }

    private User getDefaultUser() {
        return new User(defaultUser, passwordEncoder.encode(defaultPassword), Arrays.asList(ROLE_USER, ROLE_ADMIN));
    }
}
