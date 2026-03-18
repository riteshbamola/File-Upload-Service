package com.ritesh.scalablefileupload.repo;

import com.ritesh.scalablefileupload.model.RefreshToken;
import com.ritesh.scalablefileupload.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);

    Optional<RefreshToken> findByUser(User user);
}
