package io.lionweb.repoclient;

class NodeInfo {
    private final String id;
    private final String parent;
    private final int depth;

    public NodeInfo(String id, String parent, int depth) {
        this.id = id;
        this.parent = parent;
        this.depth = depth;
    }

    public String getId() {
        return id;
    }

    public String getParent() {
        return parent;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return "NodeInfo{id='" + id + "', parent='" + parent + "', depth=" + depth + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeInfo)) return false;
        NodeInfo that = (NodeInfo) o;
        return depth == that.depth &&
                id.equals(that.id) &&
                (parent != null ? parent.equals(that.parent) : that.parent == null);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + depth;
        return result;
    }
}