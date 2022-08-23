package com.beam.facebook.controller;

import com.beam.facebook.model.GenericResponse;
import com.beam.facebook.model.User;
import com.beam.facebook.service.DiskService;
import com.beam.facebook.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

import static com.beam.facebook.service.UserService.SESSION_USER;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final DiskService diskService;

    @GetMapping(value = "login")
    public String login() {
        return "index";
    }

    @GetMapping(value = "register")
    public String register() {
        return "index";
    }

    @GetMapping(value = {"/", "/wall"})
    public String index(HttpSession session) {
        if (session.getAttribute(SESSION_USER) == null) {
            return "redirect:/login";
        } else {
            return "index"; // yeniden session ekleme yapilmasi gerekli mi?
        }
    }

    @GetMapping("logout")
    public String logout(HttpSession session) {
        session.removeAttribute(SESSION_USER);
        return "redirect:/login";
    }

    @GetMapping("/img/{filename}")
    public ResponseEntity<byte[]> image(@PathVariable String filename,HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER);
        Optional<User> u = userService.findById(filename);
        filename = u.get().getAvatar();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity(diskService.read(filename), headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.of(null);
        }
    }


}
