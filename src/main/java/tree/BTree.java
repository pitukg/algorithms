package tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BTree<K extends Comparable<K>, V> {

  // TODO: enforce uniqueness of keys
  // TODO: use .equals() for K type everywhere

  class BNode {

    BNode parent; // null for root
    List<K> keys;
    List<V> values;
    List<BNode> children;
    int size; // number of entries in node (number of children is size+1)
    // Invariant is maintained for each node except the root: t-1 <= size <= 2t-1

    /**
     * Private``copy'' constructor, assumes consistent parameters, for internal use only.
     * Makes copy of the parameters so subLists can be provided as arguments.
     */
    private BNode(List<K> keys, List<V> values, List<BNode> children, int size, BNode parent) {
      this.keys = new ArrayList<>(keys);
      this.values = new ArrayList<>(values);
      this.children = new ArrayList<>(children);
      this.size = size;
      this.parent = parent;
    }

    /**
     * Factory method for node creation.
     * It is overridden for leaf nodes.
     *
     * @return New empty node.
     */
    BNode makeNode(List<K> keys, List<V> values, List<BNode> children, int size, BNode parent) {
      return new BNode(keys, values, children, size, parent);
    }

    // EXPOSED METHODS

    /**
     * Insert a new entry, with preemptively splitting if node is full.
     * This is the default implementation, for leaf nodes it is overridden.
     *
     * @param key   Key of entry to insert.
     * @param value Value of entry to insert.
     */
    void insert(K key, V value) {
      if (size == 2 * t - 1) {
        split().insert(key, value); // Make sure not to use this anymore.
      } else {
        int pos = findPos(key);
        children.get(pos).insert(key, value);
      }
    }

    /**
     * Deletes entry from subtree.
     * Swaps non-leaf node entry with its successor in a leaf node, then deletes the leaf node entry.
     * TODO: randomize successor/predecessor for balance
     * This is the default implementation, for leaf nodes it is overridden.
     *
     * @param key Key of entry to delete.
     */
    void delete(K key) {
      int pos = findPos(key);
      if (pos != size && keys.get(pos).equals(key)) {
        // Swap with successor
        BNode successorNode = children.get(pos + 1).minimumNode();
        int successorPos = successorNode.findPos(key);
        this.keys.set(pos, successorNode.keys.get(successorPos));
        this.values.set(pos, successorNode.values.get(successorPos));

        successorNode.delete(keys.get(pos));
      } else {
        children.get(pos).delete(key);
      }
    }

    /**
     * Find and retrieve value associated with key.
     * Default implementation, overridden for leaf nodes.
     *
     * @param key Lookup key.
     * @return Value associated with key, or null if not found.
     */
    V find(K key) {
      int pos = findPos(key);
      return children.get(pos).find(key);
    }

    /**
     * @return Minimum node in subtree.
     */
    BNode minimumNode() {
      return children.get(0).minimumNode();
    }

    /**
     * @return Maximum node in subtree.
     */
    BNode maximumNode() {
      return children.get(size).maximumNode();
    }


    // UTILITY FUNCTIONS

    /**
     * @param key Key to check position of.
     * @return Position of the smallest key bigger than argument.
     * TODO: check what happens with duplicate keys.
     */
    int findPos(K key) {
      int left = 0, right = size;
      while (left < right) {
        int mid = (right + left) / 2;
        if (keys.get(mid).compareTo(key) < 0) {
          left = mid + 1;
        } else {
          right = mid;
        }
      }
      return left;
    }

    /**
     * Split the node if it is full.
     * Usage: node = node.split(), old node should be thrown away!
     *
     * @return Parent node which got the extra element from this.
     */
    BNode split() {
      assert (size == 2 * t - 1);
      // Create new node for first half of values
      BNode left = makeNode(
          keys.subList(0, t - 1),
          values.subList(0, t - 1),
          children.subList(0, t),
          t - 1,
          parent);
      // Change parent pointers of children of left
      for (BNode child : left.children) {
        if (child != null) child.parent = left;
      }
      // Create new node for second half of values
      BNode right = makeNode(
          keys.subList(t, 2 * t - 1),
          values.subList(t, 2 * t - 1),
          children.subList(t, 2 * t),
          t - 1,
          parent);
      // Change parent pointers of children of right
      for (BNode child : right.children) {
        if (child != null) child.parent = right;
      }

      if (parent != null) {
        parent.pullFromChild(keys.get(t - 1), values.get(t - 1), left, right);
        return parent;
      } else {
        assert (root == this);
        // We are splitting the root node, create new root node.
        root = new BNode( // Should be ordinary node, not leaf, use explicit BNode constructor!
            List.of(keys.get(t - 1)),
            List.of(values.get(t - 1)),
            List.of(left, right),
            1,
            null);
        left.parent = root;
        right.parent = root;
        // TODO: for development null this instance
        this.size = -1;
        this.keys = null;
        this.values = null;
        this.children = null;
        return root;
      }
    }

    /**
     * Accepts an entry from child when child is splitting. Only call when this is not full.
     *
     * @param key   Key of entry.
     * @param value Value of entry.
     * @param left  Node of left child of entry.
     * @param right Node of right child of entry.
     */
    void pullFromChild(K key, V value, BNode left, BNode right) {
      assert (size < 2 * t - 1);
      int pos = findPos(key);
      keys.add(pos, key);
      values.add(pos, value);
      children.set(pos, left);
      assert (left.parent == this);
      children.add(pos + 1, right);
      assert (right.parent == this);
      size++;
    }

    /**
     * Expands so that this node is no longer minimal.
     * Conditions: this node is not minimal and not root node, root node has no constraint on size.
     *
     * @return Entry point to the unmodified subtree to continue recursing into.
     */
    BNode expand() {
      assert (size == t - 1 && parent != null);
      int childPos = -1;
      for (int i = 0; i <= parent.size; i++) {
        if (parent.children.get(i) == this) {
          childPos = i;
          break;
        }
      }
      assert (childPos != -1);
      if (childPos == 0) {
        // Guaranteed right neighbour because only child -> root node.
        if (parent.children.get(1).size > t - 1) {
          return parent.redistributeChildren(0, false);
        } else {
          return parent.mergeChildren(0);
        }
      } else if (childPos == parent.size) {
        // Guaranteed left neighbour because only child -> root node.
        if (parent.children.get(childPos - 1).size > t - 1) {
          return parent.redistributeChildren(childPos - 1, true);
        } else {
          return parent.mergeChildren(childPos - 1);
        }
      } else {
        BNode leftNeighbour = parent.children.get(childPos - 1);
        BNode rightNeighbour = parent.children.get(childPos + 1);
        // Redistribute with larger neighbour if possible, otherwise prefer redistribute over merge.
        // If both neighbours minimal randomize merge choice for balance.
        if (leftNeighbour.size > t - 1 && (rightNeighbour.size == t - 1 || new Random().nextBoolean())) {
          return parent.redistributeChildren(childPos - 1, true);
        } else if (rightNeighbour.size > t - 1) {
          return parent.redistributeChildren(childPos, false);
        } else {
          return parent.mergeChildren(new Random().nextBoolean() ? childPos : childPos - 1);
        }
      }
    }

    /**
     * Merge children of this node, provided they have minimum size.
     * May trigger recursive expand of this node if this is minimal.
     *
     * @param leftPos Position of left child, it is merged with its right neighbour.
     * @return Entry point to the unmodified subtree to continue recursing into.
     */
    BNode mergeChildren(int leftPos) {
      if (this.size == t - 1 && parent != null) {
        return expand();
      }

      BNode left = this.children.get(leftPos);
      BNode right = this.children.get(leftPos + 1);
      assert (left.size == t - 1 && right.size == t - 1);
      left.keys.add(this.keys.get(leftPos));
      this.keys.remove(leftPos);
      left.keys.addAll(right.keys);
      left.values.add(this.values.get(leftPos));
      this.values.remove(leftPos);
      left.values.addAll(right.values);
      this.children.remove(leftPos + 1);
      left.children.addAll(right.children);
      // Update right's children pointers to left.
      for (BNode child : right.children) {
        if (child != null) {
          child.parent = left;
        }
      }
      this.size--;
      left.size = 2 * t - 1;
      // If root has run out of entries change it to its only child.
      if (parent == null && size == 0) {
        root = left;
        root.parent = null;
      }
      // TODO: for development null right
      right.size = -1;
      right.keys = null;
      right.values = null;
      right.children = null;
      return this;
    }

    /**
     * Redistribute neighbouring nodes to equal parts, provided they are not both minimal.
     * May trigger recursive expand of this node if this is minimal.
     *
     * @param leftPos     Position of left child, it is redistributed with its right neighbour.
     * @param moreToRight If true right is guaranteed to be non-minimal, if false left is.
     * @return Entry point to the unmodified subtree to continue recursing into.
     */
    BNode redistributeChildren(int leftPos, boolean moreToRight) {
      if (this.size == t - 1 && parent != null) {
        return expand();
      }

      BNode left = this.children.get(leftPos);
      BNode right = this.children.get(leftPos + 1);
      assert (left.size > t - 1 || right.size > t - 1);
      // Dump all keys, values, children into a temporary list.
      List<K> allKeys = new ArrayList<>(left.keys);
      allKeys.add(this.keys.get(leftPos));
      allKeys.addAll(right.keys);
      List<V> allValues = new ArrayList<>(left.values);
      allValues.add(this.values.get(leftPos));
      allValues.addAll(right.values);
      List<BNode> allChildren = new ArrayList<>(left.children);
      allChildren.addAll(right.children);

      int newLeftSize = moreToRight ? (left.size + right.size) / 2 : (left.size + right.size + 1) / 2;
      int newRightSize = left.size + right.size - newLeftSize;
      left.size = newLeftSize;
      right.size = newRightSize;

      left.keys = new ArrayList<>(allKeys.subList(0, left.size));
      this.keys.set(leftPos, allKeys.get(left.size));
      right.keys = new ArrayList<>(allKeys.subList(left.size + 1, left.size + right.size + 1));

      left.values = new ArrayList<>(allValues.subList(0, left.size));
      this.values.set(leftPos, allValues.get(left.size));
      right.values = new ArrayList<>(allValues.subList(left.size + 1, left.size + right.size + 1));

      left.children = new ArrayList<>(allChildren.subList(0, left.size + 1));
      for (BNode child : left.children) {
        if (child != null) {
          child.parent = left;
        }
      }
      right.children = new ArrayList<>(allChildren.subList(left.size + 1, left.size + right.size + 2));
      for (BNode child : right.children) {
        if (child != null) {
          child.parent = right;
        }
      }

      return this;
    }

    @Override
    public String toString() {
      StringBuilder s = new StringBuilder();
      if (this.parent == null) s.append("r");
      s.append("[");
      Iterator<K> kIterator = new ArrayList<K>(keys).iterator();
      Iterator<V> vIterator = new ArrayList<V>(values).iterator();
      while (kIterator.hasNext() && vIterator.hasNext()) {
        s.append(" (k:").append(kIterator.next()).append(", v:").append(vIterator.next()).append(")");
        if (kIterator.hasNext()) {
          s.append(",");
        }
      }
      s.append(" ]");
      return s.toString();
    }

  }

  class BLeafNode extends BNode {

    BLeafNode() {
      super(
          new ArrayList<>(2 * t),
          new ArrayList<>(2 * t),
          new ArrayList<>(2 * t + 1),
          0,
          null
      );
      children.add(null); // Start with one child, this is to keep consistent with invariants.
    }

    private BLeafNode(List<K> keys, List<V> values, List<BNode> children, int size, BNode parent) {
      super(keys, values, children, size, parent);
    }

    @Override
    BNode makeNode(List<K> keys, List<V> values, List<BNode> children, int size, BNode parent) {
      return new BLeafNode(keys, values, children, size, parent);
    }

    @Override
    void insert(K key, V value) {
      if (size < 2 * t - 1) {
        // Find position with binary search __before__ incrementing size.
        int after = findPos(key);
        // Insert new key+value+child into pos.
        keys.add(after, key);
        values.add(after, value);
        children.add(null); // This is to make size of children consistent with invariants.
        // Increment size.
        size++;
      } else {
        assert (size == 2 * t - 1);
        // Split node and try again.
        split().insert(key, value);
      }
    }

    // TODO: avoid unnecessary comparison when it is a successor with an existing key
    private void delete(K key, boolean guaranteedToExist) {
    }

    @Override
    void delete(K key) {
      int pos = findPos(key);
      if (pos >= size || !keys.get(pos).equals(key)) {
        throw new UnsupportedOperationException("Key to delete not found.");
      }

      if (size > t - 1 || parent == null) {
        // Delete entry.
        keys.remove(pos);
        values.remove(pos);
        children.remove(size); // This is to make size of children consistent with invariants.
        size--;
      } else {
        // Node is minimal, expand it so that delete is possible.
        // TODO: This is O(log^2(n)), improve to O(log(n)). Idea: create expandPathToKey method.
        // Recurse into the lowest entry point with unchanged subtree.
        expand().delete(key);
      }
    }

    @Override
    V find(K key) {
      int pos = findPos(key);
      if (keys.get(pos) == key) {
        return values.get(pos);
      } else {
        return null;
      }
    }

    @Override
    BNode minimumNode() {
      if (size == 0) {
        throw new UnsupportedOperationException("No minimum of empty tree.");
      }
      return this;
    }

    @Override
    BNode maximumNode() {
      if (size == 0) {
        throw new UnsupportedOperationException("No maximum of empty tree.");
      }
      return this;
    }

    @Override
    public String toString() {
      return "l" + super.toString();
    }

  }

  private final int t;

  BNode root = this.new BLeafNode();

  public BTree() {
    this.t = 2;
  }

  public BTree(int t) {
    assert (t > 1);
    this.t = t;
  }

  public void insert(K key, V value) {
    root.insert(key, value);
  }

  public void delete(K key) {
    root.delete(key);
  }

  public V find(K key) {
    return root.find(key);
  }

  public V minimum() {
    return root.minimumNode().values.get(0);
  }

  public V maximum() {
    BNode maxNode = root.maximumNode();
    return maxNode.values.get(maxNode.size - 1);
  }
}
