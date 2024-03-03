package ru.enzhine.phw2.backend.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.enzhine.phw2.backend.psqlgen.PsqlBD
import ru.enzhine.phw2.psqlgen.annotations.PsqlTyped
import ru.enzhine.phw2.backend.services.UsersService
import kotlin.jvm.Throws

@Table(UserEntity.TABLE_NAME)
data class UserEntity @Throws(InvalidUserException::class) constructor(
    @Id
    @PsqlTyped("serial8", true)
    val id: Long?,
    @PsqlTyped("varchar(16)", true)
    val name: String,
    @PsqlTyped("varchar(72)", true)
    @Column("pass_hash")
    val passHash: String,
    @PsqlTyped("varchar(8)", true)
    val role: Role
){
    companion object {
        const val TABLE_NAME = "users"
    }
    // Forced 8 char-length limit, due to
    // lack of enumToInt and IntToEnum converter!
    enum class Role {
        ADMIN,
        CUSTOMER;
    }
    init {
        if(name.isBlank()){
            throw InvalidUserException(this, "name must not be blank!")
        }
    }
    class InvalidUserException(instance: UserEntity, exactReason: String) : RuntimeException("User $instance can not be created, because $exactReason")
}