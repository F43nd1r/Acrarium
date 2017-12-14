package com.faendir.acra.security;

import com.faendir.acra.sql.data.DataManager;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.User;
import com.faendir.acra.sql.user.UserManager;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Optional;

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

    @NonNull private final DataManager dataManager;
    @NonNull private final UserManager userManager;

    @Autowired
    public SecurityConfiguration(@NonNull DataManager dataManager, @NonNull UserManager userManager) {
        this.dataManager = dataManager;
        this.userManager = userManager;
    }

    @Override
    protected void configure(@NonNull AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new AuthenticationProvider() {
            @Nullable
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                if (authentication instanceof UsernamePasswordAuthenticationToken) {
                    Optional<App> appOptional = dataManager.getApp(authentication.getName());
                    User user;
                    if (appOptional.isPresent()) {
                        App app = appOptional.get();
                        if (app.getPassword().equals(authentication.getCredentials())) {
                            return getGrantedToken(authentication.getName(), app.getPassword(), AuthorityUtils.createAuthorityList("ROLE_REPORTER"));
                        } else {
                            throwBadCredentials(authentication.getName());
                        }
                    } else if ((user = userManager.getUser(authentication.getName())) != null) {
                        if (userManager.checkPassword(user, (String) authentication.getCredentials())) {
                            return getGrantedToken(user.getUsername(), user.getPassword(), user.getAuthorities());
                        } else {
                            throwBadCredentials(user.getUsername());
                        }
                    } else {
                        throw new UsernameNotFoundException("Username " + authentication.getName() + " not found");
                    }
                }
                return null;
            }

            @Override
            public boolean supports(@NonNull Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        });
    }

    private void throwBadCredentials(@NonNull String username) {
        throw new BadCredentialsException("Password mismatch for user " + username);
    }

    @NonNull
    private Authentication getGrantedToken(@NonNull String username, @NonNull String password, @NonNull Collection<? extends GrantedAuthority> authorities) {
        Authentication auth = new UsernamePasswordAuthenticationToken(username, password, authorities);
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) session = new VaadinSession(VaadinService.getCurrent());
        VaadinSession.setCurrent(session);
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(auth);
        session.setAttribute(SecurityContext.class, securityContext);
        return auth;
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
