package com.faendir.acra.sql.user;

import com.faendir.acra.sql.data.DataManager;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.model.User;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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

    @NonNull private final UserRepository userRepository;
    @NonNull private final DataManager dataManager;
    @NonNull private final String defaultUser;
    @NonNull private final String defaultPassword;
    @NonNull private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserManager(@NonNull UserRepository userRepository, @NonNull DataManager dataManager, @NonNull PasswordEncoder passwordEncoder,
                       @NonNull @Value("${security.user.name}") String defaultUser, @NonNull @Value("${security.user.password}") String defaultPassword) {
        this.userRepository = userRepository;
        this.dataManager = dataManager;
        this.passwordEncoder = passwordEncoder;
        this.defaultUser = defaultUser;
        this.defaultPassword = defaultPassword;
    }

    private void ensureValidPermissions(@NonNull User user) {
        dataManager.getApps().stream().filter(app -> user.getPermissions().stream().noneMatch(permission -> permission.getApp().equals(app)))
                   .forEach(app -> user.getPermissions().add(new Permission(app, user.getRoles().contains(ROLE_ADMIN) ? Permission.Level.ADMIN : Permission.Level.NONE)));
    }

    @Nullable
    public User getUser(@NonNull String username) {
        Optional<User> user = userRepository.findById(username);
        if (!user.isPresent() && defaultUser.equals(username)) {
            user = Optional.of(getDefaultUser());
        }
        user.ifPresent(this::ensureValidPermissions);
        return user.orElse(null);
    }

    public User createUser(@NonNull String username, @NonNull String password) {
        if (userRepository.existsById(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User(username, passwordEncoder.encode(password), Collections.singleton(ROLE_USER));
        ensureValidPermissions(user);
        return userRepository.save(user);
    }

    public boolean checkPassword(@Nullable User user, @NonNull String password) {
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }

    public boolean changePassword(@NonNull User user, @NonNull String oldPassword, @NonNull String newPassword) {
        if (checkPassword(user, oldPassword)) {
            user.setPassword(newPassword);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void setAdmin(@NonNull User user, boolean admin) {
        if (admin) {
            user.getRoles().add(ROLE_ADMIN);
        } else {
            user.getRoles().remove(ROLE_ADMIN);
        }
        userRepository.save(user);
    }

    public void setPermission(@NonNull User user, @NonNull App app, @NonNull Permission.Level level) {
        Optional<Permission> permission = user.getPermissions().stream().filter(p -> p.getApp().equals(app)).findAny();
        if (permission.isPresent()) {
            permission.get().setLevel(level);
        } else {
            user.getPermissions().add(new Permission(app, level));
        }
        userRepository.save(user);
    }

    @NonNull
    public List<User> getUsers() {
        List<User> users = userRepository.findAll();
        if (users.stream().noneMatch(user -> user.getUsername().equals(defaultUser))) {
            users.add(getDefaultUser());
        }
        users.forEach(this::ensureValidPermissions);
        return users;
    }

    @NonNull
    private User getDefaultUser() {
        return new User(defaultUser, passwordEncoder.encode(defaultPassword), Arrays.asList(ROLE_USER, ROLE_ADMIN));
    }
}
