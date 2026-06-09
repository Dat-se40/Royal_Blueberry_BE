package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.entity.Tag;
import com.example.Royal_Blueberry.entity.WordTagRelation;
import com.example.Royal_Blueberry.repository.TagRepository;
import com.example.Royal_Blueberry.repository.WordTagRelationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private WordTagRelationRepository relationRepository;

    @InjectMocks
    private TagService service;

    @Test
    void getAllTagsByUserDelegatesToRepository() {
        when(tagRepository.findByUserId("user-1"))
                .thenReturn(List.of(new Tag("tag-1", "user-1", "Favorites", "star", "yellow", null)));

        List<Tag> result = service.getAllTagsByUser("user-1");

        assertEquals(1, result.size());
        assertEquals("Favorites", result.get(0).getName());
    }

    @Test
    void createOrUpdateTagAssignsMissingIdUserAndTimestamp() {
        Tag tag = new Tag();
        tag.setName("Important");
        when(tagRepository.save(tag)).thenReturn(tag);

        Tag saved = service.createOrUpdateTag(tag, "user-1");

        assertNotNull(saved.getId());
        assertEquals("user-1", saved.getUserId());
        assertNotNull(saved.getLastModifiedAt());
        verify(tagRepository).save(tag);
    }

    @Test
    void deleteTagRemovesTagAndRelatedRelations() {
        service.deleteTag("tag-1", "user-1");

        verify(tagRepository).deleteById("tag-1");
        verify(relationRepository).deleteByUserIdAndTagId("user-1", "tag-1");
    }

    @Test
    void linkWordToTagAssignsMetadataBeforeSaving() {
        WordTagRelation relation = new WordTagRelation();
        relation.setWord("hello");
        relation.setMeaningIndex(0);
        when(relationRepository.save(relation)).thenReturn(relation);

        WordTagRelation saved = service.linkWordToTag(relation, "user-1");

        assertNotNull(saved.getId());
        assertEquals("user-1", saved.getUserId());
        assertNotNull(saved.getLinkedAt());
        verify(relationRepository).save(relation);
    }

    @Test
    void unlinkWordFromTagDelegatesToRepository() {
        service.unlinkWordFromTag("tag-1", "hello", 2, "user-1");

        verify(relationRepository)
                .deleteByUserIdAndTagIdAndWordAndMeaningIndex("user-1", "tag-1", "hello", 2);
    }

    @Test
    void relationQueriesDelegateToRepository() {
        when(relationRepository.findByUserIdAndWord("user-1", "hello"))
                .thenReturn(List.of(new WordTagRelation("rel-1", "user-1", "hello", 0,
                        "tag-1", true, "note", null)));
        when(relationRepository.findByUserId("user-1"))
                .thenReturn(List.of(new WordTagRelation()));

        assertEquals(1, service.getRelationsByWord("hello", "user-1").size());
        assertEquals(1, service.getAllRelationsByUser("user-1").size());
    }
}
