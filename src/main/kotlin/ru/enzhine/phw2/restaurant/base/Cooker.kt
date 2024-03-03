package ru.enzhine.phw2.restaurant.base

interface Cooker<ID> {
    /**
     * @return floating-point number in range from 0 to 1 inclusive,
     * representing current progress.
     */
    fun progress(): Double

    /**
     * @return cooker unique id
     */
    fun cookerID(): ID

    /**
     * @return userid for whom cooker is working.
     */
    fun relatedUserID(): Long

    /**
     * @return whether cooker began.
     */
    fun isBegan(): Boolean

    /**
     * Initiates cooking process.
     */
    fun begin()

    /**
     * Cancels cooking process.
     */
    fun cancel()
}