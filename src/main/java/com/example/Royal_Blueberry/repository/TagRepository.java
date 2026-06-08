package com.example.Royal_Blueberry.repository;

import com.example.Royal_Blueberry.entity.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends MongoRepository<Tag, String> {
    List<Tag> findByUserId(String userId);
}