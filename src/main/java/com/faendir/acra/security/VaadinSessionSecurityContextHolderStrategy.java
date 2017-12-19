package com.faendir.acra.security;

import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * A custom {@link SecurityContextHolderStrategy} that stores the {@link SecurityContext} in the Vaadin Session.
 */
@SuppressWarnings("WeakerAccess")
public class VaadinSessionSecurityContextHolderStrategy implements SecurityContextHolderStrategy {
    @Override
    public void clearContext() {
        getSession().setAttribute(SecurityContext.class, null);
    }

    @Override
    public SecurityContext getContext() {
        VaadinSession session = getSession();
        SecurityContext context = session.getAttribute(SecurityContext.class);
        if (context == null) {
            context = createEmptyContext();
            session.setAttribute(SecurityContext.class, context);
        }
        return context;
    }

    @Override
    public void setContext(SecurityContext context) {
        getSession().setAttribute(SecurityContext.class, context);
    }

    @NonNull
    @Override
    public SecurityContext createEmptyContext() {
        return new SecurityContextImpl();
    }

    private static VaadinSession getSession() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            session = new VaadinSession(VaadinService.getCurrent());
            VaadinSession.setCurrent(session);
        }
        return session;
    }
}
