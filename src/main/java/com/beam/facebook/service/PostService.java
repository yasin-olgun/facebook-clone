package com.beam.facebook.service;

import com.beam.facebook.model.GenericResponse;
import com.beam.facebook.model.Post;
import com.beam.facebook.model.User;
import com.beam.facebook.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.beam.facebook.service.UserService.SESSION_USER;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;


    public GenericResponse<Post> newPost(Post post, HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER);
        List<String> userPosts = user.getPostsId();
        if (post.getId() == null) {
            //new post
            post.setCreationTime(LocalDateTime.now());
            post.setOwnerId(user.getId());
            post.setId(UUID.randomUUID().toString());
            userPosts.add(post.getId());
            post.setOwnerName(user.getFirstName()+" "+user.getLastName());
            postRepository.save(post);
            return new GenericResponse<Post>()
                    .setCode(0)
                    .setData(post);
        } else {
            //update
            userPosts.add(post.getId());
            postRepository.save(post);
            return new GenericResponse<Post>()
                    .setCode(10)
                    .setData(post);
        }

    }

    public Optional<Post> getPost(String id){
       return postRepository.findById(id);
    }

    public List<Post> getAll(List<String> idList){
        List<Post> postList = (List<Post>) postRepository.findAllById(idList);
        if (postList==null){
            return new ArrayList<>();
        }
        return (List<Post>) postRepository.findAllById(idList);
    }

    public void deletePost(String id){
        postRepository.deleteById(id);
    }

}

