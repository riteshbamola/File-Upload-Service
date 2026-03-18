package com.ritesh.scalablefileupload.repo;

import com.ritesh.scalablefileupload.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Integer> {

    User findByUsername(String username);
    Boolean existsUserByUserEmail(String userEmail);
    Optional<User>  findByUserEmail(String userEmail);

}
