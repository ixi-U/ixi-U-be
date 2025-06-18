package com.ixi_U.forbiddenWord.filters;

import com.ixi_U.forbiddenWord.ForbiddenWordLoader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("embeddingSimilarityFilter")
public class EmbeddingSimilarityFilter implements ForbiddenWordFilter {

    private final ForbiddenWordLoader forbiddenWordLoader;
    @Qualifier("forbiddenVectorStore")
    private final VectorStore vectorStore;
    
//    public void run(ApplicationArguments args) throws Exception {
//
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
//                        Map.of("word", w, "type", "forbidden")
//                ))
//                .toList();
//
//        if (!docsToAdd.isEmpty()) {
//            vectorStore.add(docsToAdd);
//        }
//    }

    public List<Document> findSimilarForbiddenWords(String text) {

//        CharSequence normalized = TwitterKoreanProcessorJava.normalize(text);
//        // Tokenize and convert Scala Seq to Java List
//        Seq<KoreanTokenizer.KoreanToken> tokensSeq = (Seq<KoreanTokenizer.KoreanToken>) TwitterKoreanProcessorJava.tokenize(normalized);
//        List<KoreanTokenizer.KoreanToken> tokenList = JavaConverters.seqAsJavaListConverter(tokensSeq)
//                .asJava();
//        List<String> chunks = tokenList.stream()
//            .map(KoreanTokenizer.KoreanToken::text)
//            .toList();
//
//        Collect matching forbidden word documents without duplicates
//
//        for (String chunk : chunks) {
//            System.out.println("chunk = " + chunk);
//
//        }

        Set<Document> results = new LinkedHashSet<>();

        results.addAll(
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(text)
                                .topK(5)
                                .similarityThreshold(0.72)
                                .filterExpression("type == 'forbidden'")
                                .build()
                )
        );

        System.out.println("results = " + results);

        return List.copyOf(results);
    }

    @Override
    public boolean matches(String text) {

        return !findSimilarForbiddenWords(text).isEmpty();
    }
}
