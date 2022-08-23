package com.beam.facebook.controller;

import com.beam.facebook.model.GenericResponse;
import com.beam.facebook.model.Post;
import com.beam.facebook.model.User;
import com.beam.facebook.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.security.Principal;
import java.util.List;

import static com.beam.facebook.service.UserService.SESSION_USER;

@RequiredArgsConstructor
@RestController
@RequestMapping("user")
public class UserController {

    private final UserService userService;

    @PostMapping("save")
    public User save(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @DeleteMapping
    public void delete(@PathVariable String id) {
        userService.deleteUser(id);
    }

    @GetMapping
    public User user(HttpSession session) {
        return (User) session.getAttribute(SESSION_USER);
    }

    @GetMapping("friends")
    public List<User> getFriends(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER);
        return userService.getFriends(user.getId());
    }

    @GetMapping("posts")
    public List<Post> getPosts(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER);
        return userService.getPosts(user.getId());
    }

    @PostMapping("new-post")
    public GenericResponse<Post> newPost(@RequestBody Post post, HttpSession session) {
        return userService.newPost(post, session);
    }


    @PostMapping("login")
    public GenericResponse<User> login(HttpSession session, @RequestParam String username, @RequestParam String password) {
        return userService.validateCredential(username, password, session);
    }

    @PostMapping("send-request")
    public GenericResponse sendFriendRequest(HttpSession session, @RequestParam String username) {
        return userService.sendFriendRequest(session, username);
    }

    @PostMapping("accept-request")
    public GenericResponse acceptFriendRequest(HttpSession session, @RequestParam String id, @RequestParam int state) {
        return userService.acceptFriendRequest(session, id, state);
    }

    @GetMapping("friend-requests")
    public List<User> getFriendRequests(HttpSession session) {
        return userService.getFriendRequests(session);
    }


}
