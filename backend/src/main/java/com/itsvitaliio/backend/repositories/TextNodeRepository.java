package com.itsvitaliio.backend.repositories;

import com.itsvitaliio.backend.models.TextNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TextNodeRepository extends JpaRepository<TextNode, String> {
}
