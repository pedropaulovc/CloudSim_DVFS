/*************************************************************************
 *  Compilation:  javac DigraphGenerator.java
 *  Execution:    java DigraphGenerator V E
 *  Dependencies: Digraph.java
 *
 *  A digraph generator.
 *  
 *************************************************************************/

public class DigraphGenerator {
    private static final class Edge implements Comparable<Edge> {
        private int v;
        private int w;
        private Edge(int v, int w) {
            this.v = v;
            this.w = w;
        }
        public int compareTo(Edge that) {
            if (this.v < that.v) return -1;
            if (this.v > that.v) return +1;
            if (this.w < that.w) return -1;
            if (this.w > that.w) return +1;
            return 0;
        }
    }

    // complete digraph
    public static Digraph complete(int V) {
        return simple(V, V*(V-1));
    }

    // tournament
    public static Digraph tournament(int V) {
        return dag(V, V*(V-1)/2);
    }

    // random simple digraph with V vertices and E edges
    public static Digraph simple(int V, int E) {
        if (E > (long) V*(V-1)) throw new RuntimeException("Too many edges");
        if (E < 0)              throw new RuntimeException("Too few edges");
        Digraph G = new Digraph(V);
        SET<Edge> set = new SET<Edge>();
        while (G.E() < E) {
            int v = StdRandom.uniform(V);
            int w = StdRandom.uniform(V);
            Edge e = new Edge(v, w);
            if ((v != w) && !set.contains(e)) {
                set.add(e);
                G.addEdge(v, w);
            }
        }
        return G;
    }

    // create random DAG with V vertices and E edges
    // Note: not uniformly random among all such DAGs
    public static Digraph dag(int V, int E) {
        if (E > (long) V*(V-1) / 2) throw new RuntimeException("Too many edges");
        if (E < 0)                  throw new RuntimeException("Too few edges");
        Digraph G = new Digraph(V);
        SET<Edge> set = new SET<Edge>();
        int[] vertices = new int[V];
        for (int i = 0; i < V; i++) vertices[i] = i;
        StdRandom.shuffle(vertices);
        while (G.E() < E) {
            int v = StdRandom.uniform(V);
            int w = StdRandom.uniform(V);
            Edge e = new Edge(v, w);
            if ((v < w) && !set.contains(e)) {
                set.add(e);
                G.addEdge(vertices[v], vertices[w]);
            }
        }
        return G;
    }

    // create random rooted-in DAG with V vertices and E edges
    // Note: not uniformly random among all such DAGs
    public static Digraph rootedInDAG(int V, int E) {
        if (E > (long) V*(V-1) / 2) throw new RuntimeException("Too many edges");
        if (E < V-1)                throw new RuntimeException("Too few edges");
        Digraph G = new Digraph(V);
        SET<Edge> set = new SET<Edge>();

        // fix a topological order
        int[] vertices = new int[V];
        for (int i = 0; i < V; i++) vertices[i] = i;
        StdRandom.shuffle(vertices);

        // one edge pointing from each vertex, other than the root = vertices[0]
        for (int v = 0; v < V-1; v++) {
            int w = StdRandom.uniform(v+1, V);
            Edge e = new Edge(v, w);
            set.add(e);
            G.addEdge(vertices[v], vertices[w]);
        }

        while (G.E() < E) {
            int v = StdRandom.uniform(V);
            int w = StdRandom.uniform(V);
            Edge e = new Edge(v, w);
            if ((v < w) && !set.contains(e)) {
                set.add(e);
                G.addEdge(vertices[v], vertices[w]);
            }
        }
        return G;
    }

    // create random rooted-out DAG with V vertices and E edges
    // Note: not uniformly random among all such DAGs
    public static Digraph rootedOutDAG(int V, int E) {
        if (E > (long) V*(V-1) / 2) throw new RuntimeException("Too many edges");
        if (E < V-1)                throw new RuntimeException("Too few edges");
        Digraph G = new Digraph(V);
        SET<Edge> set = new SET<Edge>();

        // fix a topological order
        int[] vertices = new int[V];
        for (int i = 0; i < V; i++) vertices[i] = i;
        StdRandom.shuffle(vertices);

        // one edge pointing from each vertex, other than the root = vertices[0]
        for (int v = 0; v < V-1; v++) {
            int w = StdRandom.uniform(v+1, V);
            Edge e = new Edge(v, w);
            set.add(e);
            G.addEdge(vertices[w], vertices[v]);
        }

        while (G.E() < E) {
            int v = StdRandom.uniform(V);
            int w = StdRandom.uniform(V);
            Edge e = new Edge(v, w);
            if ((v < w) && !set.contains(e)) {
                set.add(e);
                G.addEdge(vertices[w], vertices[v]);
            }
        }
        return G;
    }

    // create random rooted-in tree with V vertices
    // Note: not uniformly random among all such DAGs
    public static Digraph rootedInTree(int V) {
        return rootedInDAG(V, V-1);
    }

    // create random rooted-in tree with V vertices
    // Note: not uniformly random among all such DAGs
    public static Digraph rootedOutTree(int V) {
        return rootedOutDAG(V, V-1);
    }

    // create path with V vertices
    public static Digraph path(int V) {
        Digraph G = new Digraph(V);
        int[] vertices = new int[V];
        for (int i = 0; i < V; i++) vertices[i] = i;
        StdRandom.shuffle(vertices);
        for (int i = 0; i < V-1; i++) {
            G.addEdge(vertices[i], vertices[i+1]);
        }
        return G;
    }

    // create complete binary tree with V vertices
    public static Digraph binaryTree(int V) {
        Digraph G = new Digraph(V);
        int[] vertices = new int[V];
        for (int i = 0; i < V; i++) vertices[i] = i;
        StdRandom.shuffle(vertices);
        for (int i = 1; i < V; i++) {
            G.addEdge(vertices[i], vertices[(i-1)/2]);
        }
        return G;
    }

    // create cycle with V vertices
    public static Digraph cycle(int V) {
        Digraph G = new Digraph(V);
        int[] vertices = new int[V];
        for (int i = 0; i < V; i++) vertices[i] = i;
        StdRandom.shuffle(vertices);
        for (int i = 0; i < V-1; i++) {
            G.addEdge(vertices[i], vertices[i+1]);
        }
        G.addEdge(vertices[V-1], vertices[0]);
        return G;
    }


    // test client
    public static void main(String[] args) {
        int V = Integer.parseInt(args[0]);
        int E = Integer.parseInt(args[1]);
        System.out.println("complete graph");
        System.out.println(complete(V));
        System.out.println();

        System.out.println("simple");
        System.out.println(simple(V, E));
        System.out.println();

        System.out.println("path");
        System.out.println(path(V));
        System.out.println();

        System.out.println("cycle");
        System.out.println(cycle(V));
        System.out.println();

        System.out.println("binary tree");
        System.out.println(binaryTree(V));
        System.out.println();

        System.out.println("tournament");
        System.out.println(tournament(V));
        System.out.println();

        System.out.println("DAG");
        System.out.println(dag(V, E));
        System.out.println();

        System.out.println("rooted-in DAG");
        System.out.println(rootedInDAG(V, E));
        System.out.println();

        System.out.println("rooted-out DAG");
        System.out.println(rootedOutDAG(V, E));
        System.out.println();

        System.out.println("rooted-in tree");
        System.out.println(rootedInTree(V));
        System.out.println();

        System.out.println("rooted-out DAG");
        System.out.println(rootedOutTree(V));
        System.out.println();
    }

}
