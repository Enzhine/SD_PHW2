package ru.enzhine.phw2.restaurant.pattern

/**
 * @param M memento object should store
 * current object state and progress.
 */
interface Mementable<M> {
    /**
     * Saving object state.
     */
    fun makeMemento(): M

    /**
     * Restoring object state.
     */
    fun loadMemento(m: M)
}