package ru.enzhine.phw2.psqlgen.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
/**
 * Expected to be used on data-classes.
 * Can be combined with [org.springframework.data.annotation.Id] for key-column
 * and [org.springframework.data.relational.core.mapping.Column] for
 * columns name-mapping.
 * @param type should be an existing PostgresSQL type
 */
annotation class PsqlTyped(val type: String, val notNull: Boolean=false)