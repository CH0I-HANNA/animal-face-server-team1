package com.likelion.animalface.domain.analysis.entity;

import com.likelion.animalface.domain.user.entity.User;
import com.likelion.animalface.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name = "animal_results")
@Entity
public class AnimalResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnimalType animalType;

    @Column(nullable = false)
    private Double similarity;

    @Column(nullable = false)
    private Double catSimilarity;

    @Column(nullable = false)
    private Double dogSimilarity;

    @Column(nullable = false)
    private Double foxSimilarity;

    @Column(nullable = false)
    private Double bearSimilarity;
}
