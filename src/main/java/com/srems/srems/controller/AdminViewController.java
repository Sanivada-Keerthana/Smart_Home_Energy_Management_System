package com.srems.srems.controller;

import com.srems.srems.model.SystemSettings;
import com.srems.srems.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;



@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {

        model.addAttribute("pageTitle","Admin Dashboard");
        model.addAttribute("headerTitle","Dashboard");
        model.addAttribute("username",session.getAttribute("username"));
        model.addAttribute("role", session.getAttribute("role"));

        model.addAttribute("stats",adminService.getDashboardStats());
        model.addAttribute("activePage","admin/home");

        return "admin/dashboard";
    }

    @GetMapping("/approvals")
    public String approvals(Model model, HttpSession session) {

        model.addAttribute("pageTitle","Approvals");
        model.addAttribute("headerTitle","Approvals");
        model.addAttribute("username",session.getAttribute("username"));
        model.addAttribute("role", session.getAttribute("role"));

        model.addAttribute("approvals",adminService.getPendingApprovals());
        model.addAttribute("activePage","admin/approvals");

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model, HttpSession session) {

        model.addAttribute("pageTitle","Users");
        model.addAttribute("headerTitle","Users");
        model.addAttribute("username",session.getAttribute("username"));
        model.addAttribute("role", session.getAttribute("role"));

        model.addAttribute("users",adminService.getAllUsers());
        model.addAttribute("activePage","admin/users");

        return "admin/dashboard";
    }

    @GetMapping("/aptsetup")
    public String aptSetup(Model model, HttpSession session) {

        model.addAttribute("pageTitle","Apartment Setup");
        model.addAttribute("headerTitle","Apartment Setup");
        model.addAttribute("username",session.getAttribute("username"));
        model.addAttribute("role", session.getAttribute("role"));

        model.addAttribute("blocks", adminService.getAllBlocks());
        model.addAttribute("flats", adminService.getAllFlats());
        model.addAttribute("commonAreas", adminService.getAllCommonAreas());

        model.addAttribute("activePage","admin/aptsetup");
        return "admin/dashboard";
    }

    @GetMapping("/energy")
    public String energy(Model model, HttpSession session){
        model.addAttribute("pageTitle","Energy Overview");
        model.addAttribute("headerTitle","Energy");
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("role", session.getAttribute("role"));
        model.addAttribute("activePage","admin/energy");
        return "admin/dashboard";
    }

    @GetMapping("/settings")
    public String settings(Model model, HttpSession session) {
        try {
            model.addAttribute("pageTitle","System Settings");
            model.addAttribute("headerTitle","Settings");
            model.addAttribute("username",session.getAttribute("username"));
            model.addAttribute("role", session.getAttribute("role"));

            model.addAttribute("settings", adminService.getSettings());
            model.addAttribute("activePage","admin/settings");

            return "admin/dashboard";
        } catch (Exception e) {
            // Graceful fallback to avoid white label; display error on page
            model.addAttribute("pageTitle","System Settings");
            model.addAttribute("headerTitle","Settings");
            model.addAttribute("username",session.getAttribute("username"));
            model.addAttribute("role", session.getAttribute("role"));
            model.addAttribute("activePage","admin/settings");
            model.addAttribute("error", "Failed to load settings: " + e.getMessage());
            return "admin/dashboard";
        }
    }


    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {

        model.addAttribute("pageTitle", "My Profile");
        model.addAttribute("headerTitle", "Profile");

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("role", session.getAttribute("role"));

        // Fetch logged-in user
        model.addAttribute("user",
                adminService.getUserByUsername(
                        (String) session.getAttribute("username")
                )
        );

        model.addAttribute("activePage", "admin/profile");

        return "admin/dashboard";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword,
            HttpSession session
    ) {

        adminService.updateProfile(
                (String) session.getAttribute("username"),
                username,
                email,
                password,
                confirmPassword
        );

        // Update session username if changed
        session.setAttribute("username", username);

        return "redirect:/admin/profile";
    }

    @PostMapping("/settings/save")
    public String saveSettings(@ModelAttribute SystemSettings settings, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        adminService.saveSettings(settings);
        redirectAttributes.addFlashAttribute("success", "Settings saved successfully");
        return "redirect:/admin/settings";
    }

    @PostMapping("/flat")
    public String addFlat(@RequestParam Long blockId,
                        @RequestParam String flatNumber) {

        adminService.createFlat(blockId, flatNumber);
        return "redirect:/admin/aptsetup";
    }

    @GetMapping("/flat/toggle/{id}")
    public String toggleFlat(@PathVariable Long id) {
        // logic later
        return "redirect:/admin/aptsetup";
    }

    @PostMapping("/block")
    public String addBlock(@RequestParam String blockName) {
        adminService.createBlock(blockName);
        return "redirect:/admin/aptsetup";
    }

   @PostMapping("/common-area")
    public String addCommonArea(@RequestParam String areaName) {
        adminService.createCommonArea(areaName);
        return "redirect:/admin/aptsetup";
    }

    @PostMapping("/approve/{approvalId}")
    public String approveUser(@PathVariable Long approvalId, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            adminService.approveUser(approvalId);
            redirectAttributes.addFlashAttribute("success", "User approved successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Approval failed: " + e.getMessage());
        }
        return "redirect:/admin/approvals";
    }

    @PostMapping("/reject/{approvalId}")
    public String rejectUser(@PathVariable Long approvalId, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            adminService.rejectUser(approvalId);
            redirectAttributes.addFlashAttribute("success", "User rejected successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Reject failed: " + e.getMessage());
        }
        return "redirect:/admin/approvals";
    }
    
    @GetMapping("/users/disable/{id}")
    public String disableUser(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            adminService.disableUser(id);
            redirectAttributes.addFlashAttribute("success", "User disabled successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Disable failed: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/enable/{id}")
    public String enableUser(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            adminService.enableUser(id);
            redirectAttributes.addFlashAttribute("success", "User enabled successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Enable failed: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

}