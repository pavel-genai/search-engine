package com.searchengine;

import com.searchengine.index.InvertedIndex;
import com.searchengine.persistence.IndexPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class IndexPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadRoundTrip() throws IOException {
        String indexPath = tempDir.resolve("subdir").resolve("index.ser").toString();

        IndexPersistence persistence = new IndexPersistence(indexPath);

        InvertedIndex original = new InvertedIndex();
        original.addDocument("Doc 1", "hello world java");
        original.addDocument("Doc 2", "python programming");

        persistence.save(original);

        InvertedIndex loaded = persistence.load();
        assertEquals(2, loaded.getDocumentCount());
        assertFalse(loaded.getPostings("hello").isEmpty());
        assertFalse(loaded.getPostings("python").isEmpty());
    }

    @Test
    void loadReturnsEmptyIndexWhenFileDoesNotExist() {
        String indexPath = tempDir.resolve("nonexistent.ser").toString();
        IndexPersistence persistence = new IndexPersistence(indexPath);

        InvertedIndex loaded = persistence.load();
        assertNotNull(loaded);
        assertEquals(0, loaded.getDocumentCount());
    }

    @Test
    void saveCreatesParentDirectories() throws IOException {
        String indexPath = tempDir.resolve("a").resolve("b").resolve("c").resolve("index.ser").toString();
        IndexPersistence persistence = new IndexPersistence(indexPath);

        InvertedIndex index = new InvertedIndex();
        index.addDocument("Test", "content here");

        persistence.save(index);

        InvertedIndex loaded = persistence.load();
        assertEquals(1, loaded.getDocumentCount());
    }

    @Test
    void loadReturnsEmptyIndexForCorruptedFile() {
        String indexPath = tempDir.resolve("corrupted.ser").toString();
        try {
            java.nio.file.Files.write(Path.of(indexPath), new byte[]{1, 2, 3, 4, 5});
        } catch (IOException e) {
            fail("Failed to write test file");
        }

        IndexPersistence persistence = new IndexPersistence(indexPath);
        InvertedIndex loaded = persistence.load();
        assertNotNull(loaded);
        assertEquals(0, loaded.getDocumentCount());
    }
}