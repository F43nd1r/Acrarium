package com.faendir.acra.mongod.user;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.mongod.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @NotNull private final UserRepository userRepository;
    @NotNull private final DataManager dataManager;
    @NotNull private final String defaultUser;
    @NotNull private final String defaultPassword;
    @NotNull private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserManager(@NotNull UserRepository userRepository, @NotNull DataManager dataManager, @NotNull PasswordEncoder passwordEncoder,
                       @NotNull @Value("${security.user.name}") String defaultUser, @NotNull @Value("${security.user.password}") String defaultPassword) {
        this.userRepository = userRepository;
        this.dataManager = dataManager;
        this.passwordEncoder = passwordEncoder;
        this.defaultUser = defaultUser;
        this.defaultPassword = defaultPassword;
    }

    private void ensureValidPermissions(@NotNull User user) {
        user.getPermissions().removeIf(permission -> dataManager.getApp(permission.getApp()) == null);
        dataManager.getApps().stream().filter(app -> user.getPermissions().stream().noneMatch(permission -> permission.getApp().equals(app.getId())))
                   .forEach(app -> user.getPermissions().add(new Permission(app.getId(), user.getRoles().contains(ROLE_ADMIN) ? Permission.Level.ADMIN : Permission.Level.NONE)));
    }

    @Nullable
    public User getUser(@NotNull String username) {
        Optional<User> user = userRepository.findById(username);
        if (!user.isPresent() && defaultUser.equals(username)) {
            user = Optional.of(getDefaultUser());
        }
        user.ifPresent(this::ensureValidPermissions);
        return user.orElse(null);
    }

    public void createUser(@NotNull String username, @NotNull String password) {
        if (userRepository.existsById(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User(username, passwordEncoder.encode(password), Collections.singleton(ROLE_USER));
        ensureValidPermissions(user);
        userRepository.save(user);
    }

    public boolean checkPassword(@Nullable User user, @NotNull String password) {
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }

    public boolean changePassword(@NotNull User user, @NotNull String oldPassword, @NotNull String newPassword) {
        if (checkPassword(user, oldPassword)) {
            user.setPassword(newPassword);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void setAdmin(@NotNull User user, boolean admin) {
        if (admin) {
            user.getRoles().add(ROLE_ADMIN);
        } else {
            user.getRoles().remove(ROLE_ADMIN);
        }
        userRepository.save(user);
    }

    public void setPermission(@NotNull User user, @NotNull String app, @NotNull Permission.Level level) {
        Optional<Permission> permission = user.getPermissions().stream().filter(p -> p.getApp().equals(app)).findAny();
        if (permission.isPresent()) {
            permission.get().setLevel(level);
        } else {
            user.getPermissions().add(new Permission(app, level));
        }
        userRepository.save(user);
    }

    @NotNull
    public List<User> getUsers() {
        List<User> users = userRepository.findAll();
        if (users.stream().noneMatch(user -> user.getUsername().equals(defaultUser))) {
            users.add(getDefaultUser());
        }
        users.forEach(this::ensureValidPermissions);
        return users;
    }

    @NotNull
    private User getDefaultUser() {
        return new User(defaultUser, passwordEncoder.encode(defaultPassword), Arrays.asList(ROLE_USER, ROLE_ADMIN));
    }
}
