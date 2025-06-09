package com.ixi_U.user.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node(value = "User")
@Getter
@With
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private final String id;

    @Property(name = "name")

    private final String name;

    @Property(name = "email")
    private final String email;

    @Property(name = "provider")
    private final String provider;

    @Builder.Default
    @Relationship(type = "REVIEWED", direction = Relationship.Direction.OUTGOING)
    private List<Reviewed> reviewedHistory = new ArrayList<>();

    @Builder.Default
    @Relationship(type = "SUBSCRIBED", direction = Relationship.Direction.OUTGOING)
    private List<Subscribed> subscribedHistory = new ArrayList<>();

    public static User of(final String name, final String email, final String provider){

        return User.builder()
                .name(name)
                .email(email)
                .provider(provider)
                .build();
    }

    public void addReviewed(final Reviewed reviewed){

        reviewedHistory.add(reviewed);
    }

    public void addSubscribed(final Subscribed subscribed){

        subscribedHistory.add(subscribed);
    }
}
