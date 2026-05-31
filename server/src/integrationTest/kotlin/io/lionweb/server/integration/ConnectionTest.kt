package io.lionweb.server.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Integration tests covering sign-on, sign-off, and reconnection lifecycles.
 *
 * Topology:
 *   Process 1 – LionWeb server        (port 13579)
 *   Process 2 – TestClientApp client1 (debug port [CLIENT1_DEBUG_PORT])
 *   Process 3 – TestClientApp client2 (debug port [CLIENT2_DEBUG_PORT])
 */
class ConnectionTest : BaseIntegrationTest() {
    // ── Sign-on ──────────────────────────────────────────────────────────────

    @Test
    fun `both clients sign on and receive a participation id`() {
        operateOnClients { debug1, debug2 ->
            val r1 = debug1.sendCommand("SIGN_ON")
            assertTrue(r1.startsWith("OK:"), "$CLIENT1_ID sign-on failed: $r1")

            val r2 = debug2.sendCommand("SIGN_ON")
            assertTrue(r2.startsWith("OK:"), "$CLIENT2_ID sign-on failed: $r2")
        }
    }

    @Test
    fun `each client receives a distinct participation id`() {
        operateOnClients { debug1, debug2 ->
            val pid1 = debug1.sendCommand("SIGN_ON").removePrefix("OK:")
            val pid2 = debug2.sendCommand("SIGN_ON").removePrefix("OK:")

            assertNotEquals(pid1, pid2, "two clients must not share a participationId")
        }
    }

    // ── Sign-off ─────────────────────────────────────────────────────────────

    @Test
    fun `client transitions to SIGNED_OFF after sign-off`() {
        operateOnClients { debug1, _ ->
            debug1.sendCommand("SIGN_ON")

            val r = debug1.sendCommand("SIGN_OFF")
            assertEquals("OK:SIGNED_OFF", r, "unexpected sign-off response: $r")
        }
    }

    @Test
    fun `client can sign on again after signing off and receives a new participation id`() {
        operateOnClients { debug1, _ ->
            val firstPid = debug1.sendCommand("SIGN_ON").removePrefix("OK:")

            debug1.sendCommand("SIGN_OFF")

            val secondPid = debug1.sendCommand("SIGN_ON").removePrefix("OK:")
            assertNotEquals(
                firstPid,
                secondPid,
                "re-sign-on must produce a fresh participationId",
            )
        }
    }

    // ── Reconnect ────────────────────────────────────────────────────────────

    @Test
    fun `client can reconnect with an existing participation id`() {
        operateOnClients { debug1, _ ->
            val pid = debug1.sendCommand("SIGN_ON").removePrefix("OK:")

            // RECONNECT resumes an active participation (simulates transport-level reconnect).
            // The participation is still active on the server because we did not sign off.
            val r = debug1.sendCommand("RECONNECT $pid 0")
            assertEquals(
                "OK:$pid",
                r,
                "reconnect should restore the same participationId",
            )
        }
    }

    @Test
    fun `reconnect fails when the participation id is unknown`() {
        operateOnClients { debug1, _ ->
            debug1.sendCommand("SIGN_ON")

            val r = debug1.sendCommand("RECONNECT unknown-participation-id 0")
            assertTrue(r.startsWith("ERROR:"), "expected an error for unknown participation, got: $r")
        }
    }

    @Test
    fun `reconnect fails after sign-off because the participation is no longer active`() {
        operateOnClients { debug1, _ ->
            val pid = debug1.sendCommand("SIGN_ON").removePrefix("OK:")
            debug1.sendCommand("SIGN_OFF")

            // The participation was dropped during sign-off, so RECONNECT must be rejected.
            val r = debug1.sendCommand("RECONNECT $pid 0")
            assertTrue(
                r.startsWith("ERROR:"),
                "expected an error reconnecting after sign-off, got: $r",
            )
        }
    }
}
