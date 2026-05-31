package io.lionweb.server.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Integration tests verifying that partition lifecycle events and node-content changes issued by
 * Client1 are eventually reflected in Client2's view of the server.
 *
 * Topology:
 *   Process 1 – LionWeb server        (port [serverPort])
 *   Process 2 – TestClientApp client1 (debug port [CLIENT1_DEBUG_PORT])
 *   Process 3 – TestClientApp client2 (debug port [CLIENT2_DEBUG_PORT])
 */
class PartitionSyncTest : BaseIntegrationTest() {

    /**
     * End-to-end scenario for partition creation and node synchronisation:
     * 1. Client2 subscribes to partition-list events.
     * 2. Client1 creates a partition.
     * 3. Client2 waits until the partition appears in the server list, then subscribes to its contents.
     * 4. Client1 adds two nodes after Client2 is subscribed.
     * 5. Client2 waits for both nodes to appear in its local model via delta events.
     * 6. Client2's tracked node IDs must include the partition root and both nodes.
     */
    @Test
    fun `client2 tracks partition created by client1 and sees nodes added via events`() {
        operateOnClients { debug1, debug2 ->
            val r1 = debug1.sendCommand("SIGN_ON")
            assertTrue(r1.startsWith("OK:"), "client1 sign-on failed: $r1")

            val r2 = debug2.sendCommand("SIGN_ON")
            assertTrue(r2.startsWith("OK:"), "client2 sign-on failed: $r2")

            // Client2 registers for partition-list events.
            val listResp = debug2.sendCommand("LIST_AND_SUBSCRIBE_PARTITIONS")
            assertTrue(listResp.startsWith("OK:"), "LIST_AND_SUBSCRIBE_PARTITIONS failed: $listResp")

            // Client1 creates the partition.
            val createResp = debug1.sendCommand("CREATE_PARTITION part-sync-1 TestPartition")
            assertEquals("OK", createResp, "CREATE_PARTITION failed: $createResp")

            // Client2 polls until the partition appears on the server, then subscribes to its
            // contents and starts tracking it locally.
            val awaitPartition = debug2.sendCommand("AWAIT_PARTITION_LISTED part-sync-1 3000")
            assertEquals("OK", awaitPartition, "partition did not appear on server: $awaitPartition")

            val subResp = debug2.sendCommand("SUBSCRIBE_PARTITION_CONTENTS part-sync-1")
            assertTrue(subResp.startsWith("OK:"), "SUBSCRIBE_PARTITION_CONTENTS failed: $subResp")

            // Client1 adds two nodes after Client2 is subscribed; they arrive as delta events.
            val addResp1 = debug1.sendCommand("ADD_NODE part-sync-1 node-1 TestNode")
            assertEquals("OK", addResp1, "ADD_NODE node-1 failed: $addResp1")

            val addResp2 = debug1.sendCommand("ADD_NODE part-sync-1 node-2 TestNode")
            assertEquals("OK", addResp2, "ADD_NODE node-2 failed: $addResp2")

            // Wait for the events to propagate and be applied.
            val await1 = debug2.sendCommand("AWAIT_NODE part-sync-1 node-1 3000")
            assertEquals("OK", await1, "node-1 did not propagate to client2: $await1")

            val await2 = debug2.sendCommand("AWAIT_NODE part-sync-1 node-2 3000")
            assertEquals("OK", await2, "node-2 did not propagate to client2: $await2")

            // Verify Client2's local model contains all expected nodes.
            val idsResp = debug2.sendCommand("GET_TRACKED_NODE_IDS part-sync-1")
            assertTrue(idsResp.startsWith("OK:"), "GET_TRACKED_NODE_IDS failed: $idsResp")
            val idsJson = idsResp.removePrefix("OK:")
            assertTrue(idsJson.contains("\"part-sync-1\""), "partition root not tracked: $idsJson")
            assertTrue(idsJson.contains("\"node-1\""), "node-1 not tracked: $idsJson")
            assertTrue(idsJson.contains("\"node-2\""), "node-2 not tracked: $idsJson")
        }
    }

    /**
     * End-to-end scenario for partition deletion:
     * 1. Client1 creates a partition; Client2 waits for it to appear and subscribes.
     * 2. Client1 deletes the partition.
     * 3. Client2 polls until the partition disappears from the server's list.
     */
    @Test
    fun `client2 observes that a partition deleted by client1 is gone from the server`() {
        operateOnClients { debug1, debug2 ->
            val r1 = debug1.sendCommand("SIGN_ON")
            assertTrue(r1.startsWith("OK:"), "client1 sign-on failed: $r1")

            val r2 = debug2.sendCommand("SIGN_ON")
            assertTrue(r2.startsWith("OK:"), "client2 sign-on failed: $r2")

            // Client2 subscribes to partition-list events so it hears about lifecycle changes.
            val listResp = debug2.sendCommand("LIST_AND_SUBSCRIBE_PARTITIONS")
            assertTrue(listResp.startsWith("OK:"), "LIST_AND_SUBSCRIBE_PARTITIONS failed: $listResp")

            // Client1 creates the partition; Client2 waits until it appears.
            val createResp = debug1.sendCommand("CREATE_PARTITION part-del-1 DeleteMe")
            assertEquals("OK", createResp, "CREATE_PARTITION failed: $createResp")

            val awaitCreated = debug2.sendCommand("AWAIT_PARTITION_LISTED part-del-1 3000")
            assertEquals("OK", awaitCreated, "partition did not appear on server: $awaitCreated")

            // Client2 subscribes to the partition contents (verifying it exists).
            val subResp = debug2.sendCommand("SUBSCRIBE_PARTITION_CONTENTS part-del-1")
            assertTrue(subResp.startsWith("OK:"), "SUBSCRIBE_PARTITION_CONTENTS failed: $subResp")

            // Client1 deletes the partition.
            val deleteResp = debug1.sendCommand("DELETE_PARTITION part-del-1")
            assertEquals("OK", deleteResp, "DELETE_PARTITION failed: $deleteResp")

            // Client2 polls until the partition is gone from the server list.
            val awaitGone = debug2.sendCommand("AWAIT_PARTITION_GONE part-del-1 3000")
            assertEquals("OK", awaitGone, "partition did not disappear from server: $awaitGone")
        }
    }
}
