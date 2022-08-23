package com.beam.facebook.repository;

import com.beam.facebook.model.Post;
import com.beam.facebook.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post,String> {
}
