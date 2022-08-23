package com.beam.facebook.model;

import lombok.Data;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document
@TypeAlias("post")
public class Post extends Base{

    private String message;
    private byte[] image;
    private List<String> comment;
    private String ownerId;
    private String ownerName;
    private LocalDateTime creationTime;

}
