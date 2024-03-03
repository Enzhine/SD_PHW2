package ru.enzhine.phw2.backend.entities

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.enzhine.phw2.psqlgen.annotations.PsqlTyped

@Table(CookerEntity.TABLE_NAME)
data class CookerEntity(
    @Id
    @PsqlTyped("char(36)", true)
    val uuid: String,
    @PsqlTyped("int", true)
    @Column("time_cooked")
    val timeCooked: Int,
    @PsqlTyped("bigint", true)
    @Column("user_id")
    val userId: Long
) : Persistable<String>{
    companion object {
        const val TABLE_NAME = "cookers"
    }

    override fun getId(): String {
        return uuid
    }

    override fun isNew(): Boolean {
        return true
    }
}