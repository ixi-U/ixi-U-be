package com.ixi_U.forbiddenWord;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.phrase_extractor.KoreanPhraseExtractor;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.springframework.stereotype.Service;
import scala.collection.Seq;

@Service
@RequiredArgsConstructor
public class KoreanAnalyzeService {

    public List<String> getAnalyze(String word) {

        // Normalize
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(word);

        // Tokenize
        Seq<KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
        System.out.println(OpenKoreanTextProcessorJava.splitSentences(normalized));

        // Phrase Extraction
        List<KoreanPhraseExtractor.KoreanPhrase> phrases = OpenKoreanTextProcessorJava.extractPhrases(
                tokens, true, true);
        // [한국어(Noun: 0, 3), 처리(Noun: 5, 2), 처리하는 예시(Noun: 5, 7), 예시(Noun: 10, 2), #한국어(Hashtag: 18, 4)]

        return phrases.stream().map(koreanPhrase -> koreanPhrase.text()).toList();
    }

}
