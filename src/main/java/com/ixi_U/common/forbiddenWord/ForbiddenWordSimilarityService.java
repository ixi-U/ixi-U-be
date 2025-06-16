package com.ixi_U.common.forbiddenWord;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForbiddenWordSimilarityService {

    private final ForbiddenWordLoader forbiddenWordLoader;
    private final Neo4jVectorStore vectorStore;

    @EventListener(ApplicationReadyEvent.class)
    public void initOnce() {
        // 1) 이미 'forbidden' type 문서가 하나라도 있으면 스킵
        long existing = vectorStore.searchByMetadata("type", "forbidden")
                .stream().count();
        if (existing > 0) {
            return;
        }

        // 2) 없으면 최초 한 번만 로드
        List<Document> docs = forbiddenWordLoader.getForbiddenWords().stream()
                .map(w -> new Document(
                        w,                                  // content
                        Map.of("id", w, "type", "forbidden")
                ))
                .toList();
        vectorStore.add(docs);
    }

    public List<Document> findSimilarForbiddenWords(String text) {

        List<Document> list = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(text)
                        .topK(5)
                        .similarityThreshold(0.75)
                        .filterExpression("type == 'forbidden'")
                        .build());

        System.out.println("list = " + list);
        return list;
    }

    public boolean containsForbidden(String text) {

        return !findSimilarForbiddenWords(text).isEmpty();
    }
}
