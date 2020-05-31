# algorithms
Demonstration of some algorithms and data structures.
This also aims to demonstrate Java and Test Driven Development skills.

## B-Tree
This is an implementation of a balanced tree data structure, especially suitable for distributed applications, where different parts of the data structure can reside on
different computers, possibly on the other side of the Earth.
This is because B-nodes can be stored on different computers and for large _t_ the tree is shallow
but lookups, inserts and deletes are still efficient.
For _t = 1000_ one billion entries can be indexed by only 4 lookups.

The special case of _t = 2_ is also called the _2-3-4 tree_ which is isomorphic to the popular Red-Black tree.

## Fibonacci heap
This data structure has been primarily developed for implementing the priority queue in Dijkstra's algorithms.
There are three key operations:
* _insert_: called _V_ times in Dijkstra, inserting each vertex into the priority queue, has both true and amortized _O(1)_ cost.
* _decreaseKey_: called at most _E_ times in Dijkstra, every time an edge is "relaxed", has amortized _O(1)_ cost
* _popMin_: called _V_ times in Dijkstra in each iteration, this is where the cleanup happens, merging the heaps which is
  particularly efficient in batches, it has amortized _O(log n)_ cost, where the heap contains _n_ elements

These amortized costs can be shown using the potential function _Φ = # heaps in root list + 2 * # marked/loser nodes_.
So this provides an implementation of Dijkstra's algorithms with _O(V log V + E)_ complexity.
