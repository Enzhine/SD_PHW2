package ru.enzhine.phw2.backend.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.enzhine.phw2.psqlgen.annotations.PsqlTyped
import kotlin.jvm.Throws

@Table(DishEntity.TABLE_NAME)
data class DishEntity @Throws(InvalidDishException::class) constructor(
    @Id
    @PsqlTyped("serial8", true)
    val id: Long?,
    @PsqlTyped("varchar(32)", true)
    val name: String,
    @PsqlTyped("int", true)
    val quantity: Int,
    @PsqlTyped("int", true)
    val cost: Int,
    @PsqlTyped("int", true)
    @Column("time_ms")
    val timeMs: Int
){
    companion object {
        const val TABLE_NAME = "dishes"
    }
    init {
        if(name.isBlank()){
            throw InvalidDishException(this, "name must not be blank!")
        }
        if(quantity < 0){
            throw InvalidDishException(this, "quantity must be non-negative value!")
        }
        if(cost <= 0){
            throw InvalidDishException(this, "cost must be positive value!")
        }
        if(timeMs < 0){
            throw InvalidDishException(this, "timeMs must be non-negative value!")
        }
    }
    class InvalidDishException(instance: DishEntity, exactReason: String) : RuntimeException("Dish $instance can not be created, because $exactReason")
}
