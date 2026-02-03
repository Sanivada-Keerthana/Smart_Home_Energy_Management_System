package com.srems.srems.controller;

import com.srems.srems.model.User;                 // ✅ CORRECT USER
import com.srems.srems.repository.UserRepository;
import com.srems.srems.repository.ApprovalRepository;
import com.srems.srems.model.ApprovalStatus;
import com.srems.srems.model.Role;
import com.srems.srems.model.RolePermissions;
import com.srems.srems.model.Permission;
import com.srems.srems.service.ApprovalService;
import org.springframework.security.core.Authentication;
import jakarta.annotation.Resource;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
public class UserDashboardController {

    private final UserRepository userRepository;
    private final ApprovalRepository approvalRepository;
    private final ApprovalService approvalService;

    // ✅ CONSTRUCTOR INJECTION (REQUIRED)
    public UserDashboardController(UserRepository userRepository, ApprovalRepository approvalRepository, ApprovalService approvalService) {
        this.userRepository = userRepository;
        this.approvalRepository = approvalRepository;
        this.approvalService = approvalService;
    }

    /* ================= DASHBOARD HOME ================= */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {

        if (session.getAttribute("role") == null) {
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "User Dashboard");
        model.addAttribute("headerTitle", "Dashboard");

        setCommonAttributes(model, session);
        applyPermissions(model, session);
        model.addAttribute("activePage", "user/home");

        return "user/dashboard";
    }

    /* ================= DEVICES ================= */
    @GetMapping("/device")
    public String devices(Model model, HttpSession session) {

        setCommonAttributes(model, session);
        applyPermissions(model, session);
        model.addAttribute("headerTitle", "Devices");
        model.addAttribute("activePage", "user/device");

        return "user/dashboard";
    }

    /* ================= ENERGY ================= */
    @GetMapping("/tracking")
    public String energy(Model model, HttpSession session) {

        setCommonAttributes(model, session);
        applyPermissions(model, session);
        model.addAttribute("headerTitle", "Energy");
        model.addAttribute("activePage", "user/tracking");

        return "user/dashboard";
    }

    /* ================= ANALYTICS ================= */
    @GetMapping("/analytics")
    public String analytics(Model model, HttpSession session) {

        setCommonAttributes(model, session);
        model.addAttribute("headerTitle", "Analytics");
        model.addAttribute("activePage", "user/analytics");

        return "user/dashboard";
    }

    /* ================= SCHEDULING ================= */
    @GetMapping("/scheduling")
    public String scheduling(Model model, HttpSession session) {

        setCommonAttributes(model, session);
        model.addAttribute("headerTitle", "Scheduling");
        model.addAttribute("activePage", "user/scheduling");

        return "user/dashboard";
    }

    /* ================= RECOMMENDATIONS ================= */
    @GetMapping("/recommendations")
    public String recommendations(Model model, HttpSession session) {

        setCommonAttributes(model, session);
        model.addAttribute("headerTitle", "Energy Tips");
        model.addAttribute("activePage", "user/recommendations");

        return "user/dashboard";
    }

    /* ================= PROFILE ================= */
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {

        if (session.getAttribute("role") == null) {
            return "redirect:/login";
        }

        try {
            // ensure we have a full user object in session
            Object usr = session.getAttribute("user");
            if (usr == null) {
                // gracefully redirect to login if session expired
                return "redirect:/login";
            }

            // ✅ ADD USER ENTITY TO MODEL
            model.addAttribute("user", usr);

            // compute visible users: FLAT_OWNER -> same flat; SECRETARY -> same block
            try {
                com.srems.srems.model.User logged = (com.srems.srems.model.User) usr;
                if (logged != null) {
                    if (logged.getRole() == com.srems.srems.model.Role.FLAT_OWNER) {
                        if (logged.getFlatId() != null) {
                            var users = userRepository.findByFlat_FlatId(logged.getFlatId());
                            users.removeIf(m -> m.getUserId().equals(logged.getUserId()));
                            model.addAttribute("visibleUsers", users);
                        }
                    } else if (logged.getRole() == com.srems.srems.model.Role.SECRETARY) {
                        if (logged.getBlockId() != null) {
                            var users = userRepository.findByBlockId(logged.getBlockId());
                            users.removeIf(m -> m.getUserId().equals(logged.getUserId()));
                            model.addAttribute("visibleUsers", users);
                        }
                    }
                }
            } catch (Exception ignore) {
                // safe fallback; do not block profile render
            }

            setCommonAttributes(model, session);
            model.addAttribute("headerTitle", "My Profile");
            model.addAttribute("activePage", "user/profile");

            return "user/dashboard";
        } catch (Exception e) {
            // Log to console and show a friendly error on the page instead of white-label
            System.out.println("Error rendering profile: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("profileError", "Failed to load profile: " + e.getMessage());
            // Ensure common attributes still set so dashboard can render
            setCommonAttributes(model, session);
            model.addAttribute("headerTitle", "My Profile");
            model.addAttribute("activePage", "user/profile");
            return "user/dashboard";
        }
    }

    /* ================= PROFILE UPDATE ================= */
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String username,
                                @RequestParam String email,
                                @RequestParam(required = false) String password,
                                @RequestParam(required = false) String confirmPassword,
                                HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        user.setUsername(username);
        user.setEmail(email);

        if (password != null && !password.isBlank()) {
            if (!password.equals(confirmPassword)) {
                throw new RuntimeException("Passwords do not match");
            }
            user.setPassword(password); // 🔐 hash later
        }

        userRepository.save(user);          // ✅ NO RED LINE
        session.setAttribute("user", user); // keep session in sync

        return "redirect:/user/profile";
    }

    /* ================= COMMON MODEL ATTRIBUTES ================= */
    private void setCommonAttributes(Model model, HttpSession session) {

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("role", session.getAttribute("role"));
        model.addAttribute("blockId", session.getAttribute("blockId"));
        model.addAttribute("flatId", session.getAttribute("flatId"));
    }

    private void applyPermissions(Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return;

        var permissions = RolePermissions.get(user.getRole());

        model.addAttribute("canView", permissions.contains(Permission.VIEW_DEVICE));
        model.addAttribute("canAdd", permissions.contains(Permission.ADD_DEVICE));
        model.addAttribute("canUpdate", permissions.contains(Permission.UPDATE_DEVICE));
        model.addAttribute("canDelete", permissions.contains(Permission.DELETE_DEVICE));
        model.addAttribute("canToggle", permissions.contains(Permission.TOGGLE_DEVICE));
        model.addAttribute("canApprove", permissions.contains(Permission.APPROVE_USER));
    }


    @GetMapping("/approvals")
    public String approvals(Model model, HttpSession session) {

        if (session.getAttribute("role") == null) {
            return "redirect:/login";
        }

        setCommonAttributes(model, session);
        applyPermissions(model, session);

        // load approvals assigned to this user
        Long approverId = (Long) session.getAttribute("userId");
        var approvals = approvalRepository.findByApproverUserIdAndStatus(approverId, ApprovalStatus.PENDING);
        model.addAttribute("approvals", approvals);

        model.addAttribute("headerTitle", "Pending Approvals");
        model.addAttribute("activePage", "user/approvals");

        return "user/dashboard";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id, Authentication auth, HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        String approverRole = null;
        if (auth != null && auth.getAuthorities() != null && auth.getAuthorities().iterator().hasNext()) {
            approverRole = auth.getAuthorities().iterator().next().getAuthority();
        } else {
            Object r = session.getAttribute("role");
            approverRole = r != null ? r.toString() : "ROLE_ANONYMOUS";
            System.out.println("Warning: Authentication was null in approve(); using session role: " + approverRole);
        }

        Long approverUserId = (Long) session.getAttribute("userId");

        try {
            approvalService.approve(id, approverRole, approverUserId);
            redirectAttributes.addFlashAttribute("success", "User added");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Approval failed: " + e.getMessage());
        }

        return "redirect:/user/approvals";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id, HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        Long approverUserId = (Long) session.getAttribute("userId");
        try {
            approvalService.reject(id, approverUserId);
            redirectAttributes.addFlashAttribute("success", "User rejected successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Reject failed: " + e.getMessage());
        }

        return "redirect:/user/approvals";
    }

}