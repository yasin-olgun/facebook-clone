package com.beam.facebook.controller;

import com.beam.facebook.model.GenericResponse;
import com.beam.facebook.model.Post;
import com.beam.facebook.model.User;
import com.beam.facebook.service.PostService;
import com.beam.facebook.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

import static com.beam.facebook.service.UserService.SESSION_USER;

@RequiredArgsConstructor
@RestController
@RequestMapping("post")
public class PostController {

    private final PostService postService;

    @PostMapping("new")
    public GenericResponse<Post> newPost(@RequestBody Post post, HttpSession session){
     return postService.newPost(post,session);
    }

    @GetMapping
    public Optional<Post> get(@PathVariable String id){
        return postService.getPost(id);
    }

    @GetMapping("all")
    public List<Post> getAll(HttpSession session){

        User user = (User) session.getAttribute(SESSION_USER);
       //TODO session içerisindeki user'dadn post listesi alindi, session guncel olmayabilir kontrol et
        return postService.getAll(user.getPostsId());
    }




}
