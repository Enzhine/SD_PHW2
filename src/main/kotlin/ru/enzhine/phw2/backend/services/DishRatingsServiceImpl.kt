package ru.enzhine.phw2.backend.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.enzhine.phw2.backend.entities.DishRatingEntity
import ru.enzhine.phw2.backend.repositories.CrudDishRatingsRepository

@Service
class DishRatingsServiceImpl : DishRatingsService {
    @Autowired
    private lateinit var dishesRatingsRepo: CrudDishRatingsRepository

    override fun createDishRating(orderId: Long, rate: Int, comment: String?): DishRatingEntity {
        return dishesRatingsRepo.save(DishRatingEntity(null, orderId, rate, comment))
    }
}