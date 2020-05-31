package fibonacciheap;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Fibonacci heap data structure which provides
 * min priority queue functionality.
 * @param <Elem> Type of elements in the heap, needs to extend FibElem<Elem>.
 */
public class FibonacciHeap<Elem extends FibElem<Elem>> {

  /**
   * Exception thrown when illegal operation is requested with an empty heap.
   */
  public static class EmptyHeapException extends UnsupportedOperationException {}

  private Elem min;

  /**
   * Constructs an empty heap.
   */
  public FibonacciHeap() {
    min = null;
  }

  /**
   * @return True if heap is empty,
   *         False if heap is not empty.
   */
  public boolean isEmpty() {
    return min == null;
  }

  /**
   * Inserts an element into the heap.
   * @param elem Element to insert.
   */
  public void insert(Elem elem) {
    elem.heap = this;
    if (min == null) {
      min = elem;
      min.parent = null;
      min.leftSibling = min.rightSibling = min;
      min.child = null;
      min.marked = false;
      min.deg = 0;
    } else {
      elem.child = null;
      elem.deg = 0;
      pushToRootList(elem);
    }
  }

  /**
   * @return Minimum element in heap.
   * @throws EmptyHeapException if heap is empty.
   */
  public Elem peekMin() {
    if (min == null) {
      throw new EmptyHeapException();
    }
    return min;
  }

  /**
   * Decreases key of element.
   * @param element Element whose key is decreased. Element has to belong to the heap,
   *                otherwise an assertion failure is thrown.
   * @param newKey  New key of element.
   * @implNote Evicts it into root list and enforces grandchild rule recursively on parents.
   *    * Has true cost of O(max depth of heap) but it has amortised cost O(1).
   */
  public void decreaseKey(Elem element, int newKey) {
    assert(element.heap == this);
    element.key = newKey;
    if (element.parent == null) {
      if (element.key < min.key) {
        min = element;
      }
    } else if (element.key < element.parent.key) {
      triggerLoser(element.parent, element);
      connectSiblings(element);
      pushToRootList(element);
    }
  }

  /**
   * Pops the minimum element from the heap.
   * @throws EmptyHeapException if the heap is empty.
   * @implNote A cleanup procedure follows that merges heaps until there are at most
   * one heap of each degree.
   * True cost is O(size of root list + no of min's children), but this operation
   * has amortised cost O(max degree in heap) = O(log(size of heap)).
   */
  public void popMin() {
    if (min == null) {
      throw new EmptyHeapException();
    }
    min.heap = null;
    if (min.leftSibling == min && min.child == null) {
      min = null;
      return;
    }

    if (min.child != null) {
      min.child.leftSibling.rightSibling = min.rightSibling;
      min.rightSibling.leftSibling = min.child.leftSibling;
      min.child.leftSibling = min.leftSibling;
      min.leftSibling.rightSibling = min.child;
      min = min.child;
      // old min will be garbage collected (unreachable)
      // min is just an entry point now
    } else {
      connectSiblings(min);
      min = min.leftSibling;
    }

    min.parent = null;
    int maxDeg = min.deg;
    for (Elem root = min.rightSibling; root != min; root = root.rightSibling) {
      root.parent = null;
      if (root.deg > maxDeg) {
        maxDeg = root.deg;
      }
    }

    // DO CLEANUP
    // dump root list into a list
    List<Elem> rootList = new ArrayList<>();
    rootList.add(min);
    for (Elem root = min.rightSibling; root != min; root = root.rightSibling) {
      rootList.add(root);
    }

    final int maxSize = maxDeg + rootList.size() + 1;
    Object[] degArray = new Object[maxSize]; // set to null by default
    // merge heaps of same degree NOT caring about sibling pointers in root list
    for (Elem root : rootList) {
      Elem curr = root;
      while (degArray[curr.deg] != null) {
        curr = merge((Elem)degArray[curr.deg], curr);
        // clear previous slot
        degArray[curr.deg-1] = null;
      }
      degArray[curr.deg] = curr;
    }

    // build new root list with min ptr
    rootList.clear();
    for (int deg = 0; deg < maxSize; deg++) {
      if (degArray[deg] != null) {
        rootList.add((Elem)degArray[deg]);
        if (((Elem)degArray[deg]).key <= min.key) {
          // We can't spare the work in equality case because min has to
          // point to an entry point in the root list and old min might no
          // longer be in root list
          min = (Elem)degArray[deg];
        }
      }
    }

    rootList.get(0).leftSibling = rootList.get(rootList.size()-1);
    rootList.get(rootList.size()-1).rightSibling = rootList.get(0);
    for (int i = 1; i < rootList.size(); i++) {
      rootList.get(i).leftSibling = rootList.get(i-1);
      rootList.get(i-1).rightSibling = rootList.get(i);
    }
  }

  /**
   * Pushes elem to root list.
   * Sets parent pointer to null but does not modify parent.
   * @param elem Element to be pushed to root list.
   */
  private void pushToRootList(Elem elem) {
    assert(min != null);

    elem.marked = false;
    elem.parent = null;
    elem.leftSibling = min;
    elem.rightSibling = min.rightSibling;
    min.rightSibling = elem;
    elem.rightSibling.leftSibling = elem;

    if (elem.key < min.key) {
      min = elem;
    }
  }

  /**
   * Evicts elem from sibling chain by connecting left and right siblings' pointers.
   * @param elem Element being evicted.
   */
  private void connectSiblings(Elem elem) {
    elem.leftSibling.rightSibling = elem.rightSibling;
    elem.rightSibling.leftSibling = elem.leftSibling;
  }

  /**
   * Triggers the action of elem losing child.
   * Handles changes in the tree above child's level that happen after child is evicted,
   * including changing elem's child pointer if necessary.
   * Possibly a chain of eviction is triggered because of the grandchildren rule.
   * @param elem  The element who loses child.
   * @param child The child elem loses.
   */
  private void triggerLoser(Elem elem, Elem child) {
    elem.deg--;
    if (elem.child == child) {
      // Change child entry point of elem
      if (child.rightSibling == child) {
        // Only child lost
        elem.child = null;
      } else {
        // Modify entry point to children
        elem.child = child.rightSibling;
      }
    }
    if (!elem.marked) {
      if (elem.parent != null) {
        elem.marked = true;
      }
    } else {
      triggerLoser(elem.parent, elem);
      connectSiblings(elem);
      pushToRootList(elem);
    }
  }

  /**
   * Merges two heaps together so that the heap property is preserved.
   * @param x First heap to merge.
   * @param y Second heap to merge.
   * @return The merged heap -- is equal to either x or y with fields changed.
   */
  private Elem merge(Elem x, Elem y) {
    assert (x.deg == y.deg);
    Elem a, b;
    if (x.key < y.key) {
      a = x;
      b = y;
    } else {
      a = y;
      b = x;
    }

    // make b child of a
    b.parent = a;
    a.deg++;

    if (a.child == null) {
      a.child = b;
      b.leftSibling = b.rightSibling = b;
    } else {
      // insert b to the right of child ptr
      b.rightSibling = a.child.rightSibling;
      a.child.rightSibling.leftSibling = b;
      a.child.rightSibling = b;
      b.leftSibling = a.child;
    }

    return a;
  }

}