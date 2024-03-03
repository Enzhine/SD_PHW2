package ru.enzhine.phw2.restaurant.entities

import ru.enzhine.phw2.utils.MutablePair
import java.util.*

/**
 * Light wrapper for
 * orders creation
 */
class ShopCart {
    private val cart: MutableList<MutablePair<Long, Int>> = LinkedList<MutablePair<Long, Int>>()

    operator fun iterator() = cart.iterator()

    fun clear() {
        cart.clear()
    }

    fun isEmpty(): Boolean {
        return cart.isEmpty()
    }

    fun removeDish(id: Long): Boolean {
        return cart.removeIf { it.first == id}
    }

    fun plusDish(id: Long, count: Int, maxCount: Int): Boolean{
        val pair = cart.find { it.first == id }
        if(pair == null){
            if(count > 0){
                cart.add(MutablePair(id, count))
                return true
            }
            return false
        }
        pair.second += count
        if(pair.second > maxCount){
            pair.second = maxCount
            return false
        }else if(pair.second <= 0){
            cart.remove(pair)
        }
        return true
    }
}
