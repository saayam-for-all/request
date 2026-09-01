package org.sfa.request.repository;

import org.sfa.request.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query("SELECT u.timeZone FROM User u WHERE u.userId = :userId")
    Optional<String> findTimeZoneByUserId(@Param("userId") String userId);
}