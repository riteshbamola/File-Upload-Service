package com.ritesh.scalablefileupload.repo;

import com.ritesh.scalablefileupload.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepo extends JpaRepository<File,Long> {

}
