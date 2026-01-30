package com.srems.srems.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blocks")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blockId;

    @Column(nullable = false, unique = true)
    private String blockName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Block() {
        this.createdAt = LocalDateTime.now();
    }

    public Block(String blockName) {
        this.blockName = blockName;
        this.createdAt = LocalDateTime.now();
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}