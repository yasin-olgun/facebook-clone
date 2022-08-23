package com.beam.facebook.service;

import com.beam.facebook.model.GenericResponse;
import com.beam.facebook.model.Post;
import com.beam.facebook.model.User;
import com.beam.facebook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    public static final String SESSION_USER = "user";
    private final UserRepository userRepository;
    private final PostService postService;

    private final DiskService diskService;
    private final SimpMessagingTemplate template;


    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        boolean alreadyTaken = userRepository.findByUsername(user.getUsername()).isPresent();

        if (user.getAvatar() != null && user.getAvatar().startsWith("data:image")) {
            String ext = user.getAvatar().substring(user.getAvatar().indexOf("/") + 1, user.getAvatar().indexOf(";"));
            byte[] data = Base64.getDecoder().decode(user.getAvatar().substring(user.getAvatar().indexOf(",") + 1));
            try {
                user.setAvatar(diskService.write(data, ext));
            } catch (IOException e) {
                user.setAvatar(null);
                e.printStackTrace();
            }
        }


        if (!alreadyTaken) {
            // new user
            user.setId(UUID.randomUUID().toString());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return user;
        }
        if (user.getId() != null) {
            userRepository.save(user);
        }
        return null;
    }

    public GenericResponse<User> validateCredential(String username, String password, HttpSession session) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return new GenericResponse<User>()
                    .setCode(1);
        } else {
            User user = optionalUser.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                session.setAttribute(SESSION_USER, user);

                return new GenericResponse<User>()
                        .setCode(0)
                        .setData(user);
            } else {
                return new GenericResponse<User>()
                        .setCode(2);
            }
        }
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void deleteUserByUsername(String username) {
        Optional<User> u = userRepository.findByUsername(username);
        userRepository.delete(u.get());
    }

    public void deleteUser(String id) {
        Optional<User> u = userRepository.findByUsername(id);
        userRepository.delete(u.get());
        // todo optional presenet kontrol et
    }

    public List<User> getFriends(String userId) {
        Optional<User> user = userRepository.findById(userId);
        return (List<User>) userRepository.findAllById(user.get().getFriendsId());
    }

    public List<User> getFriendRequests(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER);
        return (List<User>) userRepository.findAllById(user.getFriendRequestSenderIds());
    }


    public List<Post> getPosts(String userId) {
        // kullanicinin kendi postlarini getiriyor
        //timeline'da arkadaslarinin da postlarini gorebilmesi icin
        // friendList'inde olan tum kisilerin postlarini return edecegim
        Optional<User> user = userRepository.findById(userId);
        List<Post> myPosts = postService.getAll(user.get().getPostsId());
        List<Post> friendsPosts = getFriendPosts(userId);
        myPosts.addAll(friendsPosts);
        return myPosts;
    }

    public List<Post> getFriendPosts(String userId) {
        Optional<User> user = userRepository.findById(userId);
        List<String> userFriendList = user.get().getFriendsId();
        List<Post> posts = new ArrayList<>();
        for (String friendId : userFriendList) {
            Optional<User> temp = userRepository.findById(friendId);
            posts.addAll(postService.getAll(temp.get().getPostsId()));
        }
        return posts;
    }

    public GenericResponse sendFriendRequest(HttpSession session, String username) {
        User user = (User) session.getAttribute(SESSION_USER);
        Optional<User> otherUser = userRepository.findByUsername(username);

        if (otherUser.isEmpty()) {
            return new GenericResponse()
                    .setCode(10);
        }
        if (otherUser.get().getId().equals(user.getId())) {
            return new GenericResponse()
                    .setCode(11);
        }
        List<String> friendRequests = otherUser.get().getFriendRequestSenderIds();
        List<String> friendList = otherUser.get().getFriendsId();
        if (!friendRequests.contains(user.getId()) && !friendList.contains(user.getId())) {
            friendRequests.add(user.getId());
            otherUser.get().setFriendRequestSenderIds(friendRequests);
            userRepository.save(otherUser.get());

            template.convertAndSend("/server-client/request", user);

            return new GenericResponse() // istek basariyla gonderildi
                    .setCode(0);
        }
        return new GenericResponse() // istek daha once zaten gonderildi
                .setCode(1);
    }

    public GenericResponse acceptFriendRequest(HttpSession session, String id, int state) {
        User u = (User) session.getAttribute(SESSION_USER); // istek gonderilen
        Optional<User> user = userRepository.findById(u.getId());
        Optional<User> otherUser = userRepository.findById(id); // istek gonderen taraf
        List<String> requests = user.get().getFriendRequestSenderIds();

        if (state == 0) {
            // istek kabul edilme durumu
            List<String> friendList = user.get().getFriendsId();
            if (requests.contains(otherUser.get().getId())) {
                requests.remove(id);
                user.get().setFriendRequestSenderIds(requests);
                friendList.add(id);
                user.get().setFriendsId(friendList);
                userRepository.save(user.get());
                List<String> otherUserFriendList = otherUser.get().getFriendsId();
                otherUserFriendList.add(user.get().getId());
                otherUser.get().setFriendsId(otherUserFriendList);
                userRepository.save(otherUser.get());
                session.setAttribute(SESSION_USER, user.get());
                return new GenericResponse()
                        .setCode(0);
            }
        } else if (state == 1) {
            // istek ret edilme durumu
            requests.remove(id);
            user.get().setFriendRequestSenderIds(requests);
            userRepository.save(user.get());
            //session.setAttribute(SESSION_USER, user.get());

            return new GenericResponse()
                    .setCode(1);

        }
        // hata
        return new GenericResponse()
                .setCode(10);
        // id parametresi ile gonderilen user'in istegini kabul ediyor
    }


    public GenericResponse<Post> newPost(Post post, HttpSession session) {
        GenericResponse<Post> g = postService.newPost(post, session);
        Optional<User> user = userRepository.findById(g.getData().getOwnerId());
        List<String> postsId = user.get().getPostsId();
        postsId.add(g.getData().getId());
        user.get().setPostsId(postsId);
        User u = (User) session.getAttribute(SESSION_USER);

        userRepository.save(user.get());
        template.convertAndSend("/server-client/post/" + u.getId(), post);
        for (String id : user.get().getFriendsId()) {
            template.convertAndSend("/server-client/post/" + id, post);
        }

        return g;
        //todo func tasarimi kotu, daha iyi yapilmasi gerekiyor
        // gereksiz cok fazla kullanim var
    }
}
