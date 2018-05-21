package com.faendir.acra.service.user;

import com.faendir.acra.config.AcraConfiguration;
import com.faendir.acra.dataprovider.BufferedDataProvider;
import com.faendir.acra.dataprovider.ObservableDataProvider;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.User;
import com.faendir.acra.sql.user.UserRepository;
import com.faendir.acra.util.PlainTextUser;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Service
public class UserService implements Serializable {
    @NonNull private final UserRepository userRepository;
    @NonNull private final AcraConfiguration acraConfiguration;
    @NonNull private final PasswordEncoder passwordEncoder;
    @NonNull private final RandomStringGenerator randomStringGenerator;

    @Autowired
    public UserService(@NonNull UserRepository userRepository, @NonNull PasswordEncoder passwordEncoder, @NonNull AcraConfiguration acraConfiguration,
            @NonNull RandomStringGenerator randomStringGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.acraConfiguration = acraConfiguration;
        this.randomStringGenerator = randomStringGenerator;
    }

    @Nullable
    public User getUser(@NonNull String username) {
        Optional<User> user = userRepository.findById(username);
        if (!user.isPresent() && acraConfiguration.getUser().getName().equals(username)) {
            user = Optional.of(getDefaultUser());
        }
        return user.orElse(null);
    }

    @PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public void createUser(@NonNull String username, @NonNull String password) {
        if (userRepository.existsById(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        userRepository.save(new User(username, passwordEncoder.encode(password), Collections.singleton(User.Role.USER)));
    }

    public PlainTextUser createReporterUser() {
        String username;
        do {
            username = randomStringGenerator.generate(16);
        } while (userRepository.existsById(username));
        String password = randomStringGenerator.generate(16);
        return new PlainTextUser(username, password, passwordEncoder.encode(password), Collections.singleton(User.Role.REPORTER));
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

    @PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public void setAdmin(@NonNull User user, boolean admin) {
        if (admin) {
            user.getRoles().add(User.Role.ADMIN);
        } else {
            user.getRoles().remove(User.Role.ADMIN);
        }
        userRepository.save(user);
    }

    @PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
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
    private User getDefaultUser() {
        return new User(acraConfiguration.getUser().getName(), passwordEncoder.encode(acraConfiguration.getUser().getPassword()), Arrays.asList(User.Role.USER, User.Role.ADMIN));
    }

    public ObservableDataProvider<User, Void> getUserProvider() {
        return new BufferedDataProvider<>(acraConfiguration.getPaginationSize(),
                pageable -> userRepository.findAllByRoles(User.Role.USER, pageable),
                () -> userRepository.countAllByRoles(User.Role.USER));
    }
}
