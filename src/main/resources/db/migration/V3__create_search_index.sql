CREATE INDEX idx_documents_search_vector
ON documents
USING GIN(search_vector);
