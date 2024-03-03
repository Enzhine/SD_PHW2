package ru.enzhine.phw2.restaurant

import org.slf4j.LoggerFactory
import ru.enzhine.phw2.backend.entities.CookerEntity
import ru.enzhine.phw2.backend.entities.OrderEntity
import ru.enzhine.phw2.backend.repositories.CrudOrdersRepository
import ru.enzhine.phw2.backend.services.OrdersService
import ru.enzhine.phw2.restaurant.pattern.CallbackAcceptor
import ru.enzhine.phw2.restaurant.base.Cooker
import ru.enzhine.phw2.restaurant.pattern.Mementable
import java.time.Duration
import java.time.Instant
import java.util.UUID

class ThreadCooker(
    private val callbackAcceptor: CallbackAcceptor<ThreadCooker>,
    private val ordersService: OrdersService,
    private val ordersRepo: CrudOrdersRepository,
    private var uuid: UUID,
    var orders: MutableList<OrderEntity>
) : Thread(), Mementable<CookerEntity>, Cooker<UUID> {

    companion object{
        internal val LOG = LoggerFactory.getLogger(ThreadCooker::class.java)
    }

    private var tickedMillis = 0
    private var totalMillisCached: Int = ordersService.getTotalOrdersCompletionMs(orders)
    var totalCostCached: Int = ordersService.getTotalOrdersCost(orders)
        private set

    // delta-time measurement
    private lateinit var lastTick: Instant
    private val deltaTime: Int
        get() {
            val now = Instant.now()
            val delta = Duration.between(lastTick, now)
            lastTick = now
            return delta.toMillis().toInt()
        }

    /**
     * Forces to recalculate total cost and total millis.
     */
    fun updateCached() {
        totalMillisCached =  ordersService.getTotalOrdersCompletionMs(orders)
        totalCostCached =  ordersService.getTotalOrdersCost(orders)
    }

    // cooks for totalMillisCached milliseconds
    private fun cook(): Boolean{
        tickedMillis += deltaTime
        return tickedMillis < totalMillisCached
    }

    // basic behavior implementation
    override fun run() {
        LOG.info("$uuid:$tickedMillis:$totalMillisCached")
        lastTick = Instant.now()
        while (cook()){
            if(interrupted()){
                return
            }
            try{
                sleep(1)
            }catch (_: InterruptedException) {
                return
            }
        }
        finishCooking()
        LOG.info("$uuid:$tickedMillis:$totalMillisCached")
    }

    // fires when finished cooking
    private fun finishCooking() {
        callbackAcceptor.callback(this)
    }

    override fun progress(): Double {
        return tickedMillis.toDouble() / totalMillisCached
    }

    override fun cookerID(): UUID {
        return uuid
    }

    override fun relatedUserID(): Long {
        return orders.first().userId
    }

    override fun isBegan(): Boolean {
        return state != State.NEW
    }

    override fun begin() {
        start()
    }

    override fun cancel() {
        try{
            interrupt()
        }catch (_: InterruptedException) {}
    }

    // interface implementation
    override fun makeMemento(): CookerEntity {
        return CookerEntity(uuid.toString(), tickedMillis, orders.first().userId)
    }

    // interface implementation
    override fun loadMemento(m: CookerEntity) {
        uuid = UUID.fromString(m.uuid)
        tickedMillis = m.timeCooked
        orders = ordersRepo.findByUserId(m.userId).toMutableList()
        updateCached()
    }
}