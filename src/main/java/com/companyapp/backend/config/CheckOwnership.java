package com.companyapp.backend.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotace pro označení parametrů (UUID nebo DTO),
 * u kterých se má provést kontrola vlastnictví (IDOR štít).
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckOwnership {
}
