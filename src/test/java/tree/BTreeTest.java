package tree;

import static com.google.common.truth.Truth.assertThat;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;
import java.util.stream.Collectors;


@RunWith(JUnit4.class)
public class BTreeTest {

  private static BTree<Integer, String>.BNode getRootNode() {
    return new BTree<Integer, String>().root;
  }

  private static void insertAll(BTree<Integer, String> tree, @NotNull Integer... is) {
    for (int i : is) tree.insert(i, Integer.toString(i));
  }

  private static void insertAll(BTree<Integer, String> tree, @NotNull Collection<Integer> keys) {
    for (int i : keys) tree.insert(i, Integer.toString(i));
  }

  @Test
  public void insert_insertsElem_toEmptyLeafNode() {
    // ARRANGE
    BTree<Integer, String>.BNode root = getRootNode();

    // ACT
    root.insert(3, "a");

    // ASSERT
    assertThat(root.keys.get(0)).isEqualTo(3);
    assertThat(root.values.get(0)).isEqualTo("a");
  }

  @Test
  public void insert_insertsElements_toLeafNode() {
    // ARRANGE
    BTree<Integer, String>.BNode root = getRootNode();

    // ACT
    root.insert(4, "a");
    root.insert(1, "b");
    root.insert(2, "c");

    // ASSERT
    assertThat(root.values).hasSize(3);
    assertThat(root.values).containsExactly("a", "b", "c");
    assertThat(root.keys).isInOrder();
  }

  @Test
  public void split_isCorrect_forRoot() {
    // ARRANGE
    BTree<Integer, String>.BNode root = getRootNode();
    root.insert(4, "a");
    root.insert(1, "b");
    root.insert(2, "c");

    // ACT
    BTree<Integer, String>.BNode newRoot = root.split();

    // ASSERT
    assertThat(newRoot).isNotEqualTo(root);
    assertThat(newRoot.keys).containsExactly(2);
    assertThat(newRoot.values).containsExactly("c");
    assertThat(newRoot.children.size()).isEqualTo(2);
    assertThat(newRoot.children.get(0).keys).containsExactly(1);
    assertThat(newRoot.children.get(1).keys).containsExactly(4);
    assertThat(newRoot.children.get(0).values).containsExactly("b");
    assertThat(newRoot.children.get(1).values).containsExactly("a");
    assertThat(newRoot.children.stream()
        .filter(n -> n.children.size() == 2).count()).isEqualTo(2);
    assertThat(newRoot.children.stream()
        .filter(n -> n.children.stream().filter(Objects::isNull).count() == 2)
        .count()).isEqualTo(2);
  }

  @Test
  public void split_isCorrect_forNonRootNode() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 1, 2, 3, 4, 5);

    // ACT
    tree.insert(6, "6");

    // ASSERT
    assertThat(tree.root.values).containsExactly("2", "4");
    assertThat(tree.root.children.size()).isEqualTo(3);
    assertThat(tree.root.children.get(0).values).containsExactly("1");
    assertThat(tree.root.children.get(1).values).containsExactly("3");
    assertThat(tree.root.children.get(2).values).containsExactly("5", "6");
  }

  @Test
  public void insert_triggersSplit_whenNodeFull() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    tree.insert(4, "a");
    tree.insert(1, "b");
    tree.insert(2, "c");
    BTree.BNode oldRoot = tree.root;

    // ACT
    tree.insert(3, "d");

    // ASSERT
    assertThat(tree.root).isNotEqualTo(oldRoot);
    assertThat(tree.root.children.get(0).values).containsExactly("b");
    assertThat(tree.root.children.get(1).values).containsExactly("a", "d");
  }

  @Test
  public void insert_triggersNonPreemptiveSplit_whenNodeFull() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30, 40, 50, 60, 70, 80);

    // ACT
    tree.insert(15, "15");

    // ASSERT
    assertThat(tree.root.values).containsExactly("40");
  }

  @Test
  public void findPos_findsPosition_inTheMiddleWhenUnique() {
    // ARRANGE
    BTree<Integer, String>.BNode root = getRootNode();
    root.insert(4, "a");
    root.insert(1, "b");
    root.insert(2, "c");

    // ACT
    int pos = root.findPos(3);

    // ASSERT
    assertThat(pos).isEqualTo(2);
  }

  @Test
  public void find_returnsValue_ifExists() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30);

    // ACT
    String value = tree.find(20);

    // ASSERT
    assertThat(value).isEqualTo("20");
  }

  @Test
  public void find_returnsNull_ifDoesNotExist() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 40);

    // ACT
    String value = tree.find(30);

    // ASSERT
    assertThat(value).isNull();
  }

  @Test
  public void find_returnsValue_ifMultiLevel() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30, 40);

    // ACT
    String value = tree.find(30);

    // ASSERT
    assertThat(value).isEqualTo("30");
  }

  @Test
  public void minimum_returnsValue_ifNotEmpty() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 20, 40, 30, 10, 50);

    // ACT
    String min = tree.minimum();

    // ASSERT
    assertThat(min).isEqualTo("10");
  }

  @Test
  public void minimum_throws_ifEmpty() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();

    try {
      // ACT
      String min = tree.minimum();
      throw new RuntimeException("No exception thrown by minimum for empty tree.");
    } catch (UnsupportedOperationException e) {
      // ASSERT
    }
  }

  @Test
  public void maximum_returnsValue_ifNotEmpty() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 20, 40, 30, 10, 50);

    // ACT
    String max = tree.maximum();

    // ASSERT
    assertThat(max).isEqualTo("50");
  }

  @Test
  public void maximum_throws_ifEmpty() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();

    try {
      // ACT
      String max = tree.maximum();
      throw new RuntimeException("No exception thrown by maximum for empty tree.");
    } catch (UnsupportedOperationException e) {
      // ASSERT
    }
  }

  @Test
  public void mergeChildren_mergesLeafNodes() {
    // ARRANGE
    class TestTree extends BTree<Integer, String> {
      @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
      private TestTree() {
        BNode l = new BLeafNode();
        l.keys = new ArrayList<>(Arrays.asList(1));
        l.values = new ArrayList<>(Arrays.asList("1"));
        l.size = 1;
        BNode m = new BLeafNode();
        m.keys = new ArrayList<>(Arrays.asList(8));
        m.values = new ArrayList<>(Arrays.asList("8"));
        m.size = 1;
        BNode r = new BLeafNode();
        r.keys = new ArrayList<>(Arrays.asList(15));
        r.values = new ArrayList<>(Arrays.asList("15"));
        r.size = 1;
        root = new BLeafNode();
        root.keys = new ArrayList<>(Arrays.asList(5, 10));
        root.values = new ArrayList<>(Arrays.asList("5", "10"));
        root.children = new ArrayList<>(Arrays.asList(l, m, r));
        root.size = 2;
      }
    }
    BTree<Integer, String>.BNode root = new TestTree().root;

    // ACT
    root.mergeChildren(1);

    // ASSERT
    assertThat(root.values).containsExactly("5");
    assertThat(root.children.get(0).values).containsExactly("1");
    assertThat(root.children.get(1).values).containsExactly("8", "10", "15");
  }

  @Test
  public void mergeChildren_assertionError_ifNotMinimal() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30, 40);

    // ACT
    try {
      tree.root.mergeChildren(0);
      throw new RuntimeException("No exception thrown by mergeChildren.");
    } catch (AssertionError e) {
      // ASSERT
    }
  }

  @Test
  public void redistributeChildren_redistributesLeafNodesCorrectly() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30, 40);

    // ACT
    tree.root.redistributeChildren(0, false);

    // ASSERT
    assertThat(tree.root.values).containsExactly("30");
    assertThat(tree.root.children.get(0).values).containsExactly("10", "20");
    assertThat(tree.root.children.get(1).values).containsExactly("40");
  }

  @Test
  public void redistributeChildren_guaranteesNotMinimal_forSpecifiedNode() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30, 40);

    // ACT
    tree.root.redistributeChildren(0, true);

    // ASSERT
    assertThat(tree.root.values).containsExactly("20");
    assertThat(tree.root.children.get(0).values).containsExactly("10");
    assertThat(tree.root.children.get(1).values).containsExactly("30", "40");
  }

  @Test
  public void delete_deletesCorrectly_fromNotMinimalLeafNode() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30);

    // ACT
    tree.delete(20);

    // ASSERT
    assertThat(tree.root.values).containsExactly("10", "30");
  }

  @Test
  public void delete_swapsWithSuccessorCorrectly_forNotMinimalNodes() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30, 40, 50, 60);

    // ACT
    tree.delete(40);

    // ASSERT
    assertThat(tree.root.values).containsExactly("20", "50");
    assertThat(tree.root.children.get(0).values).containsExactly("10");
    assertThat(tree.root.children.get(1).values).containsExactly("30");
    assertThat(tree.root.children.get(2).values).containsExactly("60");
  }

  @Test
  public void delete_triggersExpand_forLeftmostKeyInMinimalLeafNode() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30, 40, 50, 60);
    tree.delete(40);

    // ACT
    tree.delete(10);

    // ASSERT
    assertThat(tree.root.values).containsExactly("50");
    assertThat(tree.root.children.get(0).values).containsExactly("20", "30");
    assertThat(tree.root.children.get(1).values).containsExactly("60");
  }

  @Test
  public void delete_triggersExpand_forRightmostKeyInMinimalLeafNode() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30, 40, 50, 60);
    tree.delete(40);

    // ACT
    tree.delete(60);

    // ASSERT
    assertThat(tree.root.values).containsExactly("20");
    assertThat(tree.root.children.get(0).values).containsExactly("10");
    assertThat(tree.root.children.get(1).values).containsExactly("30", "50");
  }

  @Test
  public void delete_triggersRedistribute_when12leafNodes() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30, 40, 50, 60);

    // ACT
    tree.delete(30);

    // ASSERT
    assertThat(tree.root.values).containsExactly("20", "50");
    assertThat(tree.root.children.get(0).values).containsExactly("10");
    assertThat(tree.root.children.get(1).values).containsExactly("40");
    assertThat(tree.root.children.get(2).values).containsExactly("60");
  }

  @Test
  public void delete_triggersExpandRecursively_forChainOfMinimalNodes() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    insertAll(tree, 10, 20, 30, 40, 50, 60, 70, 80, 90);
    /*
                   40
                /      \
            20            60
          /    \        /    \
        10      30    50    70-80-90
     */

    // ACT
    tree.delete(50); // this only causes redistribute

    // ASSERT
    assertThat(tree.root.values).containsExactly("20", "40", "70");
    assertThat(tree.root.children.get(0).values).containsExactly("10");
    assertThat(tree.root.children.get(1).values).containsExactly("30");
    assertThat(tree.root.children.get(2).values).containsExactly("60");
    assertThat(tree.root.children.get(3).values).containsExactly("80", "90");
  }

  @Test
  public void insertAllDeleteAllTest_forTEquals2() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>();
    List<Integer> keys = new Random().ints(100).boxed().collect(Collectors.toList());
    insertAll(tree, keys);
    List<String> full = new ArrayList<>(tree.root.values);
    Collections.shuffle(keys);

    // ACT
    for (int key : keys) {
      tree.delete(key);
    }

    // ASSERT
    assertThat(full).isNotEmpty();
    assertThat(tree.root.size).isEqualTo(0);
    assertThat(tree.root.children.get(0)).isNull();
  }

  @Test
  public void insertAllDeleteAllTest_forLargerT() {
    // ARRANGE
    BTree<Integer, String> tree = new BTree<>(13);
    List<Integer> keys = new Random().ints(2000).boxed().collect(Collectors.toList());
    insertAll(tree, keys);
    List<String> full = new ArrayList<>(tree.root.values);
    Collections.shuffle(keys);

    // ACT
    for (int key : keys) {
      tree.delete(key);
    }

    // ASSERT
    assertThat(full).isNotEmpty();
    assertThat(tree.root.size).isEqualTo(0);
    assertThat(tree.root.children.get(0)).isNull();
  }

  @Test
  public void insertAllDeleteAllTest_forStringKeys() {
    // ARRANGE
    BTree<String, String> tree = new BTree<>(3);
    List<String> keys = new Random().ints(500).mapToObj(Integer::toString).collect(Collectors.toList());
    for (String key : keys) tree.insert(key, key);
    List<String> full = new ArrayList<>(tree.root.values);
    Collections.shuffle(keys);

    // ACT
    for (String key : keys) {
      tree.delete(key);
    }

    // ASSERT
    assertThat(full).isNotEmpty();
    assertThat(tree.root.size).isEqualTo(0);
    assertThat(tree.root.children.get(0)).isNull();
  }

}
