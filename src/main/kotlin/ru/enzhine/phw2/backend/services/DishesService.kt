package ru.enzhine.phw2.backend.services

import ru.enzhine.phw2.backend.entities.DishEntity

interface DishesService {

    /**
     * Updates dish entity's quantity.
     */
    fun updateDishQuantity(dish: DishEntity, quantity: Int): DishEntity

    /**
     * Creates new dish
     * @param name not blank string
     * @param quantity non-negative value
     * @param cost positive value
     * @param timeMs non-negative value
     * @return created dish entity instance
     */
    fun createDish(name: String, quantity: Int, cost: Int, timeMs: Int): DishEntity
}