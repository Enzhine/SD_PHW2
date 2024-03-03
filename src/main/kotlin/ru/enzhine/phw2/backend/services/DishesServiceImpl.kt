package ru.enzhine.phw2.backend.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.enzhine.phw2.backend.entities.DishEntity
import ru.enzhine.phw2.backend.repositories.CrudDishesRepository

@Service
class DishesServiceImpl : DishesService {
    @Autowired
    private lateinit var dishesRepo: CrudDishesRepository

    override fun updateDishQuantity(dish: DishEntity, quantity: Int): DishEntity {
        return dishesRepo.save(DishEntity(dish.id, dish.name, quantity, dish.cost, dish.timeMs))
    }

    override fun createDish(name: String, quantity: Int, cost: Int, timeMs: Int) = dishesRepo.save(DishEntity(null, name, quantity, cost, timeMs))
}