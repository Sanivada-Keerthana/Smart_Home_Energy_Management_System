package com.srems.srems.service;
import com.srems.srems.model.Approval;
import com.srems.srems.model.ApprovalStatus;
import com.srems.srems.repository.ApprovalRepository;
import com.srems.srems.model.User;
import com.srems.srems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;

@Service
public class ApprovalService {

    @Autowired
    private ApprovalRepository approvalRepo;

    @Autowired
    private UserRepository userRepository;

    /**
     * Approve a pending request. Only the assigned approver can act.
     * approverRole: e.g., ROLE_FLAT_OWNER, ROLE_SECRETARY, ROLE_ADMIN
     * approverUserId: id of the user performing the approval (from session)
     */
    public void approve(Long approvalId, String approverRole, Long approverUserId) {

        Approval a = approvalRepo.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        if (!a.getApproverUserId().equals(approverUserId)) {
            throw new AccessDeniedException("Not assigned to you");
        }

        String targetRole = a.getRequestedUser().getRole().name();

        if ("ROLE_FLAT_OWNER".equals(approverRole) &&
                List.of("FLAT_MEMBER","FLAT_GUEST").contains(targetRole)) {

            approveLogic(a);
            return;
        }

        if ("ROLE_SECRETARY".equals(approverRole) &&
                List.of("SECURITY","STAFF").contains(targetRole)) {

            approveLogic(a);
            return;
        }

        if ("ROLE_ADMIN".equals(approverRole)) {
            approveLogic(a);
            return;
        }

        throw new AccessDeniedException("Not allowed");
    }

    private void approveLogic(Approval a) {
        a.setStatus(ApprovalStatus.APPROVED);

        User u = a.getRequestedUser();
        u.setApproved(true);
        u.setActive(true);
        userRepository.save(u);

        approvalRepo.save(a);
    }

    public void reject(Long approvalId, Long approverUserId) {
        Approval a = approvalRepo.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        if (!a.getApproverUserId().equals(approverUserId)) {
            throw new AccessDeniedException("Not assigned to you");
        }

        a.setStatus(ApprovalStatus.REJECTED);

        User u = a.getRequestedUser();
        u.setApproved(false);
        u.setActive(false);
        userRepository.save(u);

        approvalRepo.save(a);
    }
}