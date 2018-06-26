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
package com.faendir.acra.security;

import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * A custom {@link SecurityContextHolderStrategy} that stores the {@link SecurityContext} in the Vaadin Session.
 */
@Configuration
@SuppressWarnings("WeakerAccess")
public class VaadinSessionSecurityContextHolderStrategy implements SecurityContextHolderStrategy {
    static {
        SecurityContextHolder.setStrategyName(VaadinSessionSecurityContextHolderStrategy.class.getName());
    }

    private static VaadinSession getSession() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            session = new VaadinSession(VaadinService.getCurrent());
            VaadinSession.setCurrent(session);
        }
        return session;
    }

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
}
