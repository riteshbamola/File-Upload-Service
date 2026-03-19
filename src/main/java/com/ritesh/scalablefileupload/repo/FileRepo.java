package com.ritesh.scalablefileupload.repo;

import com.ritesh.scalablefileupload.model.File;
import com.ritesh.scalablefileupload.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepo extends JpaRepository<File,Long> {

    List<File> findByOwner(User owner);
}
