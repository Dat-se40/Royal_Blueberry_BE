package com.example.Royal_Blueberry.repository;

import com.example.Royal_Blueberry.entity.EmbedWordVector;
import com.example.Royal_Blueberry.service.EmbedWordService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!no_ai")
public interface EmbedWordVectorRepository extends MongoRepository<EmbedWordVector,String>
{
}
