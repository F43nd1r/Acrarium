package com.faendir.acra.mongod.user;

import com.faendir.acra.mongod.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Lukas
 * @since 20.05.2017
 */
interface UserRepository extends MongoRepository<User, String> {
}
