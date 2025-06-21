package com.ixi_U.forbiddenWord.filters;

import com.ixi_U.forbiddenWord.KoreanAnalyzeService;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("embeddingSimilarityFilter")
@RequiredArgsConstructor
public class EmbeddingSimilarityFilter implements ForbiddenWordFilter {

    @Qualifier("forbiddenVectorStore")
    private final VectorStore vectorStore;
    private final KoreanAnalyzeService koreanAnalyzeService;

    public List<Document> findSimilarForbiddenWords(String text) {

        List<String> chunks = koreanAnalyzeService.getAnalyze(text);

        System.out.println("chunks = " + chunks);
        Set<Document> results = ConcurrentHashMap.newKeySet();

        chunks.parallelStream().forEach(chunk -> {
            List<Document> found = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(chunk)
                            .topK(3)
                            .similarityThreshold(0.8)
                            .filterExpression("type == 'EmbeddedForbidden'")
                            .build()
            );
            synchronized (results) {
                results.addAll(found);
            }
        });

        System.out.println("results = " + results);

        return List.copyOf(results);
    }

    @Override
    public boolean matches(String text) {

        return !findSimilarForbiddenWords(text).isEmpty();
    }

}