package com.ritesh.scalablefileupload.repo;

import com.ritesh.scalablefileupload.model.File;
import com.ritesh.scalablefileupload.model.MultiPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultiPartRepo extends JpaRepository<MultiPart,Integer> {
    List<MultiPart> findByFile(File file);
    MultiPart findByFileAndPartNumber(File fileid, int partNumber);

    void deleteByFile(File file);
}
