package com.itsvitaliio.backend.repositories;

import com.itsvitaliio.backend.models.ImageNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageNodeRepository extends JpaRepository<ImageNode, String> {
}
