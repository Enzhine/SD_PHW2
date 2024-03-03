package ru.enzhine.phw2.backend.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.enzhine.phw2.psqlgen.annotations.PsqlTyped

@Table(OrderEntity.TABLE_NAME)
data class OrderEntity(
    @Id
    @PsqlTyped("serial8", true)
    val id: Long?,
    @PsqlTyped("bigint", true)
    @Column("user_id")
    val userId: Long,
    @PsqlTyped("bigint", true)
    @Column("dish_id")
    val dishId: Long,
    @PsqlTyped("int",true)
    val quantity: Int,
    @PsqlTyped("varchar(8)", true)
    val status: Status
){
    companion object {
        const val TABLE_NAME = "orders"
    }
    // Forced 8 char-length limit, due to
    // lack of enumToInt and IntToEnum converter!
    enum class Status {
        INQUEUE,
        COOKING,
        READY,
        PAID;
    }
}
