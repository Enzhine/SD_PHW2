package ru.enzhine.phw2.backend.services

import ru.enzhine.phw2.backend.entities.OrderEntity
import ru.enzhine.phw2.restaurant.entities.ShopCart

interface OrdersService {

    /**
     * Updates order entity's quantity.
     */
    fun updateOrderQuantity(ord: OrderEntity, q: Int): OrderEntity

    /**
     * Updates order entity's status.
     */
    fun updateOrderStatus(ord: OrderEntity, s: OrderEntity.Status): OrderEntity

    /**
     * Removes order.
     */
    fun cancelOrder(ord: OrderEntity)

    /**
     * @return total 'timeMs' sum of each order entity dish property.
     */
    fun getTotalOrdersCompletionMs(ords: Iterable<OrderEntity>): Int

    /**
     * @return total 'cost' sum of each order entity dish property.
     */
    fun getTotalOrdersCost(ords: Iterable<OrderEntity>): Int

    /**
     * @return whether orders can be created from such shop-cart.
     */
    fun verifyShopCart(cart: ShopCart): Boolean

    /**
     * @return created order entity instance.
     */
    fun createOrder(userId: Long, dishId: Long, quantity: Int, status: OrderEntity.Status): OrderEntity

    /**
     * Creates orders from users shop-cart.
     * @return whether orders creation was completed successfully.
     */
    fun createOrdersFromShopCart(cart: ShopCart, userId: Long): Iterable<OrderEntity>

    /**
     * @return list of unpaid orders.
     */
    fun getReadyOrders(userId: Long): Iterable<OrderEntity>
}