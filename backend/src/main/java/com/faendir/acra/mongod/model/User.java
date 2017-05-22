package com.faendir.acra.mongod.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Document
public class User implements UserDetails {
    @Id
    private String username;
    private String password;
    private Set<String> roles;
    private Set<Permission> permissions;

    public User() {
        permissions = new HashSet<>();
        roles = new HashSet<>();
    }

    public User(String username, String password, Collection<String> roles) {
        this();
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>(roles);
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>(permissions);
        authorities.addAll(roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

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
