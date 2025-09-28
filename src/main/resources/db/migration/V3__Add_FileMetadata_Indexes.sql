-- Additional indexes for FileMetadata table to improve query performance
-- Migration V3: Optimize FileMetadata table indexes

-- Composite index for file search by user and activity
CREATE INDEX idx_user_active_upload ON FileMetadata (user_id, is_active, upload_time DESC);

-- Composite index for node statistics queries
CREATE INDEX idx_node_active_size ON FileMetadata (node_id, is_active, file_size);

-- Index for inactive file cleanup
CREATE INDEX idx_active_last_accessed ON FileMetadata (is_active, last_accessed);

-- Full-text index for filename search (if MySQL supports it)
-- ALTER TABLE FileMetadata ADD FULLTEXT(original_filename);

-- Index for content type filtering
CREATE INDEX idx_content_type_active ON FileMetadata (content_type, is_active);

-- Composite index for file existence checks
CREATE INDEX idx_file_exists ON FileMetadata (file_id, is_active, node_id);

-- Index for checksum-based duplicate detection (when checksum is implemented)
CREATE INDEX idx_checksum ON FileMetadata (checksum) WHERE checksum IS NOT NULL;