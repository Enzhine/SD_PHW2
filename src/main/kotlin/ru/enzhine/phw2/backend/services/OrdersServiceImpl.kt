package ru.enzhine.phw2.backend.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.enzhine.phw2.backend.entities.OrderEntity
import ru.enzhine.phw2.backend.repositories.CrudDishesRepository
import ru.enzhine.phw2.backend.repositories.CrudOrdersRepository
import ru.enzhine.phw2.restaurant.entities.ShopCart
import java.util.LinkedList

@Service
class OrdersServiceImpl : OrdersService {
    @Autowired
    private lateinit var ordersRepo: CrudOrdersRepository
    @Autowired
    private lateinit var dishesRepo: CrudDishesRepository
    @Autowired
    private lateinit var dishesService: DishesService

    override fun updateOrderQuantity(ord: OrderEntity, q: Int): OrderEntity {
        val dishOpt = dishesRepo.findById(ord.dishId)
        if(dishOpt.isPresent){
            dishesService.updateDishQuantity(dishOpt.get(), dishOpt.get().quantity-(q-ord.quantity))
        }
        return ordersRepo.save(OrderEntity(ord.id!!, ord.userId, ord.dishId, q, ord.status))
    }

    override fun updateOrderStatus(ord: OrderEntity, s: OrderEntity.Status): OrderEntity {
        return ordersRepo.save(OrderEntity(ord.id!!, ord.userId, ord.dishId, ord.quantity, s))
    }

    override fun cancelOrder(ord: OrderEntity) {
        val dishOpt = dishesRepo.findById(ord.dishId)
        if(dishOpt.isPresent){
            dishesService.updateDishQuantity(dishOpt.get(), dishOpt.get().quantity+ord.quantity)
        }
        ordersRepo.delete(ord)
    }

    override fun getTotalOrdersCompletionMs(ords: Iterable<OrderEntity>): Int {
        var totalTime = 0
        for(ord in ords) {
            val optDish = dishesRepo.findById(ord.dishId)
            if(optDish.isPresent){
                totalTime += ord.quantity * optDish.get().timeMs
            }
        }
        return totalTime
    }

    override fun getTotalOrdersCost(ords: Iterable<OrderEntity>): Int {
        var totalCost = 0
        for(ord in ords) {
            val optDish = dishesRepo.findById(ord.dishId)
            if(optDish.isPresent){
                totalCost += ord.quantity * optDish.get().cost
            }
        }
        return totalCost
    }

    override fun verifyShopCart(cart: ShopCart): Boolean {
        for(p in cart){
            val optDish = dishesRepo.findById(p.first)
            if(optDish.isEmpty){
                return false
            }
            if(optDish.get().quantity < p.second){
                return false
            }
        }
        return true
    }

    override fun createOrder(userId: Long, dishId: Long, quantity: Int, status: OrderEntity.Status): OrderEntity {
        val dishOpt = dishesRepo.findById(dishId)
        if(dishOpt.isPresent){
            dishesService.updateDishQuantity(dishOpt.get(), dishOpt.get().quantity-quantity)
        }
        return ordersRepo.save(OrderEntity(null, userId, dishId, quantity, status))
    }

    override fun createOrdersFromShopCart(cart: ShopCart, userId: Long): Iterable<OrderEntity> {
        val lst = LinkedList<OrderEntity>()
        for(p in cart){
            val optDish = dishesRepo.findById(p.first)
            if(optDish.isEmpty){
                return emptyList()
            }
            if(optDish.get().quantity < p.second){
                return emptyList()
            }
            lst.add(createOrder(userId, p.first, p.second, OrderEntity.Status.INQUEUE))
        }
        return lst
    }

    override fun getReadyOrders(userId: Long): Iterable<OrderEntity> {
        return ordersRepo.findByUserId(userId).filter { it.status == OrderEntity.Status.READY }
    }
}