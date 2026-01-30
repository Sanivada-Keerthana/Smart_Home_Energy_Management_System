package com.srems.srems.controller;

import com.srems.srems.model.User;
import com.srems.srems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpSession;

@Controller
public class DevLoginController {

    @Autowired
    private UserRepository userRepository;

    // Temporary dev helper: set session as a user and redirect to dashboard
    @GetMapping("/dev/login/{id}")
    public String loginAsUser(@PathVariable Long id, HttpSession session) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return "redirect:/login";

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("role", user.getRole().name());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("blockId", user.getBlockId());
        session.setAttribute("flatId", user.getFlat() != null ? user.getFlat().getFlatId() : null);
        session.setAttribute("user", user);

        return "redirect:/dashboard";
    }
}
