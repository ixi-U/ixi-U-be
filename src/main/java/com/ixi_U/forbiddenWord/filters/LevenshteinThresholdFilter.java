package com.ixi_U.forbiddenWord.filters;

import com.ixi_U.forbiddenWord.ForbiddenWordFilter;
import com.ixi_U.forbiddenWord.ForbiddenWordLoader;
import com.ixi_U.forbiddenWord.WordPreprocessingPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("levenshteinThresholdFilter")
@RequiredArgsConstructor
public class LevenshteinThresholdFilter implements ForbiddenWordFilter {

    private final ForbiddenWordLoader forbiddenWordLoader;

    @Override
    public boolean matches(String text) {

        String cleanedInput = text;

        for (WordPreprocessingPolicy policy : WordPreprocessingPolicy.values()) {
            cleanedInput = policy.apply(cleanedInput);
        }

        for (String forbidden : forbiddenWordLoader.getForbiddenWords()) {
            if (text.contains(forbidden) || cleanedInput.contains(forbidden)) {

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

    private int getThreshold(int length) {

        return (int) Math.floor(length * 0.25);
    }
}
