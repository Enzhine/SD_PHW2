package ru.enzhine.phw2.backend.services

import ru.enzhine.phw2.backend.entities.DishRatingEntity

interface DishRatingsService {
    /**
     * Creates new dish rating
     * @param orderId existing order id
     * @param rate integer from 1 to 5
     * @param comment any string
     * @return created dish rating entity instance
     */
    fun createDishRating(orderId: Long, rate: Int, comment: String?) : DishRatingEntity
}