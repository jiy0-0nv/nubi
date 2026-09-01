package com.nubi.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_images")
@Getter
@NoArgsConstructor
public class ReviewImagesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ReviewEntity review;

    @Column(nullable = false)
    private String url;

    @Column(name = "is_thumbnail", nullable = false)
    private boolean thumbnail;

    @Builder
    public ReviewImagesEntity(ReviewEntity review, String url, boolean thumbnail) {
        this.review = review;
        this.url = url;
        this.thumbnail = thumbnail;
    }
}
