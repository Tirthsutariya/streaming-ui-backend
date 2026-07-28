-- Reference DDL for the Video table (PostgreSQL).
-- With ddl-auto: validate on the postgres profile, create this table first.

CREATE TABLE IF NOT EXISTS video (
    id          UUID         PRIMARY KEY,
    description VARCHAR(100) NOT NULL UNIQUE,
    name        VARCHAR(100),
    thumbnail   VARCHAR(255),
    video_link  VARCHAR(1000),
    created_at  TIMESTAMP    NOT NULL,
    created_by  UUID,
    updated_at  TIMESTAMP    NOT NULL,
    updated_by  UUID,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_video_is_deleted ON video (is_deleted);

ALTER TABLE video
    ADD COLUMN IF NOT EXISTS likes INTEGER NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS comment (
                                       id          UUID PRIMARY KEY,
                                       video_id    UUID NOT NULL,
                                       comment     TEXT NOT NULL,

                                       created_at  TIMESTAMP NOT NULL,
                                       created_by  UUID,
                                       updated_at  TIMESTAMP NOT NULL,
                                       updated_by  UUID,
                                       is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,

                                       CONSTRAINT fk_comment_video
                                       FOREIGN KEY (video_id)
    REFERENCES video(id)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_comment_video
    ON comment(video_id);

CREATE INDEX IF NOT EXISTS idx_comment_deleted
    ON comment(is_deleted);
