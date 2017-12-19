package com.faendir.acra.sql.model;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Entity
public class User implements UserDetails {
    @Id private String username;
    @ElementCollection(fetch = FetchType.EAGER) private Set<String> roles;
    @ElementCollection(fetch = FetchType.EAGER) private Set<Permission> permissions;
    private String password;

    @PersistenceConstructor
    User() {
    }

    public User(@NonNull String username, @NonNull String password, @NonNull Collection<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>(roles);
        this.permissions = new HashSet<>();
    }

    @NonNull
    public Set<Permission> getPermissions() {
        return permissions;
    }

    @NonNull
    public Set<String> getRoles() {
        return roles;
    }

    @NonNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.concat(permissions.stream(), roles.stream().map(SimpleGrantedAuthority::new)).collect(Collectors.toList());
    }

    @NonNull
    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    @NonNull
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
