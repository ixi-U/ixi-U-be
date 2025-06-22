package com.ixi_U.forbiddenWord;

import com.ixi_U.forbiddenWord.filters.AhoCorasickFilter;
import com.ixi_U.forbiddenWord.filters.ForbiddenWordFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewFilter implements ForbiddenWordFilter {

    private final AhoCorasickFilter ahoCorasickFilter;

    @Override
    public boolean matches(String text) {

        if (ahoCorasickFilter.matches(text)) {

            return true;
        }
        return false;
    }
}
