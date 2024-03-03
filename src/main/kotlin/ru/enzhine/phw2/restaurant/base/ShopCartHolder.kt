package ru.enzhine.phw2.restaurant.base

import ru.enzhine.phw2.restaurant.entities.ShopCart

interface ShopCartHolder {
    /**
     * @return current shop cart.
     */
    fun getShopCart(): ShopCart

    /**
     * @return user id, related to this shopCart.
     */
    fun userID(): Long
}