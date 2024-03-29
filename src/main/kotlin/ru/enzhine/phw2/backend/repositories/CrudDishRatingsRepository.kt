package ru.enzhine.phw2.backend.repositories

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.enzhine.phw2.backend.entities.DishEntity
import ru.enzhine.phw2.backend.entities.DishRatingEntity
import ru.enzhine.phw2.backend.entities.OrderEntity
import java.util.Optional

/**
 * Will be auto generated by Spring :)
 */
@Repository
interface CrudDishRatingsRepository : CrudRepository<DishRatingEntity, Long> {
    @Query("SELECT * FROM \"${DishRatingEntity.TABLE_NAME}\" WHERE order_id = :orderId")
    fun findByOrderId(orderId: Long): Optional<DishRatingEntity>
}