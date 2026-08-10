package evidence_verification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String test() {
        return "Evidence Verification Backend is Running!";
    }

    @GetMapping("/secure")
    public ResponseEntity<Map<String, String>> secureTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (authentication != null) ? authentication.getName() : "Unknown";

        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome, you are authenticated!");
        response.put("user", userEmail);

        return ResponseEntity.ok(response);
    }
}