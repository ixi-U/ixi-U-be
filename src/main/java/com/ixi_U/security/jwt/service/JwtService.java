package com.ixi_U.security.jwt.service;

import com.ixi_U.security.jwt.dto.CustomUserDto;
import com.ixi_U.security.jwt.provider.ClaimType;
import com.ixi_U.security.jwt.provider.JwtProvider;
import com.ixi_U.security.jwt.provider.dto.JwtProviderRequestDto;
import com.ixi_U.security.jwt.provider.dto.JwtProviderResponseDto;
import com.ixi_U.security.jwt.validator.JwtValidator;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    //    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final JwtValidator jwtValidator;

    /**
     * 토큰 재발급
     */
    public JwtProviderResponseDto reIssueToken(Claims claims, String refreshTokenFromClient) {

        // 요청 들어온 AT 에서 userId와 userRole 꺼냄
        CustomUserDto customUserDto = getUserInfoFromClaims(claims);

        String userId = customUserDto.userId();
        UserRole userRole = customUserDto.userRole();

//        // userId에 해당하는 RT 검색
//        UserEntity userEntity = userRepository.getReferenceById(userId);
//        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByUser(userEntity)
//                .orElseThrow(() -> new IllegalArgumentException("리프레쉬 토큰이 없습니다."));
//
//        String refreshTokenFromDB = refreshTokenEntity.getRefreshToken();
//
//        // DB RT 검증
//        jwtValidator.getClaims(refreshTokenFromDB); // 서명/구조 검증
//
//        // 클라이언트 RT와 DB RT 일치 비교
//        boolean valid = jwtValidator.validateRefreshToken(refreshTokenFromClient, refreshTokenFromDB);
//
//        if (!valid) {
//            throw new AuthenticationServiceException("RefreshToken 불일치");
//        }

        // 새 토큰 발급
        JwtProviderRequestDto requestDto = JwtProviderRequestDto.of(userId, userRole);

        return jwtProvider.generateAccessTokenAndRefreshToken(requestDto);
    }

    public JwtProviderResponseDto generateAccessTokenAndRefreshToken(JwtProviderRequestDto jwtProviderRequestDto) {
        return jwtProvider.generateAccessTokenAndRefreshToken(jwtProviderRequestDto);
    }

//    @Transactional
//    public void deleteOldAndNewRefreshToken(SaveRefreshTokenDTO saveRefreshTokenDTO) {
//        log.info("삭제 쿼리 실행");
//        deleteRefreshToken(saveRefreshTokenDTO.getUserId());
//        log.info("저장 쿼리 실행");
//        saveRefreshToken(saveRefreshTokenDTO);
//    }

    public CustomUserDto getUserInfoFromAccessToken(String accessToken) {
        Claims claims = jwtValidator.getClaims(accessToken);

        return getUserInfoFromClaims(claims);
    }

//    public void saveRefreshTokenForOAuth2Login(SaveRefreshTokenDTO saveRefreshTokenDTO) {
//
//        UserEntity userEntity = userRepository.getReferenceById(saveRefreshTokenDTO.getUserId());
//
//        refreshTokenRepository.findByUser(userEntity)
//                .ifPresentOrElse(
//                        entity -> entity.updateRefreshToken(saveRefreshTokenDTO.getRefreshToken()),
//                        () -> {
//                            RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.of(saveRefreshTokenDTO.getRefreshToken(), userEntity);
//                            refreshTokenRepository.save(refreshTokenEntity);
//                        }
//                );
//    }

//    private void saveRefreshToken(SaveRefreshTokenDTO saveRefreshTokenDTO) {
//
//        UserEntity userEntity = userRepository.getReferenceById(saveRefreshTokenDTO.getUserId());
//
//        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.of(saveRefreshTokenDTO.getRefreshToken(), userEntity);
//
//        refreshTokenRepository.save(refreshTokenEntity);
//    }

//    private void deleteRefreshToken(Long userId) {

//        UserEntity userEntity = userRepository.getReferenceById(userId);
//
//        refreshTokenRepository.deleteByUser(userEntity);
//        refreshTokenRepository.flush();
//    }

    private CustomUserDto getUserInfoFromClaims(Claims claims) {
        String userId = claims.get(ClaimType.USER_ID.getKey(), String.class);
        String userRole = claims.get(ClaimType.USER_ROLE.getKey(), String.class);

        return CustomUserDto.of(userId, UserRole.valueOf(userRole));
    }

}
