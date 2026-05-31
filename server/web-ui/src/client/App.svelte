<script lang="ts">
  import MessageLogPanel from '../components/MessageLogPanel.svelte'
  import type { MessageLogEntry } from '../components/MessageLogPanel.svelte'

  interface ContainmentInfo {
    key: string
    name: string
    languageKey: string | null
    languageVersion: string | null
    multiple: boolean
    optional: boolean
    typeKey: string | null
    typeLanguageKey: string | null
  }

  interface PropertyInfo {
    key: string
    name: string
    languageKey: string | null
    languageVersion: string | null
    optional: boolean
    typeKey: string | null
    typeName: string | null
  }

  interface ConceptInfo {
    key: string
    name: string
    languageName: string
    languageKey: string
    languageVersion: string
    isPartition: boolean
    containments: ContainmentInfo[]
    properties: PropertyInfo[]
  }

  interface StoredPropertyValue {
    key: string
    languageKey: string | null
    languageVersion: string | null
    value: string | null
  }

  interface NodeInfo {
    id: string
    classifierKey: string | null
    classifierLanguageKey: string | null
    classifierLanguageVersion: string | null
    parentId: string | null
    containmentKey: string | null
    containmentLanguageKey: string | null
    containmentLanguageVersion: string | null
    properties: StoredPropertyValue[]
    children: Record<string, string[]>
  }

  interface PartitionInfo {
    id: string
    classifierKey?: string | null
    classifierLanguageKey?: string | null
  }

  interface ClientState {
    clientId: string
    serverUrl: string
    partitions: PartitionInfo[]
    messages: MessageLogEntry[]
    concepts: ConceptInfo[]
    nodes: NodeInfo[]
  }

  let state = $state<ClientState | null>(null)
  let error = $state<string | null>(null)
  let selectedConceptKey = $state<string>('')
  let actionError = $state<string | null>(null)
  let expandedNodes = $state<Set<string>>(new Set())
  // parentId -> { containmentKey, conceptKey }
  let addChildState = $state<Record<string, { containmentKey: string; conceptKey: string }>>({})
  // nodeId -> propKey -> draft string value
  let editingProps = $state<Record<string, Record<string, string>>>({})

  $effect(() => {
    if (state?.concepts && state.concepts.length > 0 && !selectedConceptKey) {
      const pc = state.concepts.find(c => c.isPartition)
      selectedConceptKey = (pc ?? state.concepts[0]).key + '|' + (pc ?? state.concepts[0]).languageKey
    }
  })

  function getNodesMap(): Map<string, NodeInfo> {
    if (!state) return new Map()
    return new Map(state.nodes.map(n => [n.id, n]))
  }

  function conceptFor(node: NodeInfo): ConceptInfo | undefined {
    return state?.concepts.find(c => c.key === node.classifierKey && c.languageKey === node.classifierLanguageKey)
  }

  function candidateConcepts(): ConceptInfo[] {
    return state?.concepts.filter(c => !c.isPartition) ?? []
  }

  function toggleExpanded(nodeId: string) {
    const next = new Set(expandedNodes)
    if (next.has(nodeId)) next.delete(nodeId)
    else next.add(nodeId)
    expandedNodes = next
  }

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
    const [conceptKey, languageKey] = selectedConceptKey.split('|')
    if (!conceptKey || !languageKey) { actionError = 'Please select a concept'; return }
    try {
      const resp = await fetch('./api/action', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: 'create', conceptKey, languageKey }),
      })
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
      await loadState()
    } catch (e) { actionError = e instanceof Error ? e.message : String(e) }
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
    } catch (e) { actionError = e instanceof Error ? e.message : String(e) }
  }

  function beginAddChild(parentId: string, containmentKey: string) {
    const first = candidateConcepts()[0]
    addChildState = {
      ...addChildState,
      [parentId]: { containmentKey, conceptKey: first ? first.key + '|' + first.languageKey : '' },
    }
  }

  function cancelAddChild(parentId: string) {
    const next = { ...addChildState }
    delete next[parentId]
    addChildState = next
  }

  async function doAddChild(parentId: string, cont: ContainmentInfo) {
    actionError = null
    const st = addChildState[parentId]
    if (!st?.conceptKey) { actionError = 'Select a concept'; return }
    const [conceptKey, languageKey] = st.conceptKey.split('|')
    try {
      const resp = await fetch('./api/action', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          type: 'addChild',
          parentId,
          containmentKey: cont.key,
          containmentLanguageKey: cont.languageKey,
          containmentLanguageVersion: cont.languageVersion,
          conceptKey,
          languageKey,
        }),
      })
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
      cancelAddChild(parentId)
      await loadState()
    } catch (e) { actionError = e instanceof Error ? e.message : String(e) }
  }

  async function doDeleteChild(
    nodeId: string, parentId: string,
    cont: ContainmentInfo, index: number,
  ) {
    actionError = null
    try {
      const resp = await fetch('./api/action', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          type: 'deleteChild', nodeId, parentId,
          containmentKey: cont.key,
          containmentLanguageKey: cont.languageKey,
          containmentLanguageVersion: cont.languageVersion,
          index,
        }),
      })
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
      await loadState()
    } catch (e) { actionError = e instanceof Error ? e.message : String(e) }
  }

  function beginEditProp(nodeId: string, propKey: string, currentValue: string | null) {
    editingProps = {
      ...editingProps,
      [nodeId]: { ...(editingProps[nodeId] ?? {}), [propKey]: currentValue ?? '' },
    }
  }

  function cancelEditProp(nodeId: string, propKey: string) {
    const next = { ...editingProps }
    if (next[nodeId]) {
      const np = { ...next[nodeId] }
      delete np[propKey]
      next[nodeId] = np
    }
    editingProps = next
  }

  async function doSetProperty(nodeId: string, prop: PropertyInfo, value: string) {
    actionError = null
    try {
      const resp = await fetch('./api/action', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          type: 'setProperty',
          nodeId,
          propertyKey: prop.key,
          propertyLanguageKey: prop.languageKey,
          propertyLanguageVersion: prop.languageVersion,
          value,
        }),
      })
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
      cancelEditProp(nodeId, prop.key)
      await loadState()
    } catch (e) { actionError = e instanceof Error ? e.message : String(e) }
  }

  $effect(() => {
    loadState()
    const interval = setInterval(loadState, 2000)
    return () => clearInterval(interval)
  })
</script>

{#snippet renderNode(node: NodeInfo, allNodes: Map<string, NodeInfo>, depth: number)}
  {@const concept = conceptFor(node)}
  <div class="node-item" style="--depth: {depth}">
    <div class="node-header">
      <button class="expand-btn" onclick={() => toggleExpanded(node.id)}>
        {expandedNodes.has(node.id) ? '▼' : '▶'}
      </button>
      <span class="node-concept">{concept?.name ?? node.classifierKey ?? '?'}</span>
      <code class="node-id">{node.id.slice(0, 8)}…</code>
    </div>

    {#if expandedNodes.has(node.id) && concept}
      <div class="node-details">

        <!-- Properties -->
        {#if concept.properties.length > 0}
          <div class="section-label">Properties</div>
          {#each concept.properties as prop}
            {@const stored = node.properties.find(p => p.key === prop.key)}
            {@const currentVal = stored?.value ?? null}
            {@const draftVal = editingProps[node.id]?.[prop.key]}
            {@const isEditing = draftVal !== undefined}
            <div class="prop-row">
              <span class="prop-name">{prop.name}</span>
              {#if isEditing}
                <input
                  class="prop-input"
                  type="text"
                  value={draftVal}
                  oninput={(e) => {
                    editingProps = {
                      ...editingProps,
                      [node.id]: { ...(editingProps[node.id] ?? {}), [prop.key]: (e.target as HTMLInputElement).value },
                    }
                  }}
                  onkeydown={(e) => {
                    if (e.key === 'Enter') doSetProperty(node.id, prop, editingProps[node.id][prop.key])
                    else if (e.key === 'Escape') cancelEditProp(node.id, prop.key)
                  }}
                />
                <button class="btn-xs btn-primary" onclick={() => doSetProperty(node.id, prop, editingProps[node.id][prop.key])}>Save</button>
                <button class="btn-xs btn-ghost" onclick={() => cancelEditProp(node.id, prop.key)}>Cancel</button>
              {:else}
                <button class="prop-value" onclick={() => beginEditProp(node.id, prop.key, currentVal)} title="Click to edit">
                  {#if currentVal !== null}{currentVal}{:else}<em class="unset">unset</em>{/if}
                </button>
                <button class="btn-xs btn-ghost" onclick={() => beginEditProp(node.id, prop.key, currentVal)}>Edit</button>
              {/if}
            </div>
          {/each}
        {/if}

        <!-- Containments -->
        {#each concept.containments as cont}
          {@const childIds = node.children[cont.key] ?? []}
          {@const addingSt = addChildState[node.id]}
          {@const isAddingHere = addingSt?.containmentKey === cont.key}
          <div class="cont-section">
            <div class="cont-header">
              <span class="cont-label">{cont.name}</span>
              <span class="cont-meta">{cont.multiple ? 'many' : 'one'}{cont.optional ? '' : ', required'}</span>
              {#if cont.multiple || childIds.length === 0}
                <button class="btn-xs btn-ghost" onclick={() => beginAddChild(node.id, cont.key)}>+ Child</button>
              {/if}
            </div>

            {#if isAddingHere}
              {@const candidates = candidateConcepts()}
              <div class="add-child-row">
                <select
                  class="select-sm"
                  value={addingSt.conceptKey}
                  onchange={(e) => {
                    addChildState = {
                      ...addChildState,
                      [node.id]: { ...addingSt, conceptKey: (e.target as HTMLSelectElement).value },
                    }
                  }}
                >
                  {#each candidates as c}
                    <option value="{c.key}|{c.languageKey}">{c.name} ({c.languageName})</option>
                  {/each}
                </select>
                <button class="btn-xs btn-primary" onclick={() => doAddChild(node.id, cont)}>Add</button>
                <button class="btn-xs btn-ghost" onclick={() => cancelAddChild(node.id)}>Cancel</button>
              </div>
            {/if}

            {#each childIds as childId, idx}
              {@const child = allNodes.get(childId)}
              <div class="child-entry">
                {#if child}
                  <div class="child-node">
                    {@render renderNode(child, allNodes, depth + 1)}
                  </div>
                  <button
                    class="btn-xs btn-delete"
                    title="Delete node"
                    onclick={() => doDeleteChild(childId, node.id, cont, idx)}
                  >✕</button>
                {:else}
                  <code class="missing-node">{childId}</code>
                {/if}
              </div>
            {/each}

            {#if childIds.length === 0 && !isAddingHere}
              <div class="empty-cont">empty</div>
            {/if}
          </div>
        {/each}

      </div>
    {/if}
  </div>
{/snippet}

<div class="layout">
  <header class="topbar">
    <div class="topbar-inner">
      <div class="brand">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" class="logo-icon">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <path d="M8 12h8M12 8v8" stroke="currentColor" stroke-width="2"/>
        </svg>
        <span>LionWeb JVM Demo Client</span>
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
    {#if error}<div class="alert-error">{error}</div>{/if}
    {#if actionError}<div class="alert-error">{actionError}</div>{/if}

    <div class="section-header">
      <h2>Partitions</h2>
      {#if state}<span class="count-badge">{state.partitions.length}</span>{/if}
    </div>

    <div class="create-row">
      <select bind:value={selectedConceptKey} class="concept-select">
        {#if state?.concepts}
          {#each state.concepts.slice().sort((a, b) => (b.isPartition ? 1 : 0) - (a.isPartition ? 1 : 0)) as c}
            <option value="{c.key}|{c.languageKey}">
              {c.isPartition ? '★ ' : ''}{c.name} ({c.languageName})
            </option>
          {/each}
        {/if}
      </select>
      <button onclick={createPartition} class="btn-primary" disabled={!selectedConceptKey}>
        Create Partition
      </button>
    </div>

    {#if state}
      {#if state.partitions.length === 0}
        <div class="empty-state"><p>No partitions yet. Create one above.</p></div>
      {:else}
        {@const allNodes = getNodesMap()}
        <div class="partition-list">
          {#each state.partitions as partition}
            {@const rootNode = allNodes.get(partition.id)}
            {@const rootConcept = rootNode ? conceptFor(rootNode) : undefined}
            <div class="partition-card">
              <div class="partition-row">
                <div class="partition-info">
                  <code class="partition-id">{partition.id}</code>
                  {#if partition.classifierKey}
                    <span class="partition-classifier">{rootConcept?.name ?? partition.classifierKey}</span>
                  {/if}
                </div>
                <div class="partition-actions">
                  {#if rootNode}
                    <button class="btn-icon" onclick={() => toggleExpanded(partition.id)}>
                      {expandedNodes.has(partition.id) ? '▲' : '▼'}
                    </button>
                  {/if}
                  <button class="btn-danger" onclick={() => deletePartition(partition.id)}>Delete</button>
                </div>
              </div>

              {#if expandedNodes.has(partition.id) && rootNode}
                <div class="node-body">
                  {@render renderNode(rootNode, allNodes, 0)}
                </div>
              {/if}
            </div>
          {/each}
        </div>
      {/if}

      <MessageLogPanel messages={state.messages} myClientId={state.clientId} />
    {/if}
  </main>
</div>

<style>
  .layout { min-height: 100vh; display: flex; flex-direction: column; }

  .topbar {
    background: var(--color-surface);
    border-bottom: 1px solid var(--color-border);
    box-shadow: var(--shadow);
    position: sticky; top: 0; z-index: 10;
  }

  .topbar-inner {
    max-width: 1100px; margin: 0 auto; padding: 0 1.5rem;
    height: 56px; display: flex; align-items: center;
    justify-content: space-between; gap: 1rem;
  }

  .brand { display: flex; align-items: center; gap: 10px; font-size: 1.1rem; font-weight: 600; color: var(--color-primary); }
  .logo-icon { color: var(--color-primary); flex-shrink: 0; }
  .conn-info { display: flex; align-items: center; gap: 6px; font-size: 0.8rem; color: var(--color-text-muted); }
  .conn-label { font-weight: 600; }
  .conn-sep { color: var(--color-border); }

  .content { max-width: 1100px; margin: 0 auto; padding: 2rem 1.5rem; flex: 1; width: 100%; }

  .alert-error {
    background: var(--color-error-bg); color: var(--color-error);
    border: 1px solid #fecaca; border-radius: var(--radius);
    padding: 12px 16px; margin-bottom: 1.5rem; font-size: 0.875rem;
  }

  .section-header { display: flex; align-items: center; gap: 10px; margin-bottom: 1.25rem; }
  .section-header h2 { font-size: 1.25rem; font-weight: 600; }
  .count-badge {
    background: var(--color-badge); color: var(--color-badge-text);
    font-size: 0.8rem; font-weight: 600; padding: 2px 8px; border-radius: 99px;
  }

  .create-row { display: flex; gap: 10px; margin-bottom: 1.25rem; }

  .concept-select {
    flex: 1; padding: 8px 12px; border: 1px solid var(--color-border);
    border-radius: var(--radius); font-size: 0.875rem;
    background: var(--color-surface); color: var(--color-text);
    outline: none; cursor: pointer;
  }

  .btn-primary {
    padding: 8px 16px; background: var(--color-primary); color: #fff;
    border: none; border-radius: var(--radius); font-size: 0.875rem;
    font-weight: 500; cursor: pointer; white-space: nowrap; transition: background 0.15s;
  }
  .btn-primary:hover { background: var(--color-primary-dark); }

  .btn-icon {
    padding: 4px 8px; background: transparent; border: 1px solid var(--color-border);
    border-radius: var(--radius); cursor: pointer; font-size: 0.8rem;
  }

  .btn-danger {
    padding: 5px 12px; background: #fee2e2; color: #dc2626;
    border: 1px solid #fecaca; border-radius: var(--radius);
    font-size: 0.8rem; font-weight: 500; cursor: pointer; white-space: nowrap;
  }
  .btn-danger:hover { background: #fecaca; }

  .btn-xs {
    padding: 3px 9px; font-size: 0.78rem; border-radius: var(--radius);
    cursor: pointer; white-space: nowrap; font-weight: 500; border: none;
  }
  .btn-xs.btn-primary { background: var(--color-primary); color: #fff; }
  .btn-xs.btn-primary:hover { background: var(--color-primary-dark); }
  .btn-xs.btn-ghost { background: var(--color-surface); color: var(--color-text); border: 1px solid var(--color-border); }
  .btn-xs.btn-ghost:hover { background: var(--color-badge); }
  .btn-xs.btn-delete { background: #fee2e2; color: #dc2626; border: 1px solid #fecaca; }
  .btn-xs.btn-delete:hover { background: #fecaca; }

  .empty-state {
    text-align: center; padding: 3rem; color: var(--color-text-muted);
    background: var(--color-surface); border: 1px dashed var(--color-border);
    border-radius: var(--radius);
  }

  .partition-list { display: flex; flex-direction: column; gap: 8px; margin-bottom: 0.5rem; }

  .partition-card {
    background: var(--color-surface); border: 1px solid var(--color-border);
    border-radius: var(--radius); overflow: hidden;
  }

  .partition-row {
    display: flex; align-items: center; justify-content: space-between;
    padding: 10px 14px; gap: 1rem;
  }

  .partition-info { display: flex; flex-direction: column; gap: 2px; flex: 1; min-width: 0; }
  .partition-id { font-size: 0.825rem; word-break: break-all; }
  .partition-classifier { font-size: 0.75rem; color: var(--color-primary); font-weight: 500; }
  .partition-actions { display: flex; gap: 6px; align-items: center; }

  .node-body { border-top: 1px solid var(--color-border); padding: 10px 14px; }

  /* Node tree */
  .node-item { margin-left: calc(var(--depth, 0) * 18px); }

  .node-header { display: flex; align-items: center; gap: 6px; padding: 3px 0; }

  .expand-btn {
    background: transparent; border: none; cursor: pointer;
    font-size: 0.65rem; color: var(--color-text-muted); padding: 2px 5px;
    line-height: 1;
  }

  .node-concept { font-size: 0.875rem; font-weight: 600; color: var(--color-primary); }
  .node-id { font-size: 0.72rem; color: var(--color-text-muted); }

  .node-details { margin-left: 20px; margin-top: 4px; padding-left: 8px; border-left: 2px solid var(--color-border); }

  .section-label {
    font-size: 0.72rem; font-weight: 600; color: var(--color-text-muted);
    text-transform: uppercase; letter-spacing: 0.05em; margin: 6px 0 3px;
  }

  .prop-row { display: flex; align-items: center; gap: 6px; padding: 3px 0; font-size: 0.85rem; }
  .prop-name { font-weight: 500; min-width: 90px; color: var(--color-text-muted); }
  .prop-value {
    flex: 1; cursor: pointer; padding: 2px 5px; border-radius: 3px;
    border: 1px solid transparent; background: transparent;
    text-align: left; font-size: 0.85rem; color: var(--color-text);
  }
  .prop-value:hover { background: var(--color-badge); border-color: var(--color-border); }
  .prop-input {
    flex: 1; padding: 3px 8px; border: 1px solid var(--color-primary);
    border-radius: var(--radius); font-size: 0.85rem;
    background: var(--color-surface); color: var(--color-text); outline: none;
  }
  .unset { color: var(--color-text-muted); font-style: italic; font-size: 0.78rem; }

  .cont-section { margin: 8px 0; }
  .cont-header { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
  .cont-label { font-size: 0.8rem; font-weight: 600; }
  .cont-meta {
    font-size: 0.7rem; color: var(--color-text-muted);
    background: var(--color-badge); padding: 1px 5px; border-radius: 99px;
  }

  .add-child-row { display: flex; gap: 6px; margin-bottom: 5px; align-items: center; }
  .select-sm {
    flex: 1; padding: 3px 8px; border: 1px solid var(--color-border);
    border-radius: var(--radius); font-size: 0.8rem;
    background: var(--color-surface); color: var(--color-text); cursor: pointer;
  }

  .child-entry {
    display: flex; align-items: flex-start; gap: 6px;
    padding: 3px 0 3px 8px; border-left: 2px solid var(--color-border); margin-left: 4px;
  }
  .child-node { flex: 1; min-width: 0; }
  .missing-node { font-size: 0.78rem; color: var(--color-text-muted); }
  .empty-cont { font-size: 0.75rem; color: var(--color-text-muted); font-style: italic; padding-left: 4px; }
</style>
