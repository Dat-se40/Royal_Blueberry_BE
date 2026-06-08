package com.example.Royal_Blueberry.repository;

import com.example.Royal_Blueberry.entity.WordTagRelation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordTagRelationRepository extends MongoRepository<WordTagRelation, String> {
    List<WordTagRelation> findByUserId(String userId);
    List<WordTagRelation> findByUserIdAndTagId(String userId, String tagId);
    List<WordTagRelation> findByUserIdAndWord(String userId, String word);

    // Dùng để xóa liên kết khi un-tag
    void deleteByUserIdAndTagIdAndWordAndMeaningIndex(String userId, String tagId, String word, Integer meaningIndex);
    // Dùng để xóa tất cả liên kết khi xóa 1 tag
    void deleteByUserIdAndTagId(String userId, String tagId);
}