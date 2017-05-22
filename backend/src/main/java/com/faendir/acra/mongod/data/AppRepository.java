package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.App;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Lukas
 * @since 22.03.2017
 */
interface AppRepository extends MongoRepository<App, String> {
}
