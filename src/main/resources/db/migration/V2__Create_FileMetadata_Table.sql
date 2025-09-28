-- Create FileMetadata table for persistent file-to-node mapping
-- Migration V2: Add file metadata persistence

CREATE TABLE FileMetadata (
    file_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    node_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    checksum VARCHAR(64),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    upload_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_accessed TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    INDEX idx_file_id_active (file_id, is_active),
    INDEX idx_node_id_active (node_id, is_active),
    INDEX idx_user_id_active (user_id, is_active),
    INDEX idx_upload_time (upload_time),
    INDEX idx_last_accessed (last_accessed),
    INDEX idx_original_filename (original_filename),
    CONSTRAINT fk_file_metadata_node
        FOREIGN KEY (node_id)
        REFERENCES StorageContainers(container_id)
        ON DELETE CASCADE
);

-- Add comments for documentation
ALTER TABLE FileMetadata
    COMMENT = 'Persistent metadata for files stored across distributed nodes';

ALTER TABLE FileMetadata
    MODIFY COLUMN file_id BIGINT AUTO_INCREMENT
    COMMENT 'Unique identifier for the file';

ALTER TABLE FileMetadata
    MODIFY COLUMN original_filename VARCHAR(255) NOT NULL
    COMMENT 'Original filename as uploaded by user';

ALTER TABLE FileMetadata
    MODIFY COLUMN stored_filename VARCHAR(255) NOT NULL
    COMMENT 'Filename as stored on the storage node';

ALTER TABLE FileMetadata
    MODIFY COLUMN file_size BIGINT NOT NULL
    COMMENT 'Size of the file in bytes';

ALTER TABLE FileMetadata
    MODIFY COLUMN content_type VARCHAR(100)
    COMMENT 'MIME type of the file';

ALTER TABLE FileMetadata
    MODIFY COLUMN node_id BIGINT NOT NULL
    COMMENT 'ID of the storage node containing this file';

ALTER TABLE FileMetadata
    MODIFY COLUMN user_id BIGINT NOT NULL
    COMMENT 'ID of the user who uploaded the file';

ALTER TABLE FileMetadata
    MODIFY COLUMN checksum VARCHAR(64)
    COMMENT 'File checksum for integrity verification';

ALTER TABLE FileMetadata
    MODIFY COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE
    COMMENT 'Whether the file is active (not deleted)';

ALTER TABLE FileMetadata
    MODIFY COLUMN upload_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT 'When the file was uploaded';

ALTER TABLE FileMetadata
    MODIFY COLUMN last_accessed TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT 'When the file was last accessed';

ALTER TABLE FileMetadata
    MODIFY COLUMN version INT NOT NULL DEFAULT 0
    COMMENT 'Version number for optimistic locking';