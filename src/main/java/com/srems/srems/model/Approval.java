package com.srems.srems.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approvals")
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long approvalId;

    // 🔗 USER WHO NEEDS APPROVAL
    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_user_id")
    private User requestedUser;

    // 👤 ADMIN / APPROVER ID
    @Column(nullable = false)
    private Long approverUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime actionAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus;

    /* ---------- CONSTRUCTORS ---------- */

    public Approval() {
        this.status = ApprovalStatus.PENDING;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Approval(User requestedUser, Long approverUserId) {
        this.requestedUser = requestedUser;
        this.approverUserId = approverUserId;
        this.status = ApprovalStatus.PENDING;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /* ---------- GETTERS ---------- */

    public Long getApprovalId() {
        return approvalId;
    }

    public User getRequestedUser() {
        return requestedUser;
    }

    public Long getApproverUserId() {
        return approverUserId;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getActionAt() {
        return actionAt;
    }

    /* ---------- ACTIONS ---------- */

    public void approve() {
        this.status = ApprovalStatus.APPROVED;
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.actionAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = ApprovalStatus.REJECTED;
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.actionAt = LocalDateTime.now();
    }

    public ApprovalStatus getStatusEnum() {
        return this.status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

}