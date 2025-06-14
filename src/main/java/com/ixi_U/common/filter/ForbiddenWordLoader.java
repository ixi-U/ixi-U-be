package com.ixi_U.common.filter;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ForbiddenWordLoader {

    private final List<String> forbiddenWords = new ArrayList<>();

    @PostConstruct
    public void init() throws Exception {

        try (InputStream is = getClass().getResourceAsStream("/forbidden-words.txt")) {
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                reader.lines().forEach(forbiddenWords::add);
            }
        }
    }

}
