package ru.enzhine.phw2.restaurant.base

interface CookerModerator<ID, C: Cooker<ID>> {
    /**
     * Makes cooker from shopCart.
     * @return cooker id if successfully created, null otherwise.
     */
    fun accept(shopCartHolder: ShopCartHolder): ID?

    /**
     * Replaces shopCart of corresponding cooker if such was created.
     */
    fun replaceShopCart(shopCartHolder: ShopCartHolder)

    /**
     * Makes cooker from shopCart.
     * @return cooker id if successfully created, null otherwise.
     */
    fun cancel(cookerId: ID): Boolean

    /**
     * Tries to find created cooker with such id.
     * @return cooker id if such exists, null otherwise.
     */
    fun getCooker(cookerId: ID): Cooker<ID>?

    /**
     * Tries to find created cooker, related to user with such id.
     * @return cooker id if such exists, null otherwise.
     */
    fun getUserCooker(userId: Long): Cooker<ID>?
}