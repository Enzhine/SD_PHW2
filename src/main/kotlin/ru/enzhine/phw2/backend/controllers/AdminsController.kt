package ru.enzhine.phw2.backend.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import ru.enzhine.phw2.backend.entities.DishEntity
import ru.enzhine.phw2.backend.entities.OrderEntity
import ru.enzhine.phw2.backend.entities.UserEntity
import ru.enzhine.phw2.backend.repositories.CrudDishRatingsRepository
import ru.enzhine.phw2.backend.repositories.CrudDishesRepository
import ru.enzhine.phw2.backend.repositories.CrudOrdersRepository
import ru.enzhine.phw2.backend.repositories.CrudUsersRepository
import ru.enzhine.phw2.backend.services.DishesService
import ru.enzhine.phw2.backend.services.UsersService
import java.util.LinkedList
import kotlin.math.cos

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin")
class AdminsController {

    @Autowired
    private lateinit var usersService: UsersService

    @Autowired
    private lateinit var ratesRepository: CrudDishRatingsRepository

    @Autowired
    private lateinit var usersRepository: CrudUsersRepository

    @Autowired
    private lateinit var dishesRepository: CrudDishesRepository

    @Autowired
    private lateinit var ordersRepository: CrudOrdersRepository

    @Autowired
    private lateinit var dishesService: DishesService

    @GetMapping("/")
    fun menu(model: Model): String{
        val auth = SecurityContextHolder.getContext().authentication
        model.addAttribute("username", auth.name)
        return "admin-menu.html"
    }

    @GetMapping("/register")
    fun register(model: Model): String{
        val auth = SecurityContextHolder.getContext().authentication
        model.addAttribute("username", auth.name)
        model.addAttribute("action", "register")
        return "admin-menu.html"
    }

    @PostMapping("/register")
    fun register(@RequestParam username: String,  @RequestParam password: String,@RequestParam role: String, model: Model): String{
        try{
            usersService.registerUser(username, password, UserEntity.Role.valueOf(role.uppercase()))
            model.addAttribute("registerSuccess", username)
        }catch (ex: UsersService.UserAlreadyExistsException){
            model.addAttribute("registerError", username)
        }
        model.addAttribute("action", "register")
        return "admin-menu.html"
    }

    @GetMapping("/dishes")
    fun menuList(model: Model): String{
        val dishes = dishesRepository.findAll()
        model.addAttribute("dishes", dishes)
        model.addAttribute("action", "dishes")
        return "admin-menu.html"
    }

    @PostMapping("/dishes")
    fun menuList(@RequestParam name: String,  @RequestParam quantity: Int, @RequestParam cost: Int, @RequestParam timeMs: Int, model: Model): String{
        try{
            val ent = dishesService.createDish(name, quantity, cost, timeMs)
            model.addAttribute("action", "dishes")
            model.addAttribute("createSuccess", ent.id)
            return menuList(model)
        }catch (ex: DishEntity.InvalidDishException){
            model.addAttribute("inputError", true)
            return menuList(model)
        }
    }

    @GetMapping("/dishes/{id}")
    fun menuEdit(@PathVariable id: Long?, @RequestParam(required = false, defaultValue = "false") delete: Boolean, model: Model): String{
        if(id == null){
            model.addAttribute("findError", "null")
            return menuList(model)
        }
        val optDish = dishesRepository.findById(id)
        if(optDish.isEmpty){
            model.addAttribute("findError", id)
            return menuList(model)
        }
        if(delete){
            dishesRepository.delete(optDish.get())
            model.addAttribute("deleteSuccess", id)
            return menuList(model)
        }
        model.addAttribute("action", "dish")
        model.addAttribute("dish", optDish.get())
        return "admin-menu.html"
    }

    @PostMapping("/dishes/{id}")
    fun menuEdit(@PathVariable id: Long?, @RequestParam name: String,  @RequestParam quantity: Int, @RequestParam cost: Int, @RequestParam timeMs: Int, model: Model): String{
        if(id == null){
            model.addAttribute("findError", "null")
            return menuList(model)
        }
        val optDish = dishesRepository.findById(id)
        if(optDish.isEmpty){
            model.addAttribute("findError", id)
            return menuList(model)
        }
        try{
            val updated = DishEntity(optDish.get().id, name, quantity, cost, timeMs)
            dishesRepository.save(updated)
            model.addAttribute("updateSuccess", updated.id)
            return menuList(model)
        }catch (ex: DishEntity.InvalidDishException){
            model.addAttribute("inputError", true)
            return menuList(model)
        }
    }

    @GetMapping("/orders")
    fun orders(model: Model): String{
        model.addAttribute("action", "orders")
        val ords: MutableList<OrderOverview> = LinkedList<OrderOverview>()
        var totalCost = 0
        for (ord in ordersRepository.findAll()){
            val optDish = dishesRepository.findById(ord.dishId)
            val optComm = ratesRepository.findByOrderId(ord.id!!)
            val optUser = usersRepository.findById(ord.userId)
            ords.add(OrderOverview(ord.id,
                if(optDish.isPresent) optDish.get().name else "UNKNOWN",
                ord.quantity,
                if(optDish.isPresent) optDish.get().cost * ord.quantity else 0,
                ord.status.toString(),
                if(optUser.isPresent) optUser.get().name else "UNKNOWN",
                if(optComm.isPresent) optComm.get().rate else 0,
                if(optComm.isPresent) optComm.get().comment else ""
            ))
            if(ord.status == OrderEntity.Status.PAID){
                totalCost += (if(optDish.isPresent) optDish.get().cost * ord.quantity else 0)
            }
        }
        model.addAttribute("totalCost", totalCost)
        model.addAttribute("orders", ords)
        return "admin-menu.html"
    }
    /**
     * Simple class for Thymeleaf-engine
     * orders information data transfer
     */
    data class OrderOverview(
        val id: Long,
        val dishName: String,
        val quantity: Int,
        val cost: Int,
        val status: String,
        val userName: String,
        val userRate: Int,
        val userComment: String?
    )
}