package ru.enzhine.phw2.restaurant.pattern

/**
 * AKA Observer and observable, but lighter version.
 * @param T class, to call back this acceptor
 */
interface CallbackAcceptor<T> {
    /**
     * Notify
     */
    fun callback(t: T)
}