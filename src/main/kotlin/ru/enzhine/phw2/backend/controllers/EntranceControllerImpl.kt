package ru.enzhine.phw2.backend.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.enzhine.phw2.backend.entities.UserEntity


@Controller
@RequestMapping("/")
class EntranceControllerImpl : EntranceController{
    @GetMapping("/login")
    override fun getLogin(model: Model): String{
        val auth = SecurityContextHolder.getContext().authentication
        if(!hasRole(auth, "Anonymous")){
            model.addAttribute("loginAlready", true)
        }
        return "login.html"
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_CUSTOMER')")
    @GetMapping(path = ["/menu", "/"])
    override fun getRedirect(model: Model): String{
        val auth = SecurityContextHolder.getContext().authentication
        return if(hasRole(auth, UserEntity.Role.ADMIN)){
            "redirect:/admin/"
        }else{
            "redirect:/user/"
        }
    }

    fun hasRole(authentication: Authentication, role: UserEntity.Role): Boolean {
        return hasRole(authentication, role.toString())
    }
    fun hasRole(authentication: Authentication, specRole: String): Boolean {
        return authentication.authorities.any { it.authority.equals("ROLE_${specRole.uppercase()}") }
    }
}