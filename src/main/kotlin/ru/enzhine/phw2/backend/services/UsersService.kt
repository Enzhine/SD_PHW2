package ru.enzhine.phw2.backend.services

import ru.enzhine.phw2.backend.entities.UserEntity
import kotlin.jvm.Throws

interface UsersService {
    /**
     * @return new user instance
     * @param name user **unique** name
     * @param explicitPass user explicit not hashed password
     * @param role user expected role
     * @throws UserAlreadyExistsException if already contains user with such name
     */
    @Throws(UserAlreadyExistsException::class)
    fun registerUser(name: String, explicitPass: String, role: UserEntity.Role): UserEntity

    class UserAlreadyExistsException(usName: String): Exception("User with $usName already exists!")
}