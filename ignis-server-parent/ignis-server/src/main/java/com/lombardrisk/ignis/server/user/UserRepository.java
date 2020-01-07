package com.lombardrisk.ignis.server.user;

import com.lombardrisk.ignis.server.user.model.SecUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<SecUser, Long> {

    @Query("select a from SecUser a where lower(username) = lower(:username)")
    SecUser findByUsername(@Param("username") String username);

    @Query("select a from SecUser a")
    List<SecUser> findAll();
}
