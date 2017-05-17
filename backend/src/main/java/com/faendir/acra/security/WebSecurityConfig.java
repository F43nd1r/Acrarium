package com.faendir.acra.security;

import com.faendir.acra.data.App;
import com.faendir.acra.data.AppManager;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    static {
        SecurityContextHolder.setStrategyName(VaadinSessionSecurityContextHolderStrategy.class.getName());
    }

    private final String user;
    private final String password;
    private final AppManager appManager;
    private final AuthenticationProvider authenticationProvider = new AuthenticationProvider() {
        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                if (user.equals(authentication.getName())) {
                    if (password.equals(authentication.getCredentials())) {
                        return new UsernamePasswordAuthenticationToken(authentication.getName(), authentication.getCredentials(), AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
                    } else {
                        throw new BadCredentialsException("Password mismatch for user " + authentication.getName());
                    }
                }
                App app = appManager.getApp(authentication.getName());
                if (app != null) {
                    if (app.getPassword().equals(authentication.getCredentials())) {
                        Authentication auth = new UsernamePasswordAuthenticationToken(authentication.getName(), authentication.getCredentials(), AuthorityUtils.createAuthorityList("ROLE_REPORTER"));
                        VaadinSession session = VaadinSession.getCurrent();
                        if (session == null) session = new VaadinSession(VaadinService.getCurrent());
                        VaadinSession.setCurrent(session);
                        SecurityContext securityContext = new SecurityContextImpl();
                        securityContext.setAuthentication(auth);
                        session.setAttribute(SecurityContext.class, securityContext);
                        return auth;
                    } else {
                        throw new BadCredentialsException("Password mismatch for user " + authentication.getName());
                    }
                } else {
                    throw new UsernameNotFoundException("Username " + authentication.getName() + " not found");
                }
            }
            return null;
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        }
    };

    @Autowired
    public WebSecurityConfig(@Value("${security.user.name}") String user, @Value("${security.user.password}") String password, AppManager appManager) {
        this.user = user;
        this.password = password;
        this.appManager = appManager;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .headers().disable()
                .httpBasic();
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return authenticationManager();
    }
}
