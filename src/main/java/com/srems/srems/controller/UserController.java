package com.srems.srems.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    /* ===== COMMON ENTRY ===== */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {

        String role = (String) session.getAttribute("role");

        if (role == null) {
            return "redirect:/login";
        }

        // Admin goes to admin dashboard
        if ("ADMIN".equals(role)) {
            return "redirect:/admin/dashboard";
        }

        // All others go to SAME user dashboard
        return "redirect:/user/dashboard";
    }

    /* ===== SHARED PROFILE ENTRY ===== */
    @GetMapping("/profile")
    public String profile(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null) {
            return "redirect:/login";
        }

        if ("ADMIN".equals(role)) {
            return "redirect:/admin/profile";
        }

        return "redirect:/user/profile";
    }

}