package com.ixi_U.common.filter;

public enum WordPreprocessingPolicy {

    NUMBERS("[\\p{N}]"), // 숫자 제거
    WHITESPACES("[\\s]"), // 공백 제거
    CONSONANTS("[ㄱ-ㅎ]"), // 한글 자음(초성) 제거
    VOWELS("[ㅏ-ㅣ]"); // 모음(중성) 제거

    private final String regex;

    WordPreprocessingPolicy(String regex) {
        this.regex = regex;
    }

    public String apply(String text) {

        return text.replaceAll(this.regex, "");
    }
}
