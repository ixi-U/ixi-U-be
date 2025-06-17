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

//    public void run(ApplicationArguments args) throws Exception {
//        // load all forbidden words, but filter out those already in the vector store by checking for an existing ID match
//        List<Document> docsToAdd = forbiddenWordLoader.getForbiddenWords().stream()
//                .filter(w -> vectorStore.similaritySearch(
//                                SearchRequest.builder()
//                                        .query(w)
//                                        .topK(1)
//                                        .filterExpression("id == '" + w + "'")
//                                        .build()
//                        ).isEmpty()
//                )
//                .map(w -> new Document(
//                        w,
//                        Map.of("id", w, "type", "forbidden")
//                ))
//                .toList();
//
//        // only add if there are new entries
//        if (!docsToAdd.isEmpty()) {
//            vectorStore.add(docsToAdd);
//        }
//    }

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
