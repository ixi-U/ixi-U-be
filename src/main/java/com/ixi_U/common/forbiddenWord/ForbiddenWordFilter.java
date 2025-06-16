package com.ixi_U.common.forbiddenWord;

import org.ahocorasick.trie.Trie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ForbiddenWordFilter {

    private final ForbiddenWordLoader forbiddenWordLoader;
    private final Trie trie;

    @Autowired
    public ForbiddenWordFilter(ForbiddenWordLoader forbiddenWordLoader) {
        // TrieBuilder를 통해 단어 등록

        this.forbiddenWordLoader = forbiddenWordLoader;

        this.trie = Trie.builder()
                .addKeywords(forbiddenWordLoader.getForbiddenWords())
                .build();

    }

    private static int levenshtein(String a, String b) {

        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],  // 치환
                            Math.min(dp[i - 1][j],  // 삭제
                                    dp[i][j - 1])); // 삽입
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    public boolean isForbidden(String input) {

        String cleanedInput = input;

        for (WordPreprocessingPolicy policy : WordPreprocessingPolicy.values()) {
            cleanedInput = policy.apply(cleanedInput);
        }

        for (String forbidden : forbiddenWordLoader.getForbiddenWords()) {
            if (input.contains(forbidden) || cleanedInput.contains(forbidden)) {

                return true;
            }
            int threshold = getThreshold(
                    Math.max(cleanedInput.length(), forbidden.length()));
            if (levenshtein(cleanedInput, forbidden) <= threshold) {

                return true;
            }
        }

        return false;
    }

    private int getThreshold(int length) {

        return (int) Math.floor(length * 0.25);
    }

    public boolean containsForbiddenWords(String input) {

        String cleanedInput = input;

        for (WordPreprocessingPolicy policy : WordPreprocessingPolicy.values()) {
            cleanedInput = policy.apply(cleanedInput);
        }

        return trie.containsMatch(cleanedInput);
    }

}
