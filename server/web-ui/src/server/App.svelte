<script lang="ts">
  import MessageLogPanel from '../components/MessageLogPanel.svelte'
  import type { MessageLogEntry } from '../components/MessageLogPanel.svelte'

  interface ClassifierInfo {
    languageKey: string
    classifierKey: string
    totalCount: number
    sampleIds: string[]
  }

  interface PartitionInfo {
    id: string
    classifierKey?: string | null
    classifierLanguageKey?: string | null
  }

  interface RepositoryInfo {
    name: string
    lionWebVersion: string
    historySupport: string
    partitions: PartitionInfo[]
    classifiers: ClassifierInfo[]
  }

  interface ServerData {
    repositories: RepositoryInfo[]
  }

  let data = $state<ServerData | null>(null)
  let loading = $state(false)
  let error = $state<string | null>(null)
  let lastRefresh = $state<Date | null>(null)
  let messages = $state<MessageLogEntry[]>([])

  async function loadRepositories() {
    loading = true
    error = null
    try {
      const resp = await fetch('./api/data')
      if (!resp.ok) throw new Error(`HTTP ${resp.status}: ${resp.statusText}`)
      data = await resp.json()
      lastRefresh = new Date()
    } catch (e) {
      error = e instanceof Error ? e.message : String(e)
    } finally {
      loading = false
    }
  }

  async function loadMessages() {
    try {
      const resp = await fetch('./api/messages')
      if (resp.ok) messages = await resp.json()
    } catch {
      // silently ignore polling errors
    }
  }

  function totalNodes(repo: RepositoryInfo): number {
    return repo.classifiers.reduce((sum, c) => sum + c.totalCount, 0)
  }

  $effect(() => {
    loadRepositories()
    loadMessages()
    const interval = setInterval(loadMessages, 2000)
    return () => clearInterval(interval)
  })
</script>

<div class="layout">
  <header class="topbar">
    <div class="topbar-inner">
      <div class="brand">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" class="logo-icon">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <circle cx="12" cy="12" r="4" fill="currentColor"/>
          <line x1="12" y1="2" x2="12" y2="8" stroke="currentColor" stroke-width="2"/>
          <line x1="12" y1="16" x2="12" y2="22" stroke="currentColor" stroke-width="2"/>
          <line x1="2" y1="12" x2="8" y2="12" stroke="currentColor" stroke-width="2"/>
          <line x1="16" y1="12" x2="22" y2="12" stroke="currentColor" stroke-width="2"/>
        </svg>
        <span>LionWeb JVM Server</span>
      </div>
    </div>
  </header>

  <main class="content">
    {#if error}
      <div class="alert-error">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        {error}
      </div>
    {/if}

    {#if loading && !data}
      <div class="skeleton-container">
        {#each [1, 2] as _}
          <div class="skeleton-card">
            <div class="skeleton-line wide"></div>
            <div class="skeleton-line medium"></div>
            <div class="skeleton-stats">
              {#each [1, 2, 3] as __}
                <div class="skeleton-stat"></div>
              {/each}
            </div>
          </div>
        {/each}
      </div>
    {/if}

    {#if data}
      <div class="section-header">
        <h2>Repositories</h2>
        <span class="count-badge">{data.repositories.length}</span>
        <div class="section-actions">
          {#if lastRefresh}
            <span class="last-refresh">Refreshed {lastRefresh.toLocaleTimeString()}</span>
          {/if}
          <button onclick={loadRepositories} disabled={loading} class="btn-refresh">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class:spinning={loading}>
              <path d="M21 2v6h-6M3 12a9 9 0 0 1 15-6.7L21 8M3 22v-6h6M21 12a9 9 0 0 1-15 6.7L3 16"/>
            </svg>
            {loading ? 'Loading…' : 'Refresh'}
          </button>
        </div>
      </div>

      {#if data.repositories.length === 0}
        <div class="empty-state">
          <p>No repositories found.</p>
        </div>
      {:else}
        <div class="repo-grid">
          {#each data.repositories as repo}
            <div class="repo-card">
              <div class="repo-card-header">
                <h3 class="repo-name">{repo.name}</h3>
                <div class="badges">
                  <span class="badge primary">{repo.lionWebVersion}</span>
                  <span class="badge secondary">{repo.historySupport}</span>
                </div>
              </div>

              <div class="stats-row">
                <div class="stat-box">
                  <span class="stat-num">{repo.partitions.length}</span>
                  <span class="stat-lbl">Partitions</span>
                </div>
                <div class="stat-box">
                  <span class="stat-num">{totalNodes(repo)}</span>
                  <span class="stat-lbl">Total Nodes</span>
                </div>
                <div class="stat-box">
                  <span class="stat-num">{repo.classifiers.length}</span>
                  <span class="stat-lbl">Classifiers</span>
                </div>
              </div>

              {#if repo.partitions.length > 0}
                <details class="collapsible">
                  <summary>Partitions ({repo.partitions.length})</summary>
                  <ul class="id-list">
                    {#each repo.partitions as p}
                      <li>
                        <code>{p.id}</code>
                        {#if p.classifierKey}
                          <span class="partition-classifier">{p.classifierKey}</span>
                        {/if}
                      </li>
                    {/each}
                  </ul>
                </details>
              {/if}

              {#if repo.classifiers.length > 0}
                <details class="collapsible">
                  <summary>Nodes by Classifier ({repo.classifiers.length})</summary>
                  <div class="table-wrap">
                    <table class="data-table">
                      <thead>
                        <tr>
                          <th>Language</th>
                          <th>Classifier</th>
                          <th class="num-col">Count</th>
                        </tr>
                      </thead>
                      <tbody>
                        {#each [...repo.classifiers].sort((a, b) => b.totalCount - a.totalCount) as cls}
                          <tr>
                            <td><code>{cls.languageKey}</code></td>
                            <td><code>{cls.classifierKey}</code></td>
                            <td class="num-col">{cls.totalCount}</td>
                          </tr>
                        {/each}
                      </tbody>
                    </table>
                  </div>
                </details>
              {/if}
            </div>
          {/each}
        </div>
      {/if}
    {/if}

    <MessageLogPanel {messages} />
  </main>
</div>

<style>
  .layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }

  /* ── Topbar ── */
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

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  .spinning {
    animation: spin 0.8s linear infinite;
  }

  /* ── Content ── */
  .content {
    max-width: 1100px;
    margin: 0 auto;
    padding: 2rem 1.5rem;
    flex: 1;
    width: 100%;
  }

  /* ── Alert ── */
  .alert-error {
    display: flex;
    align-items: center;
    gap: 8px;
    background: var(--color-error-bg);
    color: var(--color-error);
    border: 1px solid #fecaca;
    border-radius: var(--radius);
    padding: 12px 16px;
    margin-bottom: 1.5rem;
    font-size: 0.875rem;
  }

  /* ── Skeleton ── */
  .skeleton-container {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .skeleton-card {
    background: var(--color-surface);
    border: 1px solid var(--color-border);
    border-radius: var(--radius);
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .skeleton-line {
    height: 16px;
    background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.4s infinite;
    border-radius: 4px;
  }

  .skeleton-line.wide { width: 50%; }
  .skeleton-line.medium { width: 30%; }

  .skeleton-stats {
    display: flex;
    gap: 1rem;
  }

  .skeleton-stat {
    height: 60px;
    flex: 1;
    background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.4s infinite;
    border-radius: var(--radius);
  }

  @keyframes shimmer {
    to { background-position: -200% 0; }
  }

  /* ── Section header ── */
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

  .section-actions {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-left: auto;
  }

  .last-refresh {
    font-size: 0.8rem;
    color: var(--color-text-muted);
  }

  .btn-refresh {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 5px 12px;
    background: var(--color-primary);
    color: #fff;
    border: none;
    border-radius: var(--radius);
    font-size: 0.8rem;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.15s;
  }

  .btn-refresh:hover:not(:disabled) {
    background: var(--color-primary-dark);
  }

  .btn-refresh:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  /* ── Empty state ── */
  .empty-state {
    text-align: center;
    padding: 3rem;
    color: var(--color-text-muted);
    background: var(--color-surface);
    border: 1px dashed var(--color-border);
    border-radius: var(--radius);
  }

  /* ── Repo grid ── */
  .repo-grid {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .repo-card {
    background: var(--color-surface);
    border: 1px solid var(--color-border);
    border-radius: var(--radius);
    box-shadow: var(--shadow);
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .repo-card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
    flex-wrap: wrap;
  }

  .repo-name {
    font-size: 1.1rem;
    font-weight: 600;
  }

  .badges {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }

  .badge {
    font-size: 0.75rem;
    font-weight: 500;
    padding: 2px 10px;
    border-radius: 99px;
  }

  .badge.primary {
    background: var(--color-badge);
    color: var(--color-badge-text);
  }

  .badge.secondary {
    background: var(--color-badge-secondary);
    color: var(--color-badge-secondary-text);
  }

  /* ── Stats ── */
  .stats-row {
    display: flex;
    gap: 1rem;
  }

  .stat-box {
    flex: 1;
    background: var(--color-bg);
    border: 1px solid var(--color-border);
    border-radius: var(--radius);
    padding: 0.75rem 1rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
  }

  .stat-num {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--color-primary);
    line-height: 1;
  }

  .stat-lbl {
    font-size: 0.75rem;
    color: var(--color-text-muted);
    text-transform: uppercase;
    letter-spacing: 0.04em;
  }

  /* ── Collapsible ── */
  .collapsible {
    border: 1px solid var(--color-border);
    border-radius: var(--radius);
    overflow: hidden;
  }

  .collapsible summary {
    padding: 10px 14px;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    user-select: none;
    background: var(--color-bg);
    list-style: none;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .collapsible summary::-webkit-details-marker {
    display: none;
  }

  .collapsible summary::before {
    content: '▶';
    font-size: 0.6em;
    transition: transform 0.15s;
  }

  .collapsible[open] summary::before {
    transform: rotate(90deg);
  }

  .collapsible summary:hover {
    background: #f1f5f9;
  }

  /* ── ID list ── */
  .id-list {
    list-style: none;
    padding: 10px 14px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    border-top: 1px solid var(--color-border);
    max-height: 220px;
    overflow-y: auto;
  }

  .id-list li {
    font-size: 0.825rem;
    padding: 2px 0;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .partition-classifier {
    font-size: 0.75rem;
    color: var(--color-primary);
    font-weight: 500;
  }

  /* ── Table ── */
  .table-wrap {
    overflow-x: auto;
    border-top: 1px solid var(--color-border);
  }

  .data-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.825rem;
  }

  .data-table th {
    text-align: left;
    padding: 8px 14px;
    font-weight: 600;
    background: #f8fafc;
    border-bottom: 1px solid var(--color-border);
    color: var(--color-text-muted);
    font-size: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.04em;
  }

  .data-table td {
    padding: 7px 14px;
    border-bottom: 1px solid #f1f5f9;
  }

  .data-table tr:last-child td {
    border-bottom: none;
  }

  .data-table tr:hover td {
    background: #f8fafc;
  }

  .num-col {
    text-align: right;
    font-variant-numeric: tabular-nums;
    font-weight: 600;
    color: var(--color-primary);
  }
</style>
