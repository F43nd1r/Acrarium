/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.service;

import com.faendir.acra.dataprovider.QueryDslDataProvider;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QUser;
import com.faendir.acra.model.User;
import com.querydsl.core.types.dsl.Expressions;
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
import javax.validation.Validator;
import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Service
public class UserService implements Serializable {
    private static final QUser USER = QUser.user;
    @NonNull
    private final PasswordEncoder passwordEncoder;
    @NonNull
    private final RandomStringGenerator randomStringGenerator;
    @NonNull
    private final EntityManager entityManager;
    @NonNull
    private final Validator validator;

    @Autowired
    public UserService(@NonNull PasswordEncoder passwordEncoder, @NonNull RandomStringGenerator randomStringGenerator,
                       @NonNull EntityManager entityManager, @NonNull Validator validator) {
        this.passwordEncoder = passwordEncoder;
        this.randomStringGenerator = randomStringGenerator;
        this.entityManager = entityManager;
        this.validator = validator;
    }

    @Nullable
    public User getUser(@NonNull String username) {
        return new JPAQuery<>(entityManager).from(USER).where(USER.username.eq(username)).select(USER).fetchOne();
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public void createUser(@NonNull String username, @NonNull String password) {
        if (new JPAQuery<>(entityManager).from(USER).where(USER.username.eq(username)).fetchFirst() != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        entityManager.persist(new User(username, passwordEncoder.encode(password), Collections.singleton(User.Role.USER)));
    }
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public User createReporterUser() {
        String username;
        do {
            username = randomStringGenerator.generate(16);
        } while (new JPAQuery<>(entityManager).from(USER).where(USER.username.eq(username)).fetchFirst() != null);
        String password = randomStringGenerator.generate(16);
        return new User(username, password, passwordEncoder.encode(password), Collections.singleton(User.Role.REPORTER));
    }

    public boolean checkPassword(@Nullable User user, @NonNull String password) {
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional
    public User store(@NonNull User user) {
        if(user.hasPlainTextPassword()) {
            user.setPassword(passwordEncoder.encode(user.getPlainTextPassword()));
        }
        return entityManager.merge(user);
    }

    public boolean hasAdmin() {
        return new JPAQuery<>(entityManager).from(USER).where(USER.roles.contains(User.Role.ADMIN)).select(Expressions.ONE).fetchOne() != null;
    }


    @Transactional
    @PreAuthorize("authentication.name == #user.username")
    public boolean changePassword(@NonNull User user, @NonNull String oldPassword, @NonNull String newPassword) {
        if (checkPassword(user, oldPassword)) {
            user.setPassword(passwordEncoder.encode(newPassword));
            entityManager.merge(user);
            return true;
        }
        return false;
    }

    @Transactional
    @PreAuthorize("authentication.name == #user.username")
    public boolean changeMail(@NonNull User user, @Nullable String mail) {
        String oldMail = user.getMail();
        user.setMail(mail);
        if (!validator.validate(user).isEmpty()) {
            user.setMail(oldMail);
            return false;
        }
        entityManager.merge(user);
        return true;
    }


    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public void setAdmin(@NonNull User user, boolean admin) {
        if (admin) {
            user.getRoles().add(User.Role.ADMIN);
        } else {
            user.getRoles().remove(User.Role.ADMIN);
        }
        entityManager.merge(user);
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public void setApiAccess(@NonNull User user, boolean access) {
        if (access) {
            user.getRoles().add(User.Role.API);
        } else {
            user.getRoles().remove(User.Role.API);
        }
        entityManager.merge(user);
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public void setPermission(@NonNull User user, @NonNull App app, @Nullable Permission.Level level) {
        Optional<Permission> permission = user.getPermissions().stream().filter(p -> p.getApp().equals(app)).findAny();
        if (permission.isPresent()) {
            if (level != null) {
                permission.get().setLevel(level);
            } else {
                user.getPermissions().remove(permission.get());
            }
        } else if (level != null) {
            user.getPermissions().add(new Permission(app, level));
        }
        entityManager.merge(user);
    }

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public QueryDslDataProvider<User> getUserProvider() {
        return new QueryDslDataProvider<>(new JPAQuery<>(entityManager).from(USER).where(USER.roles.any().eq(User.Role.USER)).select(USER));
    }
}
