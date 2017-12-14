package com.faendir.acra.sql.user;

import com.faendir.acra.sql.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Lukas
 * @since 20.05.2017
 */
interface UserRepository extends JpaRepository<User, String> {
}
