<script lang="ts">
  export interface MessageLogEntry {
    timestamp: number
    direction: string
    category: string
    messageKind: string
    clientId?: string | null
    participationId?: string | null
    json: string
  }

  let { messages = [] }: { messages: MessageLogEntry[] } = $props()

  function formatTime(ts: number): string {
    return new Date(ts).toLocaleTimeString()
  }

  function directionColor(entry: MessageLogEntry): string {
    if (entry.category === 'error') return 'entry-error'
    if (entry.direction === 'sent') return 'entry-sent'
    return 'entry-received'
  }

  let expanded = $state<Record<number, boolean>>({})

  function toggle(idx: number) {
    expanded[idx] = !expanded[idx]
  }

  function prettyJson(json: string): string {
    try {
      return JSON.stringify(JSON.parse(json), null, 2)
    } catch {
      return json
    }
  }
</script>

<div class="log-panel">
  <div class="log-header">
    <span class="log-title">Message Log</span>
    <span class="log-count">{messages.length}</span>
  </div>
  {#if messages.length === 0}
    <div class="log-empty">No messages yet.</div>
  {:else}
    <div class="log-list">
      {#each messages as entry, idx}
        <div class="log-entry {directionColor(entry)}">
          <button class="entry-toggle" onclick={() => toggle(idx)}>
            <span class="entry-time">{formatTime(entry.timestamp)}</span>
            <span class="entry-dir">{entry.direction}</span>
            <span class="entry-kind">{entry.messageKind}</span>
            {#if entry.clientId}
              <span class="entry-client">{entry.clientId}</span>
            {/if}
            {#if entry.participationId}
              <span class="entry-pid">{entry.participationId}</span>
            {/if}
            <span class="entry-caret">{expanded[idx] ? '▲' : '▼'}</span>
          </button>
          {#if expanded[idx]}
            <pre class="entry-json">{prettyJson(entry.json)}</pre>
          {/if}
        </div>
      {/each}
    </div>
  {/if}
</div>

<style>
  .log-panel {
    background: var(--color-surface);
    border: 1px solid var(--color-border);
    border-radius: var(--radius);
    overflow: hidden;
    margin-top: 1.5rem;
  }

  .log-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 14px;
    background: var(--color-bg);
    border-bottom: 1px solid var(--color-border);
  }

  .log-title {
    font-size: 0.875rem;
    font-weight: 600;
  }

  .log-count {
    background: var(--color-badge);
    color: var(--color-badge-text);
    font-size: 0.75rem;
    font-weight: 600;
    padding: 1px 7px;
    border-radius: 99px;
  }

  .log-empty {
    padding: 1.5rem;
    text-align: center;
    color: var(--color-text-muted);
    font-size: 0.875rem;
  }

  .log-list {
    max-height: 400px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
  }

  .log-entry {
    border-bottom: 1px solid var(--color-border);
  }

  .log-entry:last-child {
    border-bottom: none;
  }

  .entry-sent {
    border-left: 3px solid #3b82f6;
  }

  .entry-received {
    border-left: 3px solid #22c55e;
  }

  .entry-error {
    border-left: 3px solid #ef4444;
  }

  .entry-toggle {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;
    padding: 7px 12px;
    background: none;
    border: none;
    cursor: pointer;
    text-align: left;
    font-size: 0.8rem;
  }

  .entry-toggle:hover {
    background: #f8fafc;
  }

  .entry-time {
    color: var(--color-text-muted);
    font-variant-numeric: tabular-nums;
    min-width: 70px;
  }

  .entry-dir {
    font-weight: 600;
    min-width: 55px;
  }

  .entry-sent .entry-dir {
    color: #3b82f6;
  }

  .entry-received .entry-dir {
    color: #22c55e;
  }

  .entry-error .entry-dir {
    color: #ef4444;
  }

  .entry-kind {
    font-family: 'SFMono-Regular', Consolas, monospace;
    font-size: 0.78rem;
    flex: 1;
  }

  .entry-client {
    font-size: 0.78rem;
    font-weight: 600;
    color: var(--color-text);
    font-family: 'SFMono-Regular', Consolas, monospace;
  }

  .entry-pid {
    color: var(--color-text-muted);
    font-size: 0.75rem;
    font-family: monospace;
  }

  .entry-caret {
    color: var(--color-text-muted);
    font-size: 0.65rem;
  }

  .entry-json {
    padding: 8px 12px;
    font-size: 0.75rem;
    background: #f8fafc;
    border-top: 1px solid var(--color-border);
    overflow-x: auto;
    white-space: pre-wrap;
    word-break: break-all;
    max-height: 200px;
    overflow-y: auto;
  }
</style>
