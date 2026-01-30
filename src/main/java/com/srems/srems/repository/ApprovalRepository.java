package com.srems.srems.repository;

import com.srems.srems.model.Approval;
import com.srems.srems.model.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    List<Approval> findByStatus(ApprovalStatus status);

    long countByStatus(ApprovalStatus status);

    // Approvals assigned to a particular approver user
    List<Approval> findByApproverUserIdAndStatus(Long approverUserId, ApprovalStatus status);

    List<Approval> findByApproverUserId(Long approverUserId);
}