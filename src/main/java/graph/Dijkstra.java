package graph;

import fibonacciheap.FibElem;
import fibonacciheap.FibonacciHeap;

import java.util.ArrayList;
import java.util.List;


final class Dijkstra {

  static class Vertex extends FibElem<Vertex> {
    final String handle;
    static class Edge {
      final Vertex to;
      final int weight;

      Edge(Vertex to, int weight) {
        this.to = to;
        this.weight = weight;
      }
    }
    List<Edge> neighbours = new ArrayList<>();

    Vertex(String handle) {
      this.handle = handle;
    }

    void addEdge(Vertex to, int weight) {
      neighbours.add(new Edge(to, weight));
    }
    Iterable<Edge> getNeighbours() {
      return neighbours;
    }
  }

  final private Vertex[] vertices;

  Dijkstra(int noVertices, List<Integer> from, List<Integer> to, List<Integer> weights) {
    assert(from.size() == to.size() && to.size() == weights.size());
    vertices = new Vertex[noVertices];
    for (int i = 0; i < noVertices; i++)
      vertices[i] = new Vertex(Integer.toString(i));
    for (int i = 0; i < from.size(); i++)
      vertices[from.get(i)].addEdge(vertices[to.get(i)], weights.get(i));
  }


  void computeShortestPaths(int s) {
    FibonacciHeap<Vertex> fibonacciHeap = new FibonacciHeap<>();
    for (Vertex vertex : vertices) {
      fibonacciHeap.insert(vertex);
    }

    fibonacciHeap.decreaseKey(vertices[s], 0);

    while (!fibonacciHeap.isEmpty()) {
      Vertex p = fibonacciHeap.peekMin();
      fibonacciHeap.popMin();

      System.out.println("visiting "+p.getKey());

      for (Vertex.Edge edge : p.getNeighbours()) {
        if (edge.to.getKey() > p.getKey() + edge.weight) {
          fibonacciHeap.decreaseKey(edge.to, p.getKey() + edge.weight);
        }
      }
    }

    for (Vertex v : vertices)
      System.out.print(v.getKey() + " ");
    System.out.println();

  }

}

class Tester {
  public static void main(String[] args) {
    Dijkstra graph = new Dijkstra(4, List.of(0, 0, 2, 3), List.of(1, 2, 3, 0), List.of(10, 20, 5, 15));
    graph.computeShortestPaths(0);
  }
}
