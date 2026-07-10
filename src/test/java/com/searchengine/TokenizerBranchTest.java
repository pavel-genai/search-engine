package com.searchengine;

import com.searchengine.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerBranchTest {

    private final Tokenizer tokenizer = new Tokenizer();

    @Test
    void stemHandlesIesSuffix() {
        assertEquals("category", tokenizer.stem("categories"));
        assertEquals("puppy", tokenizer.stem("puppies"));
    }

    @Test
    void stemHandlesSsesSuffix() {
        assertEquals("class", tokenizer.stem("classes"));
        assertEquals("kiss", tokenizer.stem("kisses"));
    }

    @Test
    void stemHandlesIngSuffixWithDoubledConsonant() {
        assertEquals("run", tokenizer.stem("running"));
        assertEquals("swim", tokenizer.stem("swimming"));
    }

    @Test
    void stemHandlesIngSuffixWithoutDoubledConsonant() {
        assertEquals("program", tokenizer.stem("programming"));
    }

    @Test
    void stemHandlesTionSuffix() {
        assertEquals("creat", tokenizer.stem("creation"));
        assertEquals("evaluat", tokenizer.stem("evaluation"));
    }

    @Test
    void stemHandlesMentSuffix() {
        assertEquals("argu", tokenizer.stem("argument"));
        assertEquals("establish", tokenizer.stem("establishment"));
    }

    @Test
    void stemHandlesNessSuffix() {
        assertEquals("happi", tokenizer.stem("happiness"));
        assertEquals("dark", tokenizer.stem("darkness"));
    }

    @Test
    void stemHandlesAbleSuffix() {
        assertEquals("comfort", tokenizer.stem("comfortable"));
        assertEquals("read", tokenizer.stem("readable"));
    }

    @Test
    void stemHandlesLySuffix() {
        assertEquals("quick", tokenizer.stem("quickly"));
        assertEquals("real", tokenizer.stem("really"));
    }

    @Test
    void stemHandlesEdSuffix() {
        assertEquals("walk", tokenizer.stem("walked"));
        assertEquals("jump", tokenizer.stem("jumped"));
    }

    @Test
    void stemHandlesErSuffix() {
        assertEquals("teach", tokenizer.stem("teacher"));
        assertEquals("fast", tokenizer.stem("faster"));
    }

    @Test
    void stemHandlesEsSuffix() {
        assertEquals("box", tokenizer.stem("boxes"));
        assertEquals("wish", tokenizer.stem("wishes"));
    }

    @Test
    void stemHandlesSSuffix() {
        assertEquals("cat", tokenizer.stem("cats"));
        assertEquals("dog", tokenizer.stem("dogs"));
    }

    @Test
    void stemDoesNotStripDoubleS() {
        assertEquals("class", tokenizer.stem("class"));
        assertEquals("boss", tokenizer.stem("boss"));
    }

    @Test
    void stemReturnsShortWordsUnchanged() {
        assertEquals("the", tokenizer.stem("the"));
        assertEquals("a", tokenizer.stem("a"));
        assertEquals("ab", tokenizer.stem("ab"));
        assertEquals("abc", tokenizer.stem("abc"));
    }

    @Test
    void tokenizeWithNumbers() {
        List<String> tokens = tokenizer.tokenize("test 123 abc456");
        assertTrue(tokens.contains("test"));
        assertTrue(tokens.contains("123"));
        assertTrue(tokens.contains("abc456"));
    }

    @Test
    void tokenizeRemovesAllPunctuation() {
        List<String> tokens = tokenizer.tokenize("hello!!! world??? @#$%");
        assertEquals(2, tokens.size());
        assertEquals("hello", tokens.get(0));
        assertEquals("world", tokens.get(1));
    }

    @Test
    void tokenizeEmptyStringReturnsEmpty() {
        assertTrue(tokenizer.tokenize("").isEmpty());
    }
}