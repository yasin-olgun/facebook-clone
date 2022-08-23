package com.beam.facebook.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document
@TypeAlias("user")
@Accessors(chain = true)
public class User extends Base {

    private String username;
    private String firstName;
    private String lastName;
    private String password;
    private List<String> friendsId;
    private List<String> postsId;
    private List<String> friendRequestSenderIds;

    private String avatar;

    public List<String> getFriendRequestSenderIds() {
        if (friendRequestSenderIds == null) {
            return new ArrayList<>();
        }
        return friendRequestSenderIds;
    }

    public List<String> getFriendsId() {
        if (friendsId == null) {
            return new ArrayList<>();
        }
        return friendsId;
    }

    public List<String> getPostsId() {
        if (postsId == null) {
            return new ArrayList<>();
        }
        return postsId;

    }
}
