package com.searchengine;

import com.searchengine.SearchEngineApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.Permission;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the --cli branch of SearchEngineApplication.main without actually
 * terminating the JVM. A custom SecurityManager intercepts System.exit and
 * throws an exception instead, allowing the test to observe the cli dispatch.
 */
class SearchEngineApplicationTest {

    private InputStream originalIn;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private SecurityManager originalSm;

    @BeforeEach
    void saveStreams() {
        originalIn = System.in;
        originalOut = System.out;
        originalErr = System.err;
        originalSm = System.getSecurityManager();
    }

    @AfterEach
    void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setSecurityManager(originalSm);
    }

    @Test
    void mainWithCliFlagDispatchesToCliRunner() {
        // Capture stdout so we can confirm CliRunner's usage message is printed.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        // Install a SecurityManager that blocks System.exit by throwing instead.
        // CliRunner.run calls System.exit(runReturningExitCode(args)); with no
        // remaining args runReturningExitCode returns 1, so exit(1) is blocked.
        SecurityManager blocker = new SecurityManager() {
            @Override
            public void checkExit(int status) {
                throw new SecurityException("exit blocked: " + status);
            }

            @Override
            public void checkPermission(Permission perm) {
                // Allow everything else.
            }
        };
        System.setSecurityManager(blocker);

        // main with --cli and no remaining args -> CliRunner prints usage and
        // calls System.exit(1), which our SecurityManager blocks.
        assertThrows(SecurityException.class,
                () -> SearchEngineApplication.main(new String[]{"--cli"}));
        assertTrue(out.toString().contains("Usage"), out.toString());
    }
}