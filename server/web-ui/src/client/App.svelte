<script lang="ts">
  import MessageLogPanel from '../components/MessageLogPanel.svelte'
  import type { MessageLogEntry } from '../components/MessageLogPanel.svelte'

  interface PartitionInfo {
    id: string
  }

  interface ClientState {
    clientId: string
    serverUrl: string
    partitions: PartitionInfo[]
    messages: MessageLogEntry[]
  }

  let state = $state<ClientState | null>(null)
  let error = $state<string | null>(null)
  let newPartitionName = $state('')
  let actionError = $state<string | null>(null)

  async function loadState() {
    try {
      const resp = await fetch('./api/state')
      if (!resp.ok) throw new Error(`HTTP ${resp.status}: ${resp.statusText}`)
      state = await resp.json()
    } catch (e) {
      error = e instanceof Error ? e.message : String(e)
    }
  }

  async function createPartition() {
    actionError = null
    const name = newPartitionName.trim() || 'New Partition'
    try {
      const resp = await fetch('./api/action', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'create', name }),
      })
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
      newPartitionName = ''
      await loadState()
    } catch (e) {
      actionError = e instanceof Error ? e.message : String(e)
    }
  }

  async function deletePartition(partitionId: string) {
    actionError = null
    try {
      const resp = await fetch('./api/action', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'delete', partitionId }),
      })
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
      await loadState()
    } catch (e) {
      actionError = e instanceof Error ? e.message : String(e)
    }
  }

  $effect(() => {
    loadState()
    const interval = setInterval(loadState, 2000)
    return () => clearInterval(interval)
  })
</script>

<div class="layout">
  <header class="topbar">
    <div class="topbar-inner">
      <div class="brand">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" class="logo-icon">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <path d="M8 12h8M12 8v8" stroke="currentColor" stroke-width="2"/>
        </svg>
        <span>LionWeb Demo Client</span>
      </div>
      {#if state}
        <div class="conn-info">
          <span class="conn-label">Client:</span>
          <code>{state.clientId}</code>
          <span class="conn-sep">→</span>
          <code>{state.serverUrl}</code>
        </div>
      {/if}
    </div>
  </header>

  <main class="content">
    {#if error}
      <div class="alert-error">{error}</div>
    {/if}

    {#if actionError}
      <div class="alert-error">{actionError}</div>
    {/if}

    <div class="section-header">
      <h2>Partitions</h2>
      {#if state}
        <span class="count-badge">{state.partitions.length}</span>
      {/if}
    </div>

    <div class="create-row">
      <input
        type="text"
        placeholder="Partition name"
        bind:value={newPartitionName}
        class="partition-input"
        onkeydown={(e) => e.key === 'Enter' && createPartition()}
      />
      <button onclick={createPartition} class="btn-primary">Create Partition</button>
    </div>

    {#if state}
      {#if state.partitions.length === 0}
        <div class="empty-state">
          <p>No partitions yet. Create one above.</p>
        </div>
      {:else}
        <div class="partition-list">
          {#each state.partitions as partition}
            <div class="partition-row">
              <code class="partition-id">{partition.id}</code>
              <button
                class="btn-danger"
                onclick={() => deletePartition(partition.id)}
              >
                Delete
              </button>
            </div>
          {/each}
        </div>
      {/if}

      <MessageLogPanel messages={state.messages} />
    {/if}
  </main>
</div>

<style>
  .layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }

  .topbar {
    background: var(--color-surface);
    border-bottom: 1px solid var(--color-border);
    box-shadow: var(--shadow);
    position: sticky;
    top: 0;
    z-index: 10;
  }

  .topbar-inner {
    max-width: 1100px;
    margin: 0 auto;
    padding: 0 1.5rem;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
  }

  .brand {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 1.1rem;
    font-weight: 600;
    color: var(--color-primary);
  }

  .logo-icon {
    color: var(--color-primary);
    flex-shrink: 0;
  }

  .conn-info {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 0.8rem;
    color: var(--color-text-muted);
  }

  .conn-label {
    font-weight: 600;
  }

  .conn-sep {
    color: var(--color-border);
  }

  .content {
    max-width: 1100px;
    margin: 0 auto;
    padding: 2rem 1.5rem;
    flex: 1;
    width: 100%;
  }

  .alert-error {
    background: var(--color-error-bg);
    color: var(--color-error);
    border: 1px solid #fecaca;
    border-radius: var(--radius);
    padding: 12px 16px;
    margin-bottom: 1.5rem;
    font-size: 0.875rem;
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 1.25rem;
  }

  .section-header h2 {
    font-size: 1.25rem;
    font-weight: 600;
  }

  .count-badge {
    background: var(--color-badge);
    color: var(--color-badge-text);
    font-size: 0.8rem;
    font-weight: 600;
    padding: 2px 8px;
    border-radius: 99px;
  }

  .create-row {
    display: flex;
    gap: 10px;
    margin-bottom: 1.25rem;
  }

  .partition-input {
    flex: 1;
    padding: 8px 12px;
    border: 1px solid var(--color-border);
    border-radius: var(--radius);
    font-size: 0.875rem;
    outline: none;
    transition: border-color 0.15s;
  }

  .partition-input:focus {
    border-color: var(--color-primary);
  }

  .btn-primary {
    padding: 8px 16px;
    background: var(--color-primary);
    color: #fff;
    border: none;
    border-radius: var(--radius);
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    white-space: nowrap;
    transition: background 0.15s;
  }

  .btn-primary:hover {
    background: var(--color-primary-dark);
  }

  .empty-state {
    text-align: center;
    padding: 3rem;
    color: var(--color-text-muted);
    background: var(--color-surface);
    border: 1px dashed var(--color-border);
    border-radius: var(--radius);
  }

  .partition-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 0.5rem;
  }

  .partition-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: var(--color-surface);
    border: 1px solid var(--color-border);
    border-radius: var(--radius);
    padding: 10px 14px;
    gap: 1rem;
  }

  .partition-id {
    font-size: 0.825rem;
    word-break: break-all;
    flex: 1;
  }

  .btn-danger {
    padding: 5px 12px;
    background: #fee2e2;
    color: #dc2626;
    border: 1px solid #fecaca;
    border-radius: var(--radius);
    font-size: 0.8rem;
    font-weight: 500;
    cursor: pointer;
    white-space: nowrap;
    transition: background 0.15s;
  }

  .btn-danger:hover {
    background: #fecaca;
  }
</style>
