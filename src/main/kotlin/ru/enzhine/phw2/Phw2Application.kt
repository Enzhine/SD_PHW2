package ru.enzhine.phw2

import jakarta.annotation.PostConstruct
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import ru.enzhine.phw2.backend.entities.*
import ru.enzhine.phw2.backend.psqlgen.PsqlBD
import ru.enzhine.phw2.backend.services.*
import ru.enzhine.phw2.restaurant.RuntimeModerator
import ru.enzhine.phw2.restaurant.base.Cooker
import ru.enzhine.phw2.restaurant.base.CookerModerator
import java.util.*

@SpringBootApplication
class Phw2Application {

    @Autowired
    private lateinit var usersService: UsersService

    @Autowired
    private lateinit var cookMod: CookerModerator<UUID, Cooker<UUID>>

    @Autowired
    private lateinit var jdbc: JdbcTemplate

    companion object{
        internal val LOG = LoggerFactory.getLogger(Phw2Application::class.java)
    }

    @PostConstruct
    fun initTables(){
        if(jdbc.queryForObject(PsqlBD.hasTable(DishRatingEntity.TABLE_NAME), Boolean::class.java) != true){
            jdbc.execute(PsqlBD.createTable(DishRatingEntity.TABLE_NAME, DishRatingEntity::class.java))
        }
        if(jdbc.queryForObject(PsqlBD.hasTable(CookerEntity.TABLE_NAME), Boolean::class.java) != true){
            jdbc.execute(PsqlBD.createTable(CookerEntity.TABLE_NAME, CookerEntity::class.java))
        }
        (cookMod as RuntimeModerator).initRuntime()
        if(jdbc.queryForObject(PsqlBD.hasTable(DishEntity.TABLE_NAME), Boolean::class.java) != true){
            jdbc.execute(PsqlBD.createTable(DishEntity.TABLE_NAME, DishEntity::class.java))
        }
        if(jdbc.queryForObject(PsqlBD.hasTable(OrderEntity.TABLE_NAME), Boolean::class.java) != true){
            jdbc.execute(PsqlBD.createTable(OrderEntity.TABLE_NAME, OrderEntity::class.java))
        }
        if(jdbc.queryForObject(PsqlBD.hasTable(UserEntity.TABLE_NAME), Boolean::class.java) != true){
            jdbc.execute(PsqlBD.createTable(UserEntity.TABLE_NAME, UserEntity::class.java))
            usersService.registerUser("admin", "admin", UserEntity.Role.ADMIN)
        }
    }
}

fun main(args: Array<String>) {
    Phw2Application.LOG.info("ENABLING APP...")
    runApplication<Phw2Application>(*args)
    Phw2Application.LOG.info("APP ENABLED!")
}