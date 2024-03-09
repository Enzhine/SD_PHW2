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

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin")
class AdminsControllerImpl : AdminsController{

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
    override fun getMenu(model: Model): String{
        val auth = SecurityContextHolder.getContext().authentication
        model.addAttribute("username", auth.name)
        return "admin-menu.html"
    }

    @GetMapping("/register")
    override fun getRegister(model: Model): String{
        val auth = SecurityContextHolder.getContext().authentication
        model.addAttribute("username", auth.name)
        model.addAttribute("action", "register")
        return "admin-menu.html"
    }

    @PostMapping("/register")
    override fun postRegister(@RequestParam username: String, @RequestParam password: String, @RequestParam role: String, model: Model): String{
        try{
            usersService.registerUser(username, password, UserEntity.Role.valueOf(role.uppercase()))
            return "redirect:/admin/register?registerSuccess=$username"
        }catch (ex: UsersService.UserAlreadyExistsException){
            return "redirect:/admin/register?registerError=$username"
        }
    }

    @GetMapping("/dishes")
    override fun getDishes(model: Model): String{
        val dishes = dishesRepository.findAll()
        model.addAttribute("dishes", dishes)
        model.addAttribute("action", "dishes")
        return "admin-menu.html"
    }

    @PostMapping("/dishes")
    override fun postDishes(@RequestParam name: String,  @RequestParam quantity: Int, @RequestParam cost: Int, @RequestParam timeMs: Int, model: Model): String{
        try{
            val ent = dishesService.createDish(name, quantity, cost, timeMs)
            return "redirect:/admin/dishes?createSuccess=${ent.id}"
        }catch (ex: DishEntity.InvalidDishException){
            return "redirect:/admin/dishes?inputError=true"
        }
    }

    @GetMapping("/dishes/{id}")
    override fun getDishesId(@PathVariable id: Long?, @RequestParam(required = false, defaultValue = "false") delete: Boolean, model: Model): String{
        if(id == null){
            return "redirect:/admin/dishes?findError=null"
        }
        val optDish = dishesRepository.findById(id)
        if(optDish.isEmpty){
            return "redirect:/admin/dishes?findError=$id"
        }
        if(delete){
            dishesRepository.delete(optDish.get())
            return "redirect:/admin/dishes?deleteSuccess=$id"
        }
        model.addAttribute("action", "dish")
        model.addAttribute("dish", optDish.get())
        return "admin-menu.html"
    }

    @PostMapping("/dishes/{id}")
    override fun postDishesId(@PathVariable id: Long?, @RequestParam name: String,  @RequestParam quantity: Int, @RequestParam cost: Int, @RequestParam timeMs: Int, model: Model): String{
        if(id == null){
            return "redirect:/admin/dishes?findError=null"
        }
        val optDish = dishesRepository.findById(id)
        if(optDish.isEmpty){
            return "redirect:/admin/dishes?findError=$id"
        }
        try{
            val updated = DishEntity(optDish.get().id, name, quantity, cost, timeMs)
            dishesRepository.save(updated)
            return "redirect:/admin/dishes?updateSuccess=${updated.id}"
        }catch (ex: DishEntity.InvalidDishException){
            return "redirect:/admin/dishes?inputError=true"
        }
    }

    @GetMapping("/orders")
    override fun getOrders(model: Model, @RequestParam mode: String): String{
        model.addAttribute("action", "orders")
        model.addAttribute("mode", mode)
        val ords: MutableList<OrderOverview> = LinkedList<OrderOverview>()

        when(mode) {
            "all" -> {
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
            }
            "popular" -> {
                ordersRepository.findAll().groupBy {
                    val optDish = dishesRepository.findById(it.dishId)
                    if(optDish.isPresent){
                        optDish.get().name
                    }else{
                        "UNKNOWN"
                    }
                }.forEach {
                    val optDish = dishesRepository.findById(it.value.first().dishId)
                    if(optDish.isEmpty) return@forEach
                    val quantity =  it.value.sumOf { it2 -> it2.quantity }
                    val ratesSum = it.value.sumOf { it2 ->
                        val optRate = ratesRepository.findByOrderId(it2.id!!)
                        if(optRate.isEmpty){
                            0
                        }else{
                            optRate.get().rate
                        }
                    }
                    ords.add(OrderOverview(0,
                        it.key,
                        quantity,
                        quantity * optDish.get().cost,
                        "PAID",
                        "",
                        ratesSum,
                        null
                        ))
                    ords.sortBy { it2 -> it2.userRate }
                    ords.reverse()
                }
            }
            else -> {
                throw RuntimeException("Unexpected mode!")
            }
        }
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