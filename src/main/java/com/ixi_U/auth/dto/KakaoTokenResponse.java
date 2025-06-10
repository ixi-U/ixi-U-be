package com.ixi_U.auth.dto;

// kakao로 부터 받는 응답 dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // 역직렬화를 위한 기본 생성자 - 기본 생성자를 자동으로 생성
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoTokenResponse {

//    Long id;

    @JsonProperty("token_type")
    public String token_type = "bearer";

    @JsonProperty("access_token")
    public String access_token;

//    String id_token; // OpenID Connect 활성화 시 - 더 안전하게 사용자 로그인

    @JsonProperty("expires_in")
    public String expires_in;

    @JsonProperty("refresh_token")
    String refresh_token;

    @JsonProperty("refresh_token_expires_in")
    Integer refresh_token_expires_in; // refresh 토큰 만료 시간 (초)
}
