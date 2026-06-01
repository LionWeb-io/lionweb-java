package io.lionweb.server.integration

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.net.ServerSocket
import java.net.URI
import java.util.concurrent.TimeUnit

private const val REPOSITORY = "TestRepo"
private const val STARTUP_TIMEOUT_MS = 10_000L
const val CLIENT1_ID = "client-1"
const val CLIENT2_ID = "client-2"

abstract class BaseIntegrationTest {
    // Allocated fresh for each test so consecutive tests never share a port.
    // Fixed ports cause flaky failures: the OS may not release a port before
    // the next setUp tries to bind it again.
    private var serverPort = 0
    private var client1DebugPort = 0
    private var client2DebugPort = 0

    private val processes = mutableListOf<Process>()

    @BeforeEach
    fun setUp() {
        serverPort = freePort()
        client1DebugPort = freePort()
        client2DebugPort = freePort()

        val server =
            spawnProcess(
                "io.lionweb.server.LionWebServer",
                "--port",
                "$serverPort",
                "--repository",
                REPOSITORY,
            )
        processes += server
        awaitWebSocket("ws://localhost:$serverPort", "server")

        val client1 =
            spawnProcess(
                "io.lionweb.server.integration.TestClientApp",
                "--server-url",
                "ws://localhost:$serverPort",
                "--client-id",
                CLIENT1_ID,
                "--debug-port",
                "$client1DebugPort",
            )
        processes += client1
        awaitWebSocket("ws://localhost:$client1DebugPort", "$CLIENT1_ID debug")

        val client2 =
            spawnProcess(
                "io.lionweb.server.integration.TestClientApp",
                "--server-url",
                "ws://localhost:$serverPort",
                "--client-id",
                CLIENT2_ID,
                "--debug-port",
                "$client2DebugPort",
            )
        processes += client2
        awaitWebSocket("ws://localhost:$client2DebugPort", "$CLIENT2_ID debug")
    }

    @AfterEach
    fun tearDown() {
        processes.forEach { it.destroyForcibly() }
        processes.forEach { it.waitFor() }
        processes.clear()
    }

    protected fun spawnProcess(
        mainClass: String,
        vararg args: String,
    ): Process {
        val javaExe = "${System.getProperty("java.home")}/bin/java"
        val classpath = System.getProperty("java.class.path")
        return ProcessBuilder(javaExe, "-cp", classpath, mainClass, *args)
            .redirectErrorStream(true)
            .start()
            .also { drainOutput(it) }
    }

    /**
     * Drains the process stdout/stderr to a background thread so the process does not block when
     * its output pipe fills up.
     */
    private fun drainOutput(process: Process) {
        Thread { process.inputStream.bufferedReader().forEachLine { /* discard */ } }
            .apply {
                isDaemon = true
                start()
            }
    }

    /**
     * Retries a plain WebSocket connect until it succeeds or [STARTUP_TIMEOUT_MS] elapses. This is
     * the signal that the target process is ready to accept connections.
     */
    protected fun awaitWebSocket(
        url: String,
        label: String,
        timeoutMs: Long = STARTUP_TIMEOUT_MS,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            var connected = false
            try {
                val probe =
                    object : WebSocketClient(URI(url)) {
                        override fun onOpen(h: ServerHandshake) {}

                        override fun onClose(
                            c: Int,
                            r: String,
                            remote: Boolean,
                        ) {}

                        override fun onError(ex: Exception) {}

                        override fun onMessage(msg: String) {}
                    }
                if (probe.connectBlocking(1, TimeUnit.SECONDS)) {
                    connected = true
                    probe.closeBlocking()
                }
            } catch (_: Exception) {
            }

            if (connected) return
            Thread.sleep(100)
        }
        error("'$label' at $url did not become available within ${timeoutMs}ms")
    }

    protected fun operateOnClients(operation: (DebugClient, DebugClient) -> Unit) {
        val debug1 = DebugClient(URI("ws://localhost:$client1DebugPort"))
        val debug2 = DebugClient(URI("ws://localhost:$client2DebugPort"))
        try {
            debug1.connectBlocking()
            debug2.connectBlocking()
            operation(debug1, debug2)
        } finally {
            debug1.close()
            debug2.close()
        }
    }

    /** Asks the OS for a free port by binding to 0, records the assigned port, then releases it. */
    private fun freePort(): Int = ServerSocket(0).use { it.localPort }
}
