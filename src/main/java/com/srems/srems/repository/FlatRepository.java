package com.srems.srems.repository;

import com.srems.srems.model.Flat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlatRepository extends JpaRepository<Flat, Long> {
    Optional<Flat> findByBlock_BlockIdAndFlatNumber(Long blockId, String flatNumber);

    boolean existsByBlock_BlockIdAndFlatNumber(Long blockId, String flatNumber);

    List<Flat> findByBlock_BlockId(Long blockId);


}