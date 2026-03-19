package com.ritesh.scalablefileupload.repo;

import com.ritesh.scalablefileupload.model.MultiPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadPartRepo extends JpaRepository<MultiPart,Integer> {
}
