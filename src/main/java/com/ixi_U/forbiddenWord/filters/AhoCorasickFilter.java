package com.ixi_U.forbiddenWord.filters;

import com.ixi_U.forbiddenWord.ForbiddenWordLoader;
import com.ixi_U.forbiddenWord.WordPreprocessingPolicy;
import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Component;

@Component("ahoCorasickFilter")
public class AhoCorasickFilter implements ForbiddenWordFilter {

    private final Trie trie;

    public AhoCorasickFilter(ForbiddenWordLoader loader) {

        this.trie = Trie.builder()
                .addKeywords(loader.getForbiddenWords())
                .build();
    }

    @Override
    public boolean matches(String text) {

        String cleanedInput = text;

        for (WordPreprocessingPolicy policy : WordPreprocessingPolicy.values()) {
            cleanedInput = policy.apply(cleanedInput);
        }

        return trie.containsMatch(cleanedInput);
    }
}
