package com.streamverse.video.entity;

import com.streamverse.video.constant.VideoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "video")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
public class Video extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "description", nullable = false, unique = true, length = 100)
    private String description;

    @Column(name = "name", length = 100)
    private String name;

    // NOTE: corrected from the DBML typo "thumnail".
    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "video_link", length = 1000)
    private String videoLink;

    private Integer likes;

    // Master HLS playlist URL (master.m3u8), populated after transcoding.
    @Column(name = "hls_url", length = 1000)
    private String hlsUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private VideoStatus status;
}
