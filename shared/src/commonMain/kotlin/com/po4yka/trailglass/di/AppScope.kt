package com.po4yka.trailglass.di

import me.tatarka.inject.annotations.Scope

/**
 * Application-level scope for singleton dependencies. Dependencies annotated with @AppScope will be created once per
 * application instance.
 */
@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class AppScope
