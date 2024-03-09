package ru.enzhine.phw2.backend.controllers

import org.springframework.ui.Model
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*

interface UsersController {

    fun getMenu(model: Model): String

    fun getShopCartId(@PathVariable id: Long?, @RequestParam act: String?, model: Model): String

    fun postShopCart(model: Model): String

    fun getShopCart(model: Model): String

    fun getOrderExtraId(@PathVariable id: Long?, model: Model): String

    fun getOrder(@RequestParam(required = false, defaultValue = "false", name = "delete") delete: Boolean, model: Model): String

    fun getProceed( model: Model): String

    fun postProceed(@RequestBody body: MultiValueMap<String, String>,  model: Model): String
}