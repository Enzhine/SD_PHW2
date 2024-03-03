package ru.enzhine.phw2.restaurant

import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.enzhine.phw2.backend.entities.OrderEntity
import ru.enzhine.phw2.backend.repositories.CustomCookersRepository
import ru.enzhine.phw2.backend.repositories.CrudOrdersRepository
import ru.enzhine.phw2.backend.services.OrdersService
import ru.enzhine.phw2.restaurant.base.Cooker
import ru.enzhine.phw2.restaurant.base.CookerModerator
import ru.enzhine.phw2.restaurant.base.ShopCartHolder
import ru.enzhine.phw2.restaurant.pattern.CallbackAcceptor
import java.util.*

@Component
class RuntimeModerator : CookerModerator<UUID, Cooker<UUID>>, CallbackAcceptor<ThreadCooker>{

    @Autowired
    lateinit var ordersService: OrdersService

    @Autowired
    lateinit var ordersRepo: CrudOrdersRepository

    @Autowired
    lateinit var cookersRepo: CustomCookersRepository

    // constraint to cooker threads amount
    private val maxCookers: Int = 1

    // Comparator for cookersQueue: more expensive - go earlier.
    private val compareByCost = { c1: ThreadCooker, c2:ThreadCooker -> c1.totalCostCached.compareTo(c2.totalCostCached) }

    // Runtime Cookers working at the moment
    private val cookersWorking: MutableList<ThreadCooker> = LinkedList<ThreadCooker>()
    // Runtime Cookers awaiting to start
    private val cookersQueue: PriorityQueue<ThreadCooker> = PriorityQueue<ThreadCooker>(compareByCost)

    // lock object
    private val sync = Any()

    // Tries to push next prioritized cooker to work
    private fun updateQueue() {
        while(cookersWorking.size < maxCookers && cookersQueue.size != 0){
            val cooker = cookersQueue.poll()
            cookersWorking.add(cooker)
            cooker.begin()
            started(cooker)
        }
    }

    override fun getCooker(cookerId: UUID): ThreadCooker?{
        synchronized(sync){
            var cooker = cookersWorking.find { it.cookerID() == cookerId }
            if(cooker != null){
                return cooker
            }
            cooker = cookersQueue.find { it.cookerID() == cookerId }
            return cooker
        }
    }

    override fun getUserCooker(userId: Long): Cooker<UUID>? {
        synchronized(sync){
            var cooker = cookersWorking.find { it.orders.first().userId == userId }
            if(cooker != null){
                return cooker
            }
            cooker = cookersQueue.find { it.orders.first().userId == userId }
            return cooker
        }
    }

    // helper function
    private fun updateOrdersStatuses(c: ThreadCooker, state: OrderEntity.Status) {
        val it = c.orders.listIterator()
        while(it.hasNext()){
            val ord = it.next()
            it.set(ordersService.updateOrderStatus(ord, state))
        }
    }

    // fired when cookers got to work
    private fun started(c: ThreadCooker){
        updateOrdersStatuses(c, OrderEntity.Status.COOKING)
    }

    // fired when cooker finished working
    override fun callback(t: ThreadCooker) {
        synchronized(sync){
            updateOrdersStatuses(t, OrderEntity.Status.READY)
            cookersWorking.remove(t)
            updateQueue()
        }
    }

    override fun cancel(cookerId: UUID): Boolean {
        synchronized(sync){
            // remove cooker from queue or cooking process
            var optCook = cookersWorking.find { it.cookerID() == cookerId }
            if(optCook != null){
                optCook.interrupt()
                cookersWorking.remove(optCook)
            }else{
                optCook = cookersQueue.find { it.cookerID() ==cookerId }
                if(optCook == null){
                    return false
                }
                cookersQueue.remove(optCook)
            }
            // cancel and update queue
            for (ord in optCook.orders){
                ordersService.cancelOrder(ord)
            }
            updateQueue()
            return true
        }
    }

    override fun accept(shopCartHolder: ShopCartHolder): UUID? {
        if(getUserCooker(shopCartHolder.userID()) != null){
            return null
        }
        if(!ordersService.verifyShopCart(shopCartHolder.getShopCart())){
            return null
        }
        val orders = ordersService.createOrdersFromShopCart(shopCartHolder.getShopCart(), shopCartHolder.userID()).toMutableList()
        val cooker = ThreadCooker(this, ordersService, ordersRepo, UUID.randomUUID(), orders)
        synchronized(sync){
            cookersQueue.add(cooker)
            updateQueue()
        }
        return cooker.cookerID()
    }

    override fun replaceShopCart(shopCartHolder: ShopCartHolder) {
        val cookerOpt = getUserCooker(shopCartHolder.userID()) ?: return
        if(cookerOpt.isBegan()){
            return
        }
        val c = cookerOpt as ThreadCooker
        for(pair in shopCartHolder.getShopCart()){
            val idx = c.orders.indexOfFirst { it.dishId == pair.first }
            if(idx != -1){
                val ord = c.orders[idx]
                if(ord.quantity != pair.second){
                    c.orders[idx] = ordersService.updateOrderQuantity(ord, pair.second)
                    c.updateCached()
                }
            }else{
                val some = c.orders.first()
                val ord = ordersService.createOrder(some.userId, pair.first, pair.second, OrderEntity.Status.INQUEUE)
                c.orders.add(ord)
                c.updateCached()
            }
        }
        cookersQueue.remove(cookerOpt)
        cookersQueue.add(cookerOpt)
    }

    @PreDestroy
    fun saveRuntime() {
        for (c in cookersQueue){
            cookersRepo.save(c.makeMemento())
        }
        for (c in cookersWorking){
            cookersRepo.save(c.makeMemento())
        }
    }

    fun initRuntime() {
        val tempUuid = UUID.randomUUID()
        for (m in cookersRepo.findAll()){
            val c = ThreadCooker(this, ordersService, ordersRepo, tempUuid, mutableListOf())
            c.loadMemento(m)
            if(c.orders.first().status == OrderEntity.Status.INQUEUE){
                cookersQueue.add(c)
            }else if(c.orders.first().status == OrderEntity.Status.COOKING){
                cookersWorking.add(c)
                c.start()
            }
        }
        cookersRepo.deleteAll()
    }
}