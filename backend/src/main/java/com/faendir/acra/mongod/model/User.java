package com.faendir.acra.mongod.model;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Document
public class User implements UserDetails {
    @NotNull @Id private final String username;
    @NotNull private final Set<String> roles;
    @NotNull private final Set<Permission> permissions;
    @NotNull private String password;

    @PersistenceConstructor
    private User(@NotNull String username, @NotNull String password, @NotNull Set<String> roles, @NotNull Set<Permission> permissions) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.permissions = permissions;
    }

    public User(@NotNull String username, @NotNull String password, @NotNull Collection<String> roles) {
        this(username, password, new HashSet<>(roles), new HashSet<>());
    }

    @NotNull
    public Set<Permission> getPermissions() {
        return permissions;
    }

    @NotNull
    public Set<String> getRoles() {
        return roles;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    @NotNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.concat(permissions.stream(), roles.stream().map(SimpleGrantedAuthority::new)).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public String getPassword() {
        return password;
    }

    @NotNull
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
