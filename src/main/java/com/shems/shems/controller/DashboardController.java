package com.shems.shems.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shems.shems.service.AuthService;
import com.shems.shems.model.User;
import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        base(model, session, "Dashboard", "Overview / Home", "home");
        return "dashboard";
    }

    @GetMapping("/device")
    public String device(Model model, HttpSession session) {
        base(model, session, "Device Management", "Device Management", "device");
        return "dashboard";
    }

    @GetMapping("/tracking")
    public String tracking(Model model, HttpSession session) {
        base(model, session, "Energy Tracking", "Energy Tracking", "tracking");
        return "dashboard";
    }

    @GetMapping("/analytics")
    public String analytics(Model model, HttpSession session) {
        base(model, session, "Analytics", "Analytics", "analytics");
        return "dashboard";
    }

    @GetMapping("/scheduling")
    public String scheduling(Model model, HttpSession session) {
        base(model, session, "Scheduling", "Scheduling", "scheduling");
        return "dashboard";
    }

    @GetMapping("/recommendations")
    public String recommendations(Model model, HttpSession session) {
        base(model, session, "Recommendations", "Recommendations", "recommendations");
        return "dashboard";
    }

    @Autowired
    private AuthService authService;

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = authService.getUserById(userId);

        model.addAttribute("pageTitle", "Profile");
        model.addAttribute("headerTitle", "My Profile");
        model.addAttribute("subTitle", "View and update your profile");
        model.addAttribute("activePage", "profile");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("user", user);

        return "dashboard";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        // Password validation (only if user entered it)
        if (password != null && !password.isBlank()) {
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute(
                        "error", "Passwords do not match");
                return "redirect:/profile";
            }

            if (!authService.isValidPassword(password)) {
                redirectAttributes.addFlashAttribute(
                        "error", "Password must be at least 8 characters with uppercase, lowercase, and digit");
                return "redirect:/profile";
            }
        }

        boolean updated = authService.updateUserProfileWithPassword(
                userId, email, username, password
        );

        if (!updated) {
            redirectAttributes.addFlashAttribute(
                    "error", "Invalid email or update failed");
        } else {
            session.setAttribute("username", username);
            redirectAttributes.addFlashAttribute(
                    "success", "Profile updated successfully");
        }

        return "redirect:/profile";
    }



    private void base(Model model, HttpSession session,
                      String pageTitle, String headerTitle,
                      String activePage) {

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("headerTitle", headerTitle);
        model.addAttribute("subTitle", "Monitor usage • Manage devices • Save energy");
        model.addAttribute("activePage", activePage);

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        model.addAttribute("username", username != null ? username : "User");
        model.addAttribute("role", role != null ? role : "Role");
    }


}
