package ru.enzhine.phw2.backend.controllers

import org.springframework.ui.Model

interface EntranceController{
    fun getLogin(model: Model): String

    fun getRedirect(model: Model): String
}