package com.searchengine;

import com.searchengine.ranker.SearchResult;
import com.searchengine.ranker.TfIdfRanker;
import com.searchengine.index.InvertedIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TfIdfRankerTest {

    private InvertedIndex index;
    private TfIdfRanker ranker;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
        index.addDocument("Java Tutorial", "java programming language is powerful and java is fun");
        index.addDocument("Python Guide", "python programming language is easy");
        index.addDocument("Cooking 101", "how to cook delicious meals with fresh ingredients");
        ranker = new TfIdfRanker(index);
    }

    @Test
    void scoreReturnsNonZeroForTermInDoc() {
        double score = ranker.score("java", 1);
        assertTrue(score > 0, "TF-IDF score for 'java' in doc 1 should be positive");
    }

    @Test
    void scoreReturnsZeroForTermNotInDoc() {
        double score = ranker.score("java", 2);
        assertEquals(0.0, score, 0.001, "TF-IDF for 'java' in doc 2 should be 0");
    }

    @Test
    void scoreReturnsZeroForUnknownTerm() {
        double score = ranker.score("nonexistent", 1);
        assertEquals(0.0, score, 0.001);
    }

    @Test
    void rankReturnsSortedResults() {
        Set<Integer> docIds = new HashSet<>(Arrays.asList(1, 2));
        List<String> terms = Arrays.asList("programming");
        List<SearchResult> results = ranker.rank(docIds, terms);

        assertFalse(results.isEmpty());
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).getScore() >= results.get(i).getScore(),
                    "Results should be sorted by score descending");
        }
    }

    @Test
    void rankSkipsDocIdsWithZeroScore() {
        Set<Integer> docIds = new HashSet<>(Arrays.asList(1, 3));
        List<String> terms = Arrays.asList("java");
        List<SearchResult> results = ranker.rank(docIds, terms);
        assertEquals(1, results.size(), "Only doc 1 contains 'java', doc 3 has zero score");
        assertEquals(1, results.get(0).getDocId());
    }

    @Test
    void rankHandlesMultipleTerms() {
        Set<Integer> docIds = new HashSet<>(Arrays.asList(1, 2));
        List<String> terms = Arrays.asList("programming", "language");
        List<SearchResult> results = ranker.rank(docIds, terms);
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).getScore() > 0);
    }

    @Test
    void rankHandlesEmptyDocIds() {
        Set<Integer> docIds = Collections.emptySet();
        List<String> terms = Arrays.asList("java");
        List<SearchResult> results = ranker.rank(docIds, terms);
        assertTrue(results.isEmpty());
    }

    @Test
    void rankHandlesEmptyTerms() {
        Set<Integer> docIds = new HashSet<>(Arrays.asList(1));
        List<String> terms = Collections.emptyList();
        List<SearchResult> results = ranker.rank(docIds, terms);
        assertTrue(results.isEmpty(), "No terms means all scores are 0");
    }

    @Test
    void snippetShortContentReturnedAsIs() {
        Set<Integer> docIds = new HashSet<>(Arrays.asList(1));
        List<String> terms = Arrays.asList("java");
        List<SearchResult> results = ranker.rank(docIds, terms);
        assertFalse(results.isEmpty());
        String snippet = results.get(0).getSnippet();
        assertNotNull(snippet);
        assertTrue(snippet.length() <= 203, "Snippet should be at most 200 chars + ellipsis");
    }

    @Test
    void snippetTruncatesLongContent() {
        StringBuilder longText = new StringBuilder("java ");
        for (int i = 0; i < 100; i++) {
            longText.append("word").append(i).append(" ");
        }
        index.addDocument("Long Doc", longText.toString());
        int longDocId = index.getDocumentCount();

        Set<Integer> docIds = new HashSet<>(Arrays.asList(longDocId));
        List<String> terms = Arrays.asList("java");
        List<SearchResult> results = ranker.rank(docIds, terms);
        assertFalse(results.isEmpty());
        String snippet = results.get(0).getSnippet();
        assertTrue(snippet.endsWith("..."), "Long content snippet should end with ellipsis");
    }

    @Test
    void searchResultGettersReturnCorrectValues() {
        SearchResult result = new SearchResult(5, "Title", 3.14, "snippet text");
        assertEquals(5, result.getDocId());
        assertEquals("Title", result.getTitle());
        assertEquals(3.14, result.getScore(), 0.001);
        assertEquals("snippet text", result.getSnippet());
    }

    @Test
    void rankWithTermNotInAnyDoc() {
        Set<Integer> docIds = new HashSet<>(Arrays.asList(1, 2, 3));
        List<String> terms = Arrays.asList("nonexistent");
        List<SearchResult> results = ranker.rank(docIds, terms);
        assertTrue(results.isEmpty());
    }
}