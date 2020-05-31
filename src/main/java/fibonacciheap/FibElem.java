package fibonacciheap;

/**
 * Element in Fibonacci heap.
 * Subclasses cannot access Fibonacci heap fields.
 * @param <Elem> Type of actual elements in the heap.
 */
public class FibElem<Elem> {
  FibonacciHeap heap;
  Elem parent;
  Elem leftSibling, rightSibling;
  Elem child;
  int key = Integer.MAX_VALUE;
  boolean marked;
  int deg;
  public int getKey() {
    return key;
  }
}
