package ru.enzhine.phw2.backend.security

import org.springframework.security.core.userdetails.User
import ru.enzhine.phw2.restaurant.base.ShopCartHolder
import ru.enzhine.phw2.restaurant.entities.ShopCart
import java.util.UUID

class UserOrderDetails(user: User, private val uid: Long) : User(
    user.username,
    user.password,
    user.isEnabled,
    user.isAccountNonExpired,
    user.isCredentialsNonExpired,
    user.isAccountNonLocked,
    user.authorities), ShopCartHolder {

    private val shopCart = ShopCart()

    override fun getShopCart(): ShopCart {
        return shopCart
    }

    override fun userID(): Long {
        return uid
    }
}