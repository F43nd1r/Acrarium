package com.faendir.acra.ui.view.annotation;

import com.faendir.acra.sql.model.Permission;
import org.springframework.lang.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Lukas
 * @since 21.06.2017
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresAppPermission {
    @NonNull Permission.Level value();
}
