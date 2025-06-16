package com.ixi_U.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Getter
@With
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private final String id;

    private final String name;

    private final String email;

    private final UserRole userRole;

    private final String provider;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private final Long kakaoId;

    private String refreshToken;

    @Builder.Default
    @Relationship(type = "REVIEWED", direction = Relationship.Direction.OUTGOING)
    private List<Reviewed> reviewedHistory = new ArrayList<>();

    @Builder.Default
    @Relationship(type = "SUBSCRIBED", direction = Relationship.Direction.OUTGOING)
    private List<Subscribed> subscribedHistory = new ArrayList<>();

    public static User of(final String name, final String email, final String provider,
            final Long kakaoId, final UserRole userRole) {
        return User.builder()
                .name(name)
                .email(email)
                .provider(provider)
                .kakaoId(kakaoId)
                .userRole(userRole)
                .build();
    }

    public User updateRefreshToken(String refreshToken) {
        return this.withRefreshToken(refreshToken);
    }

    public void addReviewed(final Reviewed reviewed) {

        reviewedHistory.add(reviewed);
    }

    public void addSubscribed(final Subscribed subscribed) {

        subscribedHistory.add(subscribed);
    }
}
