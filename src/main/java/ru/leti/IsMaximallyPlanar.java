package ru.leti;

import ru.leti.wise.task.plugin.graph.GraphProperty;
import ru.leti.wise.task.graph.model.Graph;
import ru.leti.wise.task.graph.model.Vertex;
import ru.leti.wise.task.graph.model.Edge;
import ru.leti.IsPlanar;

import java.util.*;

/**
 * Проверка максимальной планарности графа на основе проверки планарности.
 */
public class IsMaximallyPlanar implements GraphProperty {
    @Override
    public boolean run(Graph graph) {
        // 1. Граф должен быть неориентированным
        if (graph.isDirect()) {
            return false;
        }
        int n = graph.getVertexCount();
        int m = graph.getEdgeCount();
        // 2. Должен быть связным
        if (!isConnected(graph)) {
            return false;
        }
        // 3. Должен быть планарным
        if (!new IsPlanar().run(graph)) {
            return false;
        }
        // 4. Должно быть ровно 3n-6 рёбер
        if (n < 3) {
            return false;
        }
        if (m != 3 * n - 6) {
            return false;
        }
        return true;
    }

    /** Проверка связности через DFS */
    private boolean isConnected(Graph graph) {
        List<Vertex> verts = graph.getVertexList();
        if (verts.isEmpty()) {
            return true;
        }
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (Vertex v : verts) {
            adj.put(v.getId(), new ArrayList<>());
        }
        for (Edge e : graph.getEdgeList()) {
            int u = e.getSource();
            int v = e.getTarget();
            adj.get(u).add(v);
            adj.get(v).add(u);
        }
        Set<Integer> visited = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();
        int start = verts.get(0).getId();
        stack.push(start);
        visited.add(start);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            for (int w : adj.get(u)) {
                if (!visited.contains(w)) {
                    visited.add(w);
                    stack.push(w);
                }
            }
        }
        return visited.size() == verts.size();
    }
}
