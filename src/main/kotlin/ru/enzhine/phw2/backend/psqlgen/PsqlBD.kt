package ru.enzhine.phw2.backend.psqlgen

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import ru.enzhine.phw2.psqlgen.annotations.PsqlTyped
import ru.enzhine.phw2.psqlgen.exceptions.PsqlClassException
import kotlin.jvm.Throws
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

object PsqlBD{
    /**
     * Generates SQL-command for PostgresSQL database, that will create
     * a table called [tName] with columns from [clazz]. [clazz] properties
     * should be annotated with corresponding [PsqlTyped] annotations.
     */
    @Throws(PsqlClassException::class)
    fun createTable(tName: String, clazz: Class<*>): String{
        val build = StringBuilder()
        for(prop in clazz.kotlin.memberProperties){
            val annot = prop.annotations.find { it is PsqlTyped } as PsqlTyped?
            annot?: continue
            val id = prop.javaField?.annotations?.find { it is Id } as Id?
            val rename = prop.javaField?.annotations?.find { it is Column } as Column?
            build.append("${rename?.value ?: prop.name} ${annot.type}${if(id != null) " PRIMARY KEY" else ""}${if(annot.notNull) " NOT NULL" else ""}, ")
        }
        if(build.isEmpty()){
            throw PsqlClassException("Class ${clazz.javaClass.name} must contain some properties annotated with ${PsqlTyped::javaClass.name}")
        }
        return "create table \"$tName\"(${build.removeRange(build.length-2, build.length)});"
    }

    /**
     * Generates SQL-command for PostgresSQL database, that will check
     * whether a table called [tName] exists.
     */
    fun hasTable(tName: String): String {
        return "select exists (select from \"pg_tables\" where schemaname = 'public' and tablename = '$tName');"
    }
}