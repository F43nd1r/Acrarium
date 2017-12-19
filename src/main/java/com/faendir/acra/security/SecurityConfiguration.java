package com.faendir.acra.security;

import com.faendir.acra.sql.model.User;
import com.faendir.acra.sql.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    static {
        SecurityContextHolder.setStrategyName(VaadinSessionSecurityContextHolderStrategy.class.getName());
    }

    @NonNull private final UserManager userManager;

    @Autowired
    public SecurityConfiguration(@NonNull UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    protected void configure(@NonNull AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new AuthenticationProvider() {
            @Nullable
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                if (authentication instanceof UsernamePasswordAuthenticationToken) {
                    User user = userManager.getUser(authentication.getName());
                    if (user == null) {
                        throw new UsernameNotFoundException("Username " + authentication.getName() + " not found");
                    }
                    if (userManager.checkPassword(user, (String) authentication.getCredentials())) {
                        return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
                    }
                    throw new BadCredentialsException("Password mismatch for user " + user.getUsername());
                }
                return null;
            }

            @Override
            public boolean supports(@NonNull Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        });
    }

    @Override
    protected void configure(@NonNull HttpSecurity http) throws Exception {
        http.csrf().disable().headers().disable().anonymous().disable().httpBasic();
    }

    @NonNull
    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return authenticationManager();
    }

    @NonNull
    @Bean
    public static SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @NonNull
    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
