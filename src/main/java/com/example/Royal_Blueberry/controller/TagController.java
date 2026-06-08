package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.entity.Tag;
import com.example.Royal_Blueberry.entity.WordTagRelation;
import com.example.Royal_Blueberry.security.CustomUserDetails;
import com.example.Royal_Blueberry.service.impl.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private String getUserId(Principal principal) {
        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) authToken.getPrincipal();
        return userDetails.getUser().getId();
    }


    // FE gọi lúc khởi động: Lấy toàn bộ Tags
    @GetMapping("/tags")
    public ResponseEntity<List<Tag>> getMyTags(Principal principal) {
        return ResponseEntity.ok(tagService.getAllTagsByUser(getUserId(principal)));
    }

    // FE gọi lúc đồng bộ 1 Tag: tags/sync
    @PostMapping("/tags/sync")
    public ResponseEntity<Tag> syncTag(@RequestBody Tag tag, Principal principal) {
        return ResponseEntity.ok(tagService.createOrUpdateTag(tag, getUserId(principal)));
    }

    // Xóa tag
    @DeleteMapping("/tags/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable String tagId, Principal principal) {
        tagService.deleteTag(tagId, getUserId(principal));
        return ResponseEntity.noContent().build();
    }


    // ================== PHẦN RELATION ==================

    // FE gọi lúc khởi động: Lấy toàn bộ Relations của User (ĐỂ C# TẢI VỀ MÁY MỚI)
    @GetMapping("/relations")
    public ResponseEntity<List<WordTagRelation>> getAllRelations(Principal principal) {
        return ResponseEntity.ok(tagService.getAllRelationsByUser(getUserId(principal)));
    }

    // FE gọi lúc đồng bộ 1 Relation: relations/sync
    @PostMapping("/relations/sync")
    public ResponseEntity<Boolean> syncRelation(@RequestBody WordTagRelation relation, Principal principal) {
        tagService.linkWordToTag(relation, getUserId(principal));
        return ResponseEntity.ok(true);
    }

    // Xóa liên kết (Bỏ tag khỏi từ)
    @DeleteMapping("/relations/unlink")
    public ResponseEntity<Void> unlinkWordFromTag(
            @RequestParam String tagId,
            @RequestParam String word,
            @RequestParam Integer meaningIndex,
            Principal principal) {
        tagService.unlinkWordFromTag(tagId, word, meaningIndex, getUserId(principal));
        return ResponseEntity.noContent().build();
    }
}