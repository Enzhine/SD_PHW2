package ru.enzhine.phw2.backend.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import ru.enzhine.phw2.backend.entities.OrderEntity
import ru.enzhine.phw2.backend.repositories.CrudDishesRepository
import ru.enzhine.phw2.backend.security.UserOrderDetails
import ru.enzhine.phw2.backend.services.DishRatingsService
import ru.enzhine.phw2.backend.services.OrdersService
import ru.enzhine.phw2.restaurant.base.Cooker
import ru.enzhine.phw2.restaurant.base.CookerModerator
import ru.enzhine.phw2.utils.MutablePair
import java.util.*
import kotlin.collections.HashMap

@Controller
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
@RequestMapping("/user")
class UsersControllerImpl : UsersController{

    @Autowired
    lateinit var ordersService: OrdersService

    @Autowired
    lateinit var dishesRepo: CrudDishesRepository

    @Autowired
    lateinit var ratingsService: DishRatingsService

    @Autowired
    lateinit var cookMod: CookerModerator<UUID, Cooker<UUID>>

    fun orderDetails(): UserOrderDetails = SecurityContextHolder.getContext().authentication.principal as UserOrderDetails

    @GetMapping("/")
    override fun getMenu(model: Model): String{
        val userOrder = orderDetails()
        val cooker = cookMod.getUserCooker(userOrder.userID())
        val unpaid = ordersService.getReadyOrders(userOrder.userID())
        model.addAttribute("hasOrder", cooker != null)
        if(unpaid.count() != 0){
            model.addAttribute("hasOrder", true)
            model.addAttribute("finished", true)
        }else{
            if(cooker == null) {
                model.addAttribute("hasCart", !userOrder.getShopCart().isEmpty())
            }else{
                model.addAttribute("finished", false)
                model.addAttribute("cooker", cooker)
                model.addAttribute("refresh", true)
                model.addAttribute("progress", Math.round(cooker.progress() * 100))
            }
        }
        return "user-menu.html"
    }

    @GetMapping("/shop-cart/{id}")
    override fun getShopCartId(@PathVariable id: Long?, @RequestParam act: String?, model: Model): String{
        if(id == null || act == null){
            return "redirect:/user/shop-cart"
        }
        val dishOpt = dishesRepo.findById(id)
        if(dishOpt.isEmpty){
            return "redirect:/user/shop-cart"
        }
        return when(act){
            "add" -> {
                if(orderDetails().getShopCart().plusDish(id, 1, dishOpt.get().quantity)){
                    "redirect:/user/shop-cart?addSuccess"
                }else{
                    "redirect:/user/shop-cart?addError"
                }
            }
            "rem" -> {
                if(orderDetails().getShopCart().plusDish(id, -1, dishOpt.get().quantity)){
                    "redirect:/user/shop-cart?remSuccess"
                }else{
                    "redirect:/user/shop-cart?remError"
                }
            }
            "remall" -> {
                if(orderDetails().getShopCart().removeDish(dishOpt.get().id!!)){
                    "redirect:/user/shop-cart?remSuccess"
                }else{
                    "redirect:/user/shop-cart"
                }
            }
            else -> "redirect:/user/shop-cart"
        }
    }

    @PostMapping("/shop-cart")
    override fun postShopCart(model: Model): String{
        val userOrder = orderDetails()
        if(userOrder.getShopCart().isEmpty()){
            return "redirect:/user/shop-cart?emptyError=true"
        }
        if(cookMod.accept(userOrder) == null){
            return "redirect:/user/shop-cart?orderError=true"
        }
        return "redirect:/user/"
    }

    @GetMapping("/shop-cart")
    override fun getShopCart(model: Model): String{
        val userOrder = orderDetails()
        model.addAttribute("action", "shop-cart")
        val cartOverview: MutableList<OrderOverview> = LinkedList<OrderOverview>()
        val dishes = dishesRepo.findAll()
        // iterate and delete not present
        val it = userOrder.getShopCart().iterator()
        var updateFlag = false
        var totalCount = 0
        var totalCost = 0
        while (it.hasNext()){
            val pair = it.next()
            val dish = dishes.find { d -> d.id == pair.first }
            if(dish == null || dish.quantity < pair.second){
                // Dish properties could've changed
                updateFlag = true
                it.remove()
                continue
            }
            totalCount += pair.second
            totalCost += dish.cost * pair.second
            cartOverview.add(OrderOverview(dish.id!!, dish.name, pair.second, dish.cost * pair.second))
        }
        // formed the cart overview
        if(updateFlag){
            model.addAttribute("unexpectedChange", true)
        }
        model.addAttribute("hasCart", cartOverview.isNotEmpty())
        model.addAttribute("totalCount", totalCount)
        model.addAttribute("totalCost", totalCost)
        model.addAttribute("cart", cartOverview)
        model.addAttribute("dishes", dishes)
        return "user-menu.html"
    }

    @GetMapping("/order/{id}")
    override fun getOrderExtraId(@PathVariable id: Long?, model: Model): String {
        if(id == null){
            return "redirect:/user/"
        }
        val dishOpt = dishesRepo.findById(id)
        val userOrder = orderDetails()
        val cooker = cookMod.getUserCooker(userOrder.userID())
        if(dishOpt.isEmpty || cooker == null){
            return "redirect:/user/"
        }
        if(userOrder.getShopCart().plusDish(id, 1, dishOpt.get().quantity)){
            cookMod.replaceShopCart(userOrder)
            return "redirect:/user/order?success"
        }else{
            return "redirect:/user/order?error"
        }
    }

    @GetMapping("/order")
    override fun getOrder(@RequestParam(required = false, defaultValue = "false", name = "delete") delete: Boolean, model: Model): String{
        model.addAttribute("refresh", true)
        val userOrder = orderDetails()
        val cooker = cookMod.getUserCooker(userOrder.userID()) ?: return "redirect:/user/"
        if(delete){
            cookMod.cancel(cooker.cookerID())
        }
        if(cooker.isBegan()){
            return "redirect:/user/"
        }
        model.addAttribute("action", "order")
        val cartOverview: MutableList<OrderOverview> = LinkedList<OrderOverview>()
        val dishes = dishesRepo.findAll()
        var totalCount = 0
        var totalCost = 0
        for(pair in userOrder.getShopCart()){
            val dish = dishes.find { d -> d.id == pair.first }
            if(dish == null){
                continue
            }
            totalCount += pair.second
            totalCost += dish.cost * pair.second
            cartOverview.add(OrderOverview(dish.id!!, dish.name, pair.second, dish.cost * pair.second))
        }
        model.addAttribute("totalCount", totalCount)
        model.addAttribute("totalCost", totalCost)
        model.addAttribute("cart", cartOverview)
        model.addAttribute("dishes", dishes)
        return "user-menu.html"
    }

    @GetMapping("/proceed")
    override fun getProceed( model: Model): String{
        val userOrder = orderDetails()
        val unpaid = ordersService.getReadyOrders(userOrder.userID())
        if(unpaid.count() == 0) {
            return "redirect:/user/"
        }
        model.addAttribute("action", "proceed")
        val cartOverview: MutableList<OrderOverview> = LinkedList<OrderOverview>()
        var totalCount = 0
        var totalCost = 0
        for(ord in unpaid){
            val dishOpt = dishesRepo.findById(ord.dishId)
            if(dishOpt.isEmpty){
                continue
            }
            val dish = dishOpt.get()
            totalCount += ord.quantity
            totalCost += ord.quantity * dish.cost
            cartOverview.add(OrderOverview(ord.id!!, dish.name, ord.quantity, dish.cost * ord.quantity))
        }
        model.addAttribute("totalCount", totalCount)
        model.addAttribute("totalCost", totalCost)
        model.addAttribute("cart", cartOverview)
        return "user-menu.html"
    }

    @PostMapping("/proceed")
    override fun postProceed(@RequestBody body: MultiValueMap<String, String>,  model: Model): String{
        val userOrder = orderDetails()
        val unpaid = ordersService.getReadyOrders(userOrder.userID())
        if(unpaid.count() == 0) {
            return "redirect:/user/"
        }
        val former = HashMap<Long, MutablePair<Int, String?>>()
        for (e in body.entries){
            if(e.key.startsWith("rate")){
                val ordId = e.key.substring(4).toLong()
                val rate: Int = e.value.first().substring(0, 1).toInt()
                if(!former.contains(ordId)){
                    former[ordId] = MutablePair(rate,null)
                }else{
                    former[ordId]!!.first = rate
                }
            }else if(e.key.startsWith("comment")){
                val ordId = e.key.substring(7).toLong()
                val comm: String? = e.value.first()
                if(!former.contains(ordId)){
                    former[ordId] = MutablePair(5,comm)
                }else{
                    former[ordId]!!.second = comm
                }
            }
        }
        for (e in former){
            ratingsService.createDishRating(e.key, e.value.first, e.value.second)
        }
        for(ord in unpaid){
            ordersService.updateOrderStatus(ord, OrderEntity.Status.PAID)
        }
        userOrder.getShopCart().clear()
        return "redirect:/user/"
    }

    /**
     * Simple class for Thymeleaf-engine
     * orders information data transfer
     */
    data class OrderOverview(
        val id: Long,
        val name: String,
        val quantity: Int,
        val cost: Int
    )
}