ALTER TABLE documents
ADD COLUMN search_vector tsvector
GENERATED ALWAYS AS (
    to_tsvector(
        'simple',
        coalesce(original_filename, '') || ' ' || coalesce(extracted_text, '')
    )
) STORED;
