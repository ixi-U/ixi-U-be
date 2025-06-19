package com.ixi_U.forbiddenWord.filters;

import com.ixi_U.forbiddenWord.ForbiddenWordLoader;
import com.ixi_U.forbiddenWord.KoreanAnalyzeService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("embeddingSimilarityFilter")
@RequiredArgsConstructor
public class EmbeddingSimilarityFilter implements ForbiddenWordFilter, ApplicationRunner {

    private final ForbiddenWordLoader forbiddenWordLoader;
    @Qualifier("forbiddenVectorStore")
    private final VectorStore vectorStore;
    private final KoreanAnalyzeService koreanAnalyzeService;
    private final Environment environment;

    public void run(ApplicationArguments args) throws Exception {

        if (System.getProperty("spring.profiles.active") == null) {
            System.setProperty("spring.profiles.active", "default");
        }

        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("test".equalsIgnoreCase(profile)) {
                return;
            }
        }

        // 벡터저장소에 존재할 경우 금칙어 데이터 저장 안함
        List<Document> docsToAdd = forbiddenWordLoader.getForbiddenWords().stream()
                .filter(w -> vectorStore.similaritySearch(
                                SearchRequest.builder()
                                        .query(w)
                                        .topK(1)
                                        .filterExpression("id == '" + w + "'")
                                        .build()
                        ).isEmpty()
                )
                .map(w -> new Document(
                        w,
                        Map.of("id", w, "type", "EmbeddedForbidden")
                ))
                .toList();


        // 벡터 저장소에 존재 여부 상관없이 데이터 저장
//        List<Document> docsToAdd = forbiddenWordLoader.getForbiddenWords().stream()
//                .map(w -> new Document(
//                        w,
//                        Map.of("id", w, "type", "EmbeddedForbidden")
//                ))
//                .toList();
//
//        System.out.println("docsToAdd.size() = " + docsToAdd.size());
//        if (!docsToAdd.isEmpty()) {
//            vectorStore.add(docsToAdd);
//        }
    }

    public List<Document> findSimilarForbiddenWords(String text) {

        List<String> chunks = koreanAnalyzeService.getAnalyze(text);

        Set<Document> results = ConcurrentHashMap.newKeySet();

        chunks.parallelStream().forEach(chunk -> {
            List<Document> found = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(chunk)
                            .topK(3)
                            .similarityThreshold(0.75)
                            .filterExpression("type == 'EmbeddedForbidden'")
                            .build()
            );
            synchronized (results) {
                results.addAll(found);
            }
        });

        return List.copyOf(results);
    }

    @Override
    public boolean matches(String text) {

        return !findSimilarForbiddenWords(text).isEmpty();
    }
}
