-- Spring Modulith
CREATE TABLE IF NOT EXISTS event_publication
(
  id               UUID NOT NULL,
  listener_id      TEXT NOT NULL,
  event_type       TEXT NOT NULL,
  serialized_event TEXT NOT NULL,
  publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
  completion_date  TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (id)
);

    --- Create a GIN index on just the tsvector column
CREATE INDEX IF NOT EXISTS serialized_event_tsvector_idx
ON event_publication
USING gin (to_tsvector('simple', serialized_event));

--- Create a separate index for listener_id if needed
CREATE INDEX IF NOT EXISTS listener_id_idx
ON event_publication
(listener_id);

CREATE INDEX IF NOT EXISTS event_publication_completion_date_idx ON event_publication (completion_date);

--CREATE INDEX IF NOT EXISTS event_publication_by_listener_id_and_serialized_event_idx ON event_publication (listener_id, serialized_event);
--CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx ON event_publication USING hash(serialized_event);
--CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx ON event_publication (completion_date);
