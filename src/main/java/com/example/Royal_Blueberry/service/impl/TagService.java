package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.entity.Tag;
import com.example.Royal_Blueberry.entity.WordTagRelation;
import com.example.Royal_Blueberry.repository.TagRepository;
import com.example.Royal_Blueberry.repository.WordTagRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final WordTagRelationRepository relationRepository;

    // --- TAG CRUD ---
    public List<Tag> getAllTagsByUser(String userId) {
        return tagRepository.findByUserId(userId);
    }

    public Tag createOrUpdateTag(Tag tag, String userId) {
        if (tag.getId() == null || tag.getId().isEmpty()) {
            tag.setId(UUID.randomUUID().toString());
        }
        tag.setUserId(userId);
        tag.setLastModifiedAt(Instant.now());
        return tagRepository.save(tag);
    }

    public void deleteTag(String tagId, String userId) {
        // Xóa Tag
        tagRepository.deleteById(tagId);
        // Xóa luôn tất cả các quan hệ Word - Tag của Tag này
        relationRepository.deleteByUserIdAndTagId(userId, tagId);
    }

    // --- WORD TAG RELATION ---
    public WordTagRelation linkWordToTag(WordTagRelation relation, String userId) {
        if (relation.getId() == null || relation.getId().isEmpty()) {
            relation.setId(UUID.randomUUID().toString());
        }
        relation.setUserId(userId);
        relation.setLinkedAt(Instant.now());
        return relationRepository.save(relation);
    }

    public void unlinkWordFromTag(String tagId, String word, Integer meaningIndex, String userId) {
        relationRepository.deleteByUserIdAndTagIdAndWordAndMeaningIndex(userId, tagId, word, meaningIndex);
    }

    public List<WordTagRelation> getRelationsByWord(String word, String userId) {
        return relationRepository.findByUserIdAndWord(userId, word);
    }
    public List<WordTagRelation> getAllRelationsByUser(String userId) {
        return relationRepository.findByUserId(userId);
    }
}