package ru.enzhine.phw2.backend.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

interface AdminsController {

    fun getMenu(model: Model): String

    fun getRegister(model: Model): String

    fun postRegister(@RequestParam username: String,  @RequestParam password: String,@RequestParam role: String, model: Model): String

    fun getDishes(model: Model): String

    fun postDishes(@RequestParam name: String,  @RequestParam quantity: Int, @RequestParam cost: Int, @RequestParam timeMs: Int, model: Model): String

    fun getDishesId(@PathVariable id: Long?, @RequestParam(required = false, defaultValue = "false") delete: Boolean, model: Model): String

    fun postDishesId(@PathVariable id: Long?, @RequestParam name: String,  @RequestParam quantity: Int, @RequestParam cost: Int, @RequestParam timeMs: Int, model: Model): String

    fun getOrders(model: Model, @RequestParam mode: String): String
}