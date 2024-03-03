package ru.enzhine.phw2.backend.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.enzhine.phw2.backend.entities.UserEntity
import ru.enzhine.phw2.backend.repositories.CrudUsersRepository
import ru.enzhine.phw2.backend.security.UserOrderDetails
import ru.enzhine.phw2.restaurant.RuntimeModerator
import ru.enzhine.phw2.restaurant.ThreadCooker

@Service
class UsersServiceImpl : UserDetailsService, UsersService {
    @Autowired
    private lateinit var usersRepository: CrudUsersRepository

    @Autowired
    private lateinit var passwordEncryptor: PasswordEncoder

    @Autowired
    private lateinit var cookMod: RuntimeModerator
    /*
     * Same as login, but goes
     * on Spring side
     */
    override fun loadUserByUsername(username: String?): UserOrderDetails {
        if(username == null){
            throw UsernameNotFoundException("Username can not be null!")
        }
        val optUser = usersRepository.findByName(username)
        if(optUser.isEmpty) {
            throw UsernameNotFoundException("There are no users with name $username!")
        }
        val user = optUser.get()
        val ud = UserOrderDetails(User.builder().username(user.name).password(user.passHash).roles(user.role.toString()).passwordEncoder{it}.build() as User, user.id!!)
        val cooker = cookMod.getUserCooker(user.id)
        if(cooker != null && cooker is ThreadCooker){
            for(e in cooker.orders){
                ud.getShopCart().plusDish(e.dishId, e.quantity, e.quantity)
            }
        }
        return ud
    }

    override fun registerUser(name: String, explicitPass: String, role: UserEntity.Role): UserEntity {
        val optUser = usersRepository.findByName(name)
        return if(optUser.isEmpty) usersRepository.save(UserEntity(null, name, passwordEncryptor.encode(explicitPass), role))
        else throw UsersService.UserAlreadyExistsException(name)
    }
}