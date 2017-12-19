package com.faendir.acra.sql.user;

import com.faendir.acra.config.AcraConfiguration;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.model.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Component
public class UserManager {
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_REPORTER = "ROLE_REPORTER";
    @NonNull private final UserRepository userRepository;
    @NonNull private final AcraConfiguration acraConfiguration;
    @NonNull private final PasswordEncoder passwordEncoder;
    @NonNull private final RandomStringGenerator generator;
    @NonNull private final Log log;

    @Autowired
    public UserManager(@NonNull UserRepository userRepository, @NonNull PasswordEncoder passwordEncoder, @NonNull AcraConfiguration acraConfiguration,
            @NonNull SecureRandom secureRandom) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.acraConfiguration = acraConfiguration;
        this.generator = new RandomStringGenerator.Builder().usingRandom(secureRandom::nextInt).withinRange('0', 'z').filteredBy(Character::isLetterOrDigit).build();
        log = LogFactory.getLog(getClass());
    }

    @Nullable
    public User getUser(@NonNull String username) {
        Optional<User> user = userRepository.findById(username);
        if (!user.isPresent() && acraConfiguration.getUser().getName().equals(username)) {
            user = Optional.of(getDefaultUser());
        }
        return user.orElse(null);
    }

    public void createUser(@NonNull String username, @NonNull String password) {
        if (userRepository.existsById(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        userRepository.save(new User(username, passwordEncoder.encode(password), Collections.singleton(ROLE_USER)));
    }

    public Pair<User, String> createReporterUser() {
        String username;
        do {
            username = generator.generate(16);
        } while (userRepository.existsById(username));
        String password = generator.generate(16);
        return Pair.of(new User(username, passwordEncoder.encode(password), Collections.singleton(ROLE_REPORTER)), password);
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
    private User getDefaultUser() {
        return new User(acraConfiguration.getUser().getName(), passwordEncoder.encode(acraConfiguration.getUser().getPassword()), Arrays.asList(ROLE_USER, ROLE_ADMIN));
    }
}
