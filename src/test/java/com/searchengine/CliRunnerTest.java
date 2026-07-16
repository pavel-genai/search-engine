package com.searchengine;

import com.searchengine.cli.CliRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CliRunnerTest {

    private InputStream originalIn;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void saveStreams() {
        originalIn = System.in;
        originalOut = System.out;
        originalErr = System.err;
    }

    @AfterEach
    void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void noArgsPrintsUsageAndExitsOne() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        int code = CliRunner.runReturningExitCode(new String[]{});
        assertEquals(1, code);
        assertTrue(out.toString().contains("Usage"));
    }

    @Test
    void nonexistentDirectoryExitsOne() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        int code = CliRunner.runReturningExitCode(new String[]{"/no/such/dir/here"});
        assertEquals(1, code);
        assertTrue(err.toString().contains("not a directory"));
    }

    @Test
    void emptyDirectoryExitsZero(@TempDir Path tempDir) throws IOException {
        Files.createDirectory(tempDir.resolve("subdir"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        int code = CliRunner.runReturningExitCode(new String[]{tempDir.toString()});
        assertEquals(0, code);
        assertTrue(out.toString().contains("No .txt files found."));
    }

    @Test
    void indexesFilesAndSearches(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("a.txt"), "java spring programming");
        Files.writeString(tempDir.resolve("b.txt"), "python flask programming");

        String input = "programming\njava\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        int code = CliRunner.runReturningExitCode(new String[]{tempDir.toString()});
        assertEquals(0, code);
        String output = out.toString();

        assertTrue(output.contains("Indexed"), output);
        assertTrue(output.contains("Indexed " + 2 + " documents"), output);
        assertTrue(output.contains("search>"), output);
        assertTrue(output.contains("Found"), output);
        assertTrue(output.contains("java"), output);
        assertTrue(output.contains("Goodbye"), output);
    }

    @Test
    void emptyQueryIsSkipped(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("a.txt"), "hello world");
        String input = "\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        int code = CliRunner.runReturningExitCode(new String[]{tempDir.toString()});
        assertEquals(0, code);
        assertTrue(out.toString().contains("Goodbye"));
    }

    @Test
    void exitCommandAlsoQuits(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("a.txt"), "hello world");
        String input = "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        int code = CliRunner.runReturningExitCode(new String[]{tempDir.toString()});
        assertEquals(0, code);
        assertTrue(out.toString().contains("Goodbye"));
    }

    @Test
    void noResultsMessage(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("a.txt"), "hello world");
        String input = "nomatch\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        int code = CliRunner.runReturningExitCode(new String[]{tempDir.toString()});
        assertEquals(0, code);
        assertTrue(out.toString().contains("No results found."));
    }

    @Test
    void unreadableTxtFileLogsErrorAndContinues(@TempDir Path tempDir) throws IOException {
        // A .txt entry that is actually a directory causes Files.readString to
        // throw IOException, exercising the per-file catch block (lines 59-60).
        Files.createDirectory(tempDir.resolve("subdir.txt"));
        Files.writeString(tempDir.resolve("good.txt"), "hello world");
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));

        int code = CliRunner.runReturningExitCode(new String[]{tempDir.toString()});
        assertEquals(0, code);
        assertTrue(err.toString().contains("Error reading"), err.toString());
        assertTrue(out.toString().contains("Indexed 1 documents"), out.toString());
    }

    @Test
    void saveFailureLogsWarning(@TempDir Path tempDir) throws IOException {
        // Make the relative index-data path a regular file so that
        // IndexPersistence.save fails when trying to create its parent dir,
        // exercising the save catch block (lines 67-68).
        Files.writeString(tempDir.resolve("a.txt"), "hello world");
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));

        java.io.File indexData = new java.io.File("index-data");
        // Remove any existing index-data (dir or file) and recreate as a file
        // so save() cannot create the parent directory.
        deleteRecursively(indexData);
        assertTrue(indexData.createNewFile());
        try {
            int code = CliRunner.runReturningExitCode(new String[]{tempDir.toString()});
            assertEquals(0, code);
            assertTrue(err.toString().contains("Warning: could not save index"),
                    err.toString());
        } finally {
            deleteRecursively(indexData);
        }
    }

    private static void deleteRecursively(java.io.File f) {
        if (f.isDirectory()) {
            java.io.File[] children = f.listFiles();
            if (children != null) {
                for (java.io.File c : children) {
                    deleteRecursively(c);
                }
            }
        }
        f.delete();
    }
}