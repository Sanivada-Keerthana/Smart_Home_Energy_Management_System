package com.srems.srems.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
    name = "flats",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"block_id", "flat_number"})
    }
)
public class Flat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    @Column(name = "flat_number", nullable = false)
    private String flatNumber;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "flat", fetch = FetchType.LAZY)
    private List<User> users;

    

    public Flat() {
        this.createdAt = LocalDateTime.now();
    }

    public Flat(Block block, String flatNumber) {
        this.block = block;
        this.flatNumber = flatNumber;
        this.createdAt = LocalDateTime.now();
    }

    /* ===== DERIVED LOGIC ===== */
    @Transient
    public boolean isOwnerAssigned() {
        return users != null &&
            users.stream()
                    .anyMatch(u -> u.getRole() == Role.FLAT_OWNER);
    }

    // Getters
    public Long getFlatId() { return flatId; }
    public Block getBlock() { return block; }
    public String getFlatNumber() { return flatNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}