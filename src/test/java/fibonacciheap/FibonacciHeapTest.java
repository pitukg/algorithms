package fibonacciheap;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RunWith(JUnit4.class)
public class FibonacciHeapTest {

  static class TestElem extends FibElem<TestElem> {
    TestElem getParent() {
      return parent;
    }
    TestElem getLeftSibling() {
      return leftSibling;
    }
    TestElem getRightSibling() {
      return rightSibling;
    }
    TestElem getChild() {
      return child;
    }
    boolean getMarked() {
      return marked;
    }
    int getDeg() {
      return deg;
    }
    void setKey(int key) {
      this.key = key;
    }

    String handle;

    TestElem(int key, String handle) {
      this.key = key;
      this.handle = handle;
    }
  }

  private List<TestElem> siblingList(TestElem elem) {
    List<TestElem> siblingList = new ArrayList<>();
    siblingList.add(elem);
    for (TestElem e = elem.getRightSibling(); e != elem; e = e.getRightSibling()) {
      siblingList.add(e);
    }
    return siblingList;
  }

  private List<String> siblingListHandles(TestElem elem) {
    return siblingList(elem).stream().map(e -> e.handle).collect(Collectors.toList());
  }

  private List<TestElem> rootList(FibonacciHeap<TestElem> heap) {
    if (heap.isEmpty()) return Collections.emptyList();
    return siblingList(heap.peekMin());
  }

  private List<String> rootListHandles(FibonacciHeap<TestElem> heap) {
    if (heap.isEmpty()) return Collections.emptyList();
    return siblingListHandles(heap.peekMin());
  }

  private FibonacciHeap<TestElem> randomHeap() {
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    Random random = new Random(1423523522);
    double probabilityOfPopMin = 0.2;

    // Make the heap arbitrarily structured
    for (int i = 0; i < 100; i++) {
      heap.insert(new TestElem(random.nextInt(), ""));
      if (random.nextDouble() < probabilityOfPopMin) try {
        heap.popMin();
      } catch (FibonacciHeap.EmptyHeapException e) {
        // no pop
      }
    }
    return heap;
  }

  private FibonacciHeap<TestElem> randomHeapWithElement(TestElem element) {
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    Random random = new Random(1423523522);
    double probabilityOfPopMin = 0.2;
    element.setKey(random.nextInt());

    // Make the heap arbitrarily structured
    for (int i = 0; i < 50; i++) {
      heap.insert(new TestElem(random.nextInt(), ""));
      if (random.nextDouble() < probabilityOfPopMin) try {
        heap.popMin();
      } catch (FibonacciHeap.EmptyHeapException e) {
        // no pop
      }
    }
    heap.insert(element);
    for (int i = 0; i < 50; i++) {
      heap.insert(new TestElem(random.nextInt(), ""));
      if (random.nextDouble() < probabilityOfPopMin) try {
        heap.popMin();
      } catch (FibonacciHeap.EmptyHeapException e) {
        // no pop
      }
    }
    return heap;
  }

  @Test
  public void isEmpty_returnsEmpty_forEmptyHeap() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();

    // ACT
    boolean empty = heap.isEmpty();

    // ASSERT
    assertThat(empty).isTrue();
  }

  @Test
  public void insert_intoEmptyHeap() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    TestElem elem = new TestElem(20, "a");

    // ACT
    heap.insert(elem);

    // ASSERT
    assertThat(heap.isEmpty()).isFalse();
  }

  @Test
  public void insert_intoNonEmptyHeap() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    TestElem a = new TestElem(20, "a");
    TestElem b = new TestElem(30, "b");

    // ACT
    heap.insert(a);
    heap.insert(b);

    // ASSERT
    assertThat(a.getRightSibling()).isEqualTo(b);
    assertThat(a.getLeftSibling()).isEqualTo(b);
    assertThat(b.getLeftSibling()).isEqualTo(a);
    assertThat(b.getRightSibling()).isEqualTo(a);
  }

  @Test
  public void insert_pushesIntoRootList() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    TestElem a = new TestElem(20, "a");
    TestElem b = new TestElem(30, "b");
    TestElem c = new TestElem(10, "c");

    // ACT
    heap.insert(a);
    heap.insert(b);
    heap.insert(c);

    // ASSERT
    assertThat(rootListHandles(heap)).containsExactly("a", "b", "c");
  }

  @Test
  public void peekMin_throwsEmptyHeapException_ifEmpty() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();

    try {
      // ACT
      heap.peekMin();
      throw new AssertionError();
    } catch (FibonacciHeap.EmptyHeapException e) {
      // ASSERT
      // Exception thrown correctly
    }
  }

  @Test
  public void min_updatedCorrectly_onInsert() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();

    // ACT
    heap.insert(new TestElem(30, "a"));
    heap.insert(new TestElem(10, "b"));
    heap.insert(new TestElem(20, "c"));

    // ASSERT
    assertThat(heap.peekMin().handle).isEqualTo("b");
  }

  @Test
  public void decreaseKey_works_onRoot() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    TestElem a = new TestElem(30, "a");
    heap.insert(a);

    // ACT
    heap.decreaseKey(a, 10);

    // ASSERT
    assertThat(a.getKey()).isEqualTo(10);
    assertThat(a.getMarked()).isFalse();
  }

  @Test
  public void decreaseKey_doesNotAcceptForeignElem() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();

    try {
      // ACT
      heap.decreaseKey(new TestElem(30, "a"), 10);
    } catch (AssertionError e) {
      // ASSERT
      // Assertion failed correctly
    }
  }

  @Test
  public void decreaseKey_updatesMin_ifNewValueMinimal() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    TestElem a = new TestElem(10, "a");
    TestElem b = new TestElem(20, "b");
    heap.insert(a);
    heap.insert(b);

    // ACT
    heap.decreaseKey(b, 5);

    // ASSERT
    assertThat(heap.peekMin().handle).isEqualTo("b");
  }

  @Test
  public void decreaseKey_marksParent_ifParentUnmarked() {
    // ARRANGE
    TestElem elem;
    FibonacciHeap<TestElem> heap;
    do {
      elem = new TestElem(20, "elem");
      heap = randomHeapWithElement(elem);
    } while (elem.getParent() == null || elem.getParent().getMarked());
    TestElem parent = elem.getParent();

    // ACT
    heap.decreaseKey(elem, Integer.MIN_VALUE);

    // ASSERT
    assertThat(parent.getMarked()).isTrue();
  }

  @Test
  public void decreaseKey_evictsParent_ifParentMarked() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    TestElem f, g;
    heap.insert(new TestElem(1, "a"));
    heap.insert(new TestElem(2, "b"));
    heap.insert(new TestElem(-1, "min")); heap.popMin();
    heap.insert(new TestElem(3, "c"));
    heap.insert(new TestElem(4, "d"));
    heap.insert(new TestElem(-1, "min")); heap.popMin();
    heap.insert(new TestElem(5, "e"));
    heap.insert(f = new TestElem(6, "f"));
    heap.insert(new TestElem(-1, "min")); heap.popMin();
    heap.insert(g = new TestElem(7, "g"));
    heap.insert(new TestElem(8, "h"));
    heap.insert(new TestElem(-1, "min")); heap.popMin();
    /*
      Now the heap has the form:
               1
             / | \
            5  3  2
          / |  |
         7  6  4
         |
         8

       If we evict 6 and 7, 5 shall be evicted too.
     */

    // ACT
    heap.decreaseKey(f, 0);
    heap.decreaseKey(g, 0);

    // ASSERT
    assertThat(rootListHandles(heap)).contains("e");
  }

  @Test
  public void decreaseKey_evictChainTriggers() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    TestElem cc, ff, gg;
    heap.insert(new TestElem(1, "a"));
    heap.insert(new TestElem(2, "b"));
    heap.insert(new TestElem(-1, "min")); heap.popMin();
    heap.insert(new TestElem(3, "c"));
    heap.insert(new TestElem(4, "d"));
    heap.insert(new TestElem(-1, "min")); heap.popMin();
    heap.insert(new TestElem(5, "e"));
    heap.insert(new TestElem(6, "f"));
    heap.insert(new TestElem(-1, "min")); heap.popMin();
    heap.insert(new TestElem(7, "g"));
    heap.insert(new TestElem(8, "h"));
    heap.insert(new TestElem(-1, "min")); heap.popMin();
    heap.insert(new TestElem(10, "aa"));
    heap.insert(new TestElem(20, "bb"));
    heap.insert(new TestElem(-1, "min")); heap.popMin();
    heap.insert(cc = new TestElem(30, "cc"));
    heap.insert(new TestElem(40, "dd"));
    heap.insert(new TestElem(-1, "min")); heap.popMin();
    heap.insert(new TestElem(50, "ee"));
    heap.insert(ff = new TestElem(60, "ff"));
    heap.insert(new TestElem(-1, "min")); heap.popMin();
    heap.insert(gg = new TestElem(70, "gg"));
    heap.insert(new TestElem(80, "hh"));
    heap.insert(new TestElem(-1, "min"));
    heap.popMin();
    /*
      Now the heap has the form:
                       1
                /     / \ \
            1'       5  3  2
         / / \     / |  |
       5' 3'  2'  7  6  4
     / |  |       |
    7' 6' 4'      8
    |
    8'

       If we evict 3' 6' and 7' then 5' shall be evicted
       which triggers the eviction of 1' as well.
     */

    // ACT
    heap.decreaseKey(cc, 0);
    heap.decreaseKey(ff, 0);
    heap.decreaseKey(gg, 0);

    // ARRANGE
    assertThat(rootListHandles(heap)).containsExactly("a", "aa", "cc", "ee", "ff", "gg");
  }

  @Test
  public void decreaseKey_setsParentsChildToNull_whenEvicted() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    TestElem a = new TestElem(10, "a");
    TestElem b = new TestElem(20, "b");
    heap.insert(a);
    heap.insert(b);
    heap.insert(new TestElem(-1, "min"));
    heap.popMin();

    // ACT
    heap.decreaseKey(b, 0);

    // ASSERT
    assertThat(a.getChild()).isNull();
  }


  @Test
  public void popMin_throwsEmptyHeapException_whenEmpty() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();

    try {
      // ACT
      heap.popMin();
    } catch (FibonacciHeap.EmptyHeapException e) {
      // ASSERT
      // Exception thrown correctly
    }
  }

  @Test
  public void popMin_mergesCorrectly_whenTwoElementsInRootList() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    TestElem a = new TestElem(20, "a");
    TestElem b = new TestElem(10, "b");
    heap.insert(a);
    heap.insert(b);
    heap.insert(new TestElem(0, "t"));

    // ACT
    heap.popMin();

    // ASSERT
    assertThat(rootListHandles(heap)).containsExactly("b");
    assertThat(siblingListHandles(heap.peekMin().getChild())).containsExactly("a");
  }

  @Test
  public void degree_incremented_whenMerged() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    TestElem a = new TestElem(20, "a");
    TestElem b = new TestElem(10, "b");
    heap.insert(a);
    heap.insert(b);
    heap.insert(new TestElem(0, "t"));

    // ACT
    heap.popMin(); // triggers merge

    // ASSERT
    assertThat(heap.peekMin().getDeg()).isEqualTo(1);
  }

  @Test
  public void degree_decremented_whenLosesChild() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    TestElem a = new TestElem(10, "a");
    TestElem b = new TestElem(20, "b");
    heap.insert(a);
    heap.insert(b);
    heap.insert(new TestElem(-1, "min"));
    heap.popMin();
    int degBefore = a.getDeg();

    // ACT
    heap.decreaseKey(b, 0);
    int degAfter = a.getDeg();

    // ASSERT
    assertThat(degBefore).isEqualTo(1);
    assertThat(degAfter).isEqualTo(0);
  }

  @Test
  public void popMin_oneOfEachDegreeRemains() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = randomHeap();

    // ACT
    heap.popMin();

    // ASSERT
    assertThat(rootList(heap).stream().map(TestElem::getDeg).distinct().count())
        .isEqualTo(rootList(heap).size());
  }

  @Test
  public void popMin_heapRunsOutOfElements_whenPoppedRepeatedly() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = randomHeap();

    // ACT
    while (!heap.isEmpty()) {
      heap.popMin();
    }

    // ASSERT
    // terminates
  }

  @Test
  public void popMin_dumpsChildrenIntoRootList() {
    // ARRANGE
    FibonacciHeap<TestElem> heap = new FibonacciHeap<>();
    heap.insert(new TestElem(10, "a"));
    heap.insert(new TestElem(20, "b"));
    heap.insert(new TestElem(30, "c"));
    heap.insert(new TestElem(40, "d"));

    // ACT
    heap.popMin();

    // ASSERT
    assertThat(rootListHandles(heap)).containsExactly("b", "c");
    // Check that pointers are correct in the other direction
    List<String> list = new ArrayList<>();
    list.add(heap.peekMin().handle);
    for (TestElem root = heap.peekMin().getLeftSibling(); root != heap.peekMin(); root = root.getLeftSibling()) {
      list.add(root.handle);
    }
    assertThat(list).containsExactly("b", "c");
  }
}
