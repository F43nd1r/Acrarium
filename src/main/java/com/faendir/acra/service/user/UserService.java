package com.faendir.acra.service.user;

import com.faendir.acra.config.AcraConfiguration;
import com.faendir.acra.dataprovider.QueryDslDataProvider;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QUser;
import com.faendir.acra.model.User;
import com.faendir.acra.util.PlainTextUser;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
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
    private static final QUser USER = QUser.user;
    @NonNull private final AcraConfiguration acraConfiguration;
    @NonNull private final PasswordEncoder passwordEncoder;
    @NonNull private final RandomStringGenerator randomStringGenerator;
    @NonNull private final EntityManager entityManager;

    @Autowired
    public UserService(@NonNull PasswordEncoder passwordEncoder, @NonNull AcraConfiguration acraConfiguration, @NonNull RandomStringGenerator randomStringGenerator,
            @NonNull EntityManager entityManager) {
        this.passwordEncoder = passwordEncoder;
        this.acraConfiguration = acraConfiguration;
        this.randomStringGenerator = randomStringGenerator;
        this.entityManager = entityManager;
    }

    @Nullable
    public User getUser(@NonNull String username) {
        User user = new JPAQuery<>(entityManager).from(USER).where(USER.username.eq(username)).select(USER).fetchOne();
        if (user == null && acraConfiguration.getUser().getName().equals(username)) {
            user = getDefaultUser();
        }
        return user;
    }

    @Transactional
    @PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public void createUser(@NonNull String username, @NonNull String password) {
        if (new JPAQuery<>(entityManager).from(USER).where(USER.username.eq(username)).fetchFirst() != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        entityManager.persist(new User(username, passwordEncoder.encode(password), Collections.singleton(User.Role.USER)));
    }

    public PlainTextUser createReporterUser() {
        String username;
        do {
            username = randomStringGenerator.generate(16);
        } while (new JPAQuery<>(entityManager).from(USER).where(USER.username.eq(username)).fetchFirst() != null);
        String password = randomStringGenerator.generate(16);
        return new PlainTextUser(username, password, passwordEncoder.encode(password), Collections.singleton(User.Role.REPORTER));
    }

    public boolean checkPassword(@Nullable User user, @NonNull String password) {
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional
    public boolean changePassword(@NonNull User user, @NonNull String oldPassword, @NonNull String newPassword) {
        if (checkPassword(user, oldPassword)) {
            user.setPassword(newPassword);
            entityManager.merge(user);
            return true;
        }
        return false;
    }

    @Transactional
    @PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public void setAdmin(@NonNull User user, boolean admin) {
        if (admin) {
            user.getRoles().add(User.Role.ADMIN);
        } else {
            user.getRoles().remove(User.Role.ADMIN);
        }
        entityManager.merge(user);
    }

    @Transactional
    @PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public void setPermission(@NonNull User user, @NonNull App app, @NonNull Permission.Level level) {
        Optional<Permission> permission = user.getPermissions().stream().filter(p -> p.getApp().equals(app)).findAny();
        if (permission.isPresent()) {
            permission.get().setLevel(level);
        } else {
            user.getPermissions().add(new Permission(app, level));
        }
        entityManager.merge(user);
    }

    @NonNull
    private User getDefaultUser() {
        return new User(acraConfiguration.getUser().getName(), passwordEncoder.encode(acraConfiguration.getUser().getPassword()), Arrays.asList(User.Role.USER, User.Role.ADMIN));
    }

    public QueryDslDataProvider<User> getUserProvider() {
        return new QueryDslDataProvider<>(new JPAQuery<>(entityManager).from(USER).where(USER.roles.any().eq(User.Role.USER)).select(USER));
    }
}
