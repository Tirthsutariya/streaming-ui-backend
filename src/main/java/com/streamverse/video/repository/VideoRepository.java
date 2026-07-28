package com.streamverse.video.repository;

import com.streamverse.video.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {

    Optional<Video> findByIdAndIsDeletedFalse(UUID id);

    Page<Video> findAllByIsDeletedFalse(Pageable pageable);

    boolean existsByDescriptionAndIsDeletedFalse(String description);

    boolean existsByDescriptionAndIsDeletedFalseAndIdNot(String description, UUID id);
}
