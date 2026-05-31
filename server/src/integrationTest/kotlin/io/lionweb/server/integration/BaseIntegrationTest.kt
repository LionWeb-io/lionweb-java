package io.lionweb.server.integration

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.net.URI
import java.util.concurrent.TimeUnit

private const val SERVER_PORT = 13579
const val CLIENT1_DEBUG_PORT = 13580
const val CLIENT2_DEBUG_PORT = 13581
private const val REPOSITORY = "TestRepo"
private const val STARTUP_TIMEOUT_MS = 10_000L
const val CLIENT1_ID = "client-1"
const val CLIENT2_ID = "client-2"

abstract class BaseIntegrationTest {

    private val processes = mutableListOf<Process>()

    @BeforeEach
    fun setUp() {
        val server = spawnProcess(
            "io.lionweb.server.LionWebServer",
            "--port", "$SERVER_PORT",
            "--repository", REPOSITORY,
        )
        processes += server
        awaitWebSocket("ws://localhost:$SERVER_PORT", "server")

        val client1 = spawnProcess(
            "io.lionweb.server.integration.TestClientApp",
            "--server-url", "ws://localhost:$SERVER_PORT",
            "--client-id", CLIENT1_ID,
            "--debug-port", "$CLIENT1_DEBUG_PORT",
        )
        processes += client1
        awaitWebSocket("ws://localhost:$CLIENT1_DEBUG_PORT", "$CLIENT1_ID debug")

        val client2 = spawnProcess(
            "io.lionweb.server.integration.TestClientApp",
            "--server-url", "ws://localhost:$SERVER_PORT",
            "--client-id", CLIENT2_ID,
            "--debug-port", "$CLIENT2_DEBUG_PORT",
        )
        processes += client2
        awaitWebSocket("ws://localhost:$CLIENT2_DEBUG_PORT", "$CLIENT2_ID debug")
    }

    @AfterEach
    fun tearDown() {
        processes.forEach { it.destroyForcibly() }
        processes.clear()
    }

    protected fun spawnProcess(mainClass: String, vararg args: String): Process {
        val javaExe = "${System.getProperty("java.home")}/bin/java"
        val classpath = System.getProperty("java.class.path")
        return ProcessBuilder(javaExe, "-cp", classpath, mainClass, *args)
            .redirectErrorStream(true)
            .start().also { drainOutput(it) }
    }

    /**
     * Drains the process stdout/stderr to a background thread so the process does not block when
     * its output pipe fills up.
     */
    protected fun drainOutput(process: Process) {
        Thread {
            process.inputStream.bufferedReader().forEachLine { /* discard */ }
        }.apply { isDaemon = true; start() }
    }

    /**
     * Retries a plain WebSocket connect until it succeeds or [STARTUP_TIMEOUT_MS] elapses.
     * This is the signal that the target process is ready to accept connections.
     */
    protected fun awaitWebSocket(url: String, label: String, timeoutMs: Long = STARTUP_TIMEOUT_MS) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            var connected = false
            try {
                val probe = object : WebSocketClient(URI(url)) {
                    override fun onOpen(h: ServerHandshake) {}
                    override fun onClose(c: Int, r: String, remote: Boolean) {}
                    override fun onError(ex: Exception) {}
                    override fun onMessage(msg: String) {}
                }
                if (probe.connectBlocking(1, TimeUnit.SECONDS)) {
                    connected = true
                    probe.closeBlocking()
                }
            } catch (_: Exception) {}

            if (connected) return
            Thread.sleep(100)
        }
        error("'$label' at $url did not become available within ${timeoutMs}ms")
    }

    protected fun operateOnClients(operation: (DebugClient, DebugClient) -> Unit) {
        val debug1 = DebugClient(URI("ws://localhost:$CLIENT1_DEBUG_PORT"))
        val debug2 = DebugClient(URI("ws://localhost:$CLIENT2_DEBUG_PORT"))
        try {
            debug1.connectBlocking()
            debug2.connectBlocking()

            operation(debug1, debug2)
        } finally {
            debug1.close()
            debug2.close()
        }
    }
}
