package ru.enzhine.phw2.backend.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.enzhine.phw2.psqlgen.annotations.PsqlTyped

@Table(DishRatingEntity.TABLE_NAME)
data class DishRatingEntity(
    @Id
    @PsqlTyped("serial8", true)
    val id: Long?,
    @PsqlTyped("bigint", true)
    @Column("order_id")
    val orderId: Long,
    @PsqlTyped("int", true)
    val rate: Int,
    @PsqlTyped("varchar(255)", false)
    val comment: String?,
) {
    companion object {
        const val TABLE_NAME = "rates"
    }
    init {
        if(rate !in 1..5){
            throw DishRatingEntity.InvalidDishRatingException(this, "name must not be blank!")
        }
    }
    class InvalidDishRatingException(instance: DishRatingEntity, exactReason: String) : RuntimeException("Dish rating $instance can not be created, because $exactReason")
}