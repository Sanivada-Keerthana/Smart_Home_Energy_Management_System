package com.srems.srems.repository;

import com.srems.srems.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByBlockName(String blockName);

    boolean existsByBlockName(String blockName);

    Optional<Block> findByBlockNameIgnoreCase(String blockName);
}