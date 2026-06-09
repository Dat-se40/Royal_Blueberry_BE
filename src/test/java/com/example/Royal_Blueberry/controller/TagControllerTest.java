package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.entity.Tag;
import com.example.Royal_Blueberry.entity.User;
import com.example.Royal_Blueberry.entity.WordTagRelation;
import com.example.Royal_Blueberry.security.CustomUserDetails;
import com.example.Royal_Blueberry.service.impl.TagService;
import com.example.Royal_Blueberry.util.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController controller;

    @Test
    void getMyTagsUsesAuthenticatedUserId() {
        when(tagService.getAllTagsByUser("u1")).thenReturn(List.of(
                new Tag("tag-1", "u1", "Favorites", "star", "yellow", null)
        ));

        ResponseEntity<List<Tag>> response = controller.getMyTags(principal());

        assertEquals(1, response.getBody().size());
        verify(tagService).getAllTagsByUser("u1");
    }

    @Test
    void syncTagReturnsSavedTag() {
        Tag tag = new Tag(null, null, "Favorites", "star", "yellow", null);
        when(tagService.createOrUpdateTag(tag, "u1")).thenReturn(
                new Tag("tag-1", "u1", "Favorites", "star", "yellow", null)
        );

        ResponseEntity<Tag> response = controller.syncTag(tag, principal());

        assertEquals("tag-1", response.getBody().getId());
    }

    @Test
    void deleteTagReturnsNoContent() {
        ResponseEntity<Void> response = controller.deleteTag("tag-1", principal());

        assertEquals(204, response.getStatusCode().value());
        verify(tagService).deleteTag("tag-1", "u1");
    }

    @Test
    void relationEndpointsDelegateToService() {
        when(tagService.getAllRelationsByUser("u1")).thenReturn(List.of(new WordTagRelation()));

        ResponseEntity<List<WordTagRelation>> allRelations = controller.getAllRelations(principal());
        ResponseEntity<Boolean> syncResponse =
                controller.syncRelation(new WordTagRelation(), principal());
        ResponseEntity<Void> unlinkResponse =
                controller.unlinkWordFromTag("tag-1", "hello", 0, principal());

        assertEquals(1, allRelations.getBody().size());
        assertEquals(true, syncResponse.getBody());
        assertEquals(204, unlinkResponse.getStatusCode().value());
        verify(tagService).unlinkWordFromTag("tag-1", "hello", 0, "u1");
    }

    private Principal principal() {
        CustomUserDetails userDetails = new CustomUserDetails(User.builder()
                .id("u1")
                .email("user@example.com")
                .role(Role.USER)
                .build());
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
