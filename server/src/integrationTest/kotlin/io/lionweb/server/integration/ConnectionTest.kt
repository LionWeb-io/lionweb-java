package io.lionweb.server.integration

import java.net.URI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Multi-process integration test for the LionWeb delta server.
 *
 * Topology:
 *   Process 1  –  LionWeb server        (port [SERVER_PORT])
 *   Process 2  –  TestClientApp client1 (debug port [CLIENT1_DEBUG_PORT])
 *   Process 3  –  TestClientApp client2 (debug port [CLIENT2_DEBUG_PORT])
 *
 * The JUnit process connects to each client's debug WebSocket to drive the test scenario and
 * verify outcomes.
 */
class ConnectionTest : BaseIntegrationTest() {
    @Test
    fun `both clients sign on and receive a participation id`() {
        operateOnClients { debug1, debug2 ->

            val r1 = debug1.sendCommand("SIGN_ON")
            println("$CLIENT1_ID sign-on response: $r1")
            assertTrue(r1.startsWith("OK:"), "$CLIENT1_ID sign-on failed: $r1")

            val r2 = debug2.sendCommand("SIGN_ON")
            println("$CLIENT2_ID sign-on response: $r2")
            assertTrue(r2.startsWith("OK:"), "$CLIENT2_ID sign-on failed: $r2")
        }
    }

}
