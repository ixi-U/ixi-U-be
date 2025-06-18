package com.ixi_U.forbiddenWord;

import com.ixi_U.forbiddenWord.filters.AhoCorasickFilter;
import com.ixi_U.forbiddenWord.filters.EmbeddingSimilarityFilter;
import com.ixi_U.forbiddenWord.filters.LevenshteinThresholdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
@RestController
@RequestMapping("/forbidden-word")
public class ForbiddenWordController {

    private final ForbiddenWordLoader forbiddenWordLoader;
    private final AhoCorasickFilter ahoCorasickFilter;
    private final LevenshteinThresholdFilter levenshteinFilter;
    private final EmbeddingSimilarityFilter embeddingSimilarityFilter;
    private final ReviewFilter reviewFilter;
    private final ChatbotFilter chatbotFilter;

    @GetMapping("/basic")
    public boolean basicV(@RequestParam String input) {

        System.out.println("input = " + input);
        for (String word : forbiddenWordLoader.getForbiddenWords()) {
            if (input.contains(word)) {

                return true;
            }
        }
        return false;
    }

    @GetMapping("/aho")
    public boolean ahoV(@RequestParam String input) {

        System.out.println("input = " + input);
        return ahoCorasickFilter.matches(input);
    }

    @GetMapping("/leven")
    public boolean levenV(@RequestParam String input) {

        System.out.println("input = " + input);
        return levenshteinFilter.matches(input);
    }

    @GetMapping("/embedding")
    public boolean embeddingV(@RequestParam String input) {

        System.out.println("input = " + input);
        return embeddingSimilarityFilter.matches(input);
    }

    @GetMapping("/contains-embedding")
    public boolean containsEmbeddingV(@RequestParam String input) {

        System.out.println("input = " + input);

        for (String word : forbiddenWordLoader.getForbiddenWords()) {
            if (input.contains(word)) {

                return true;
            }
        }
        return embeddingSimilarityFilter.matches(input);
    }

    @GetMapping("/chatbot")
    public boolean chatbotV(@RequestParam String input) {

        System.out.println("input = " + input);
        return chatbotFilter.matches(input);
    }

    @GetMapping("/review")
    public boolean reviewV(@RequestParam String input) {

        System.out.println("input = " + input);
        return reviewFilter.matches(input);
    }
}
