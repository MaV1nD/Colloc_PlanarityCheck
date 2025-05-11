package ru.leti;

import ru.leti.wise.task.plugin.graph.GraphProperty;
import ru.leti.wise.task.graph.model.Graph;
import ru.leti.wise.task.graph.model.Vertex;
import ru.leti.wise.task.graph.model.Edge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Рекурсивный алгоритм проверки планарности с мемоизацией (улучшенный Rabin–Scott),
 * адаптированный для работы с несвязными графами.
 */
public class IsPlanar implements GraphProperty {
    /** Кэш: ключ (строка вершин|рёбер) → результат планарности */
    private final Map<String, Boolean> cache = new HashMap<>();

    @Override
    public boolean run(Graph graph) {
        // 1) Собираем все вершины и рёбра
        Set<Integer> verts = graph.getVertexList().stream()
                .map(Vertex::getId)
                .collect(Collectors.toSet());
        Set<EdgePair> edges = graph.getEdgeList().stream()
                .map(e -> makeEdge(e.getSource(), e.getTarget()))
                .collect(Collectors.toSet());

        // 2) Разбиваем на связные компоненты
        List<Set<Integer>> components = getComponents(verts, edges);

        // 3) Проверяем каждую компоненту отдельно
        for (Set<Integer> compVerts : components) {
            // Фильтруем рёбра, полностью лежащие внутри компоненты
            Set<EdgePair> compEdges = edges.stream()
                    .filter(ep -> compVerts.contains(ep.u) && compVerts.contains(ep.v))
                    .collect(Collectors.toSet());
            if (!testPlanar(compVerts, compEdges)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Разбиение графа (V, E) на связные компоненты.
     * @return список множеств вершин — по одной компоненте на элемент списка.
     */
    private List<Set<Integer>> getComponents(Set<Integer> verts, Set<EdgePair> edges) {
        // строим временный список смежности
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (int v : verts) adj.put(v, new ArrayList<>());
        for (EdgePair e : edges) {
            adj.get(e.u).add(e.v);
            adj.get(e.v).add(e.u);
        }

        List<Set<Integer>> comps = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        for (int start : verts) {
            if (seen.contains(start)) continue;
            // BFS/DFS из start
            Deque<Integer> dq = new ArrayDeque<>();
            dq.add(start);
            seen.add(start);
            Set<Integer> comp = new HashSet<>();
            while (!dq.isEmpty()) {
                int u = dq.poll();
                comp.add(u);
                for (int w : adj.get(u)) {
                    if (seen.add(w)) {
                        dq.add(w);
                    }
                }
            }
            comps.add(comp);
        }
        return comps;
    }

    /**
     * Рекурсивный тест планарности для одной связной компоненты.
     */
    private boolean testPlanar(Set<Integer> verts, Set<EdgePair> edges) {
        String key = makeKey(verts, edges);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        int n = verts.size();
        int m = edges.size();

        // Любой лес (m==n-1 или меньше) — планарен
        if (m == 0) {
            return cachePut(key, true);
        }
        // менее 3 вершин — всегда планарен
        if (n < 3) {
            return cachePut(key, true);
        }
        // быстрый числовой фильтр Эйлера
        if (m > 3 * n - 6) {
            return cachePut(key, false);
        }
        // для n<=6 делаем полную проверку запрещённых подграфов
        if (n <= 6) {
            if (containsK5(verts, edges) || containsK33(verts, edges)) {
                return cachePut(key, false);
            }
            return cachePut(key, true);
        }

        // Рекурсивное ветвление по произвольному ребру
        EdgePair e = edges.iterator().next();

        // Вариант 1: удаляем e
        Set<EdgePair> edgesDel = new HashSet<>(edges);
        edgesDel.remove(e);
        if (testPlanar(verts, edgesDel)) {
            return cachePut(key, true);
        }

        // Вариант 2: сжимаем e (u,v → u)
        int u = e.u, v = e.v;
        Set<Integer> vertsCon = new HashSet<>(verts);
        vertsCon.remove(v);
        Set<EdgePair> edgesCon = new HashSet<>();
        for (EdgePair ep : edges) {
            int a = (ep.u == v ? u : ep.u);
            int b = (ep.v == v ? u : ep.v);
            if (a != b) {
                edgesCon.add(makeEdge(a, b));
            }
        }
        if (testPlanar(vertsCon, edgesCon)) {
            return cachePut(key, true);
        }

        // оба варианта не дали планарности
        return cachePut(key, false);
    }

    /** Поместить в кэш и вернуть значение */
    private boolean cachePut(String key, boolean value) {
        cache.put(key, value);
        return value;
    }

    /** Строит строковый ключ из списков вершин и рёбер */
    private String makeKey(Set<Integer> verts, Set<EdgePair> edges) {
        String vs = verts.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String es = edges.stream()
                .map(ep -> ep.u + "-" + ep.v)
                .sorted()
                .collect(Collectors.joining(","));
        return vs + "|" + es;
    }

    /** Упорядоченная пара (u<v) */
    private EdgePair makeEdge(int a, int b) {
        return (a < b) ? new EdgePair(a, b) : new EdgePair(b, a);
    }

    /** Полный граф на 5 вершинах K5? */
    private boolean containsK5(Set<Integer> verts, Set<EdgePair> edges) {
        List<Integer> list = new ArrayList<>(verts);
        int sz = list.size();
        for (int i = 0; i < sz - 4; i++)
            for (int j = i + 1; j < sz - 3; j++)
                for (int k = j + 1; k < sz - 2; k++)
                    for (int l = k + 1; l < sz - 1; l++)
                        for (int m = l + 1; m < sz; m++) {
                            Set<EdgePair> clique = new HashSet<>();
                            int[] vs = {list.get(i), list.get(j), list.get(k), list.get(l), list.get(m)};
                            for (int x = 0; x < 5; x++)
                                for (int y = x + 1; y < 5; y++) {
                                    clique.add(makeEdge(vs[x], vs[y]));
                                }
                            if (edges.containsAll(clique)) {
                                return true;
                            }
                        }
        return false;
    }

    /** Полный двудольный граф K3,3? */
    private boolean containsK33(Set<Integer> verts, Set<EdgePair> edges) {
        List<Integer> list = new ArrayList<>(verts);
        int sz = list.size();
        for (int i = 0; i < sz - 2; i++)
            for (int j = i + 1; j < sz - 1; j++)
                for (int k = j + 1; k < sz; k++) {
                    Set<Integer> U = Set.of(list.get(i), list.get(j), list.get(k));
                    List<Integer> rest = list.stream()
                            .filter(v -> !U.contains(v))
                            .collect(Collectors.toList());
                    int rs = rest.size();
                    for (int a = 0; a < rs - 2; a++)
                        for (int b = a + 1; b < rs - 1; b++)
                            for (int c = b + 1; c < rs; c++) {
                                Set<EdgePair> bip = new HashSet<>();
                                int[] us = {list.get(i), list.get(j), list.get(k)};
                                int[] vs = {rest.get(a), rest.get(b), rest.get(c)};
                                for (int x : us)
                                    for (int y : vs) {
                                        bip.add(makeEdge(x, y));
                                    }
                                if (edges.containsAll(bip)) {
                                    return true;
                                }
                            }
                }
        return false;
    }

    /** Вспомогательный класс для ребра-ключа */
    private static class EdgePair {
        final int u, v;
        EdgePair(int u, int v) { this.u = u; this.v = v; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EdgePair)) return false;
            EdgePair p = (EdgePair) o;
            return u == p.u && v == p.v;
        }
        @Override public int hashCode() {
            return Objects.hash(u, v);
        }
    }
}
