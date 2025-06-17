package com.ixi_U.forbiddenWord;

import com.ixi_U.forbiddenWord.filters.AhoCorasickFilter;
import com.ixi_U.forbiddenWord.filters.LevenshteinThresholdFilter;
import com.ixi_U.forbiddenWord.ForbiddenWordLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewFilter implements ForbiddenWordFilter {

    private final AhoCorasickFilter ahoCorasickFilter;
    private final LevenshteinThresholdFilter levenshteinFilter;
    private final ForbiddenWordLoader forbiddenWordLoader;

    @Override
    public boolean matches(String text) {

        // contains 검사
        for (String word : forbiddenWordLoader.getForbiddenWords()) {
            if (text.contains(word)) {

                return true;
            }
        }

        // Aho-Corasick 검사
        if (ahoCorasickFilter.matches(text)) {

            return true;
        }

        // Levenshtein 검사
        return levenshteinFilter.matches(text);
    }
}
