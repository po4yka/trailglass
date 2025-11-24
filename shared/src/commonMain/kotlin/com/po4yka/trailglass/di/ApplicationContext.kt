package com.po4yka.trailglass.di

import me.tatarka.inject.annotations.Qualifier

/**
 * Qualifier for application-level Context (Android only).
 * Helps distinguish application context from activity contexts.
 */
@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE
)
annotation class ApplicationContext
