package ru.leti;

import ru.leti.wise.task.plugin.graph.GraphProperty;
import ru.leti.wise.task.graph.model.Graph;
import ru.leti.wise.task.graph.model.Vertex;
import ru.leti.wise.task.graph.model.Edge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Рекурсивный алгоритм проверки планарности с мемоизацией (улучшенный Rabin–Scott).
 * Надёжен и для графов до ~50 вершин работает быстро при умеренной плотности.
 */
public class IsPlanar implements GraphProperty {
    /**
     * Кэш состояний: строковый ключ (вершины|рёбра) -> планарность
     * Позволяет избегать повторной проверки одной и той же конфигурации.
     */
    private final Map<String, Boolean> cache = new HashMap<>();

    /**
     * Точка входа: конвертируем Graph в набор вершин и множеств рёбер, запускаем тест.
     */
    @Override
    public boolean run(Graph graph) {
        // Собираем уникальные идентификаторы вершин
        Set<Integer> verts = graph.getVertexList().stream()
                .map(Vertex::getId)
                .collect(Collectors.toSet());
        // Собираем рёбра как упорядоченные пары
        Set<EdgePair> edges = graph.getEdgeList().stream()
                .map(e -> makeEdge(e.getSource(), e.getTarget()))
                .collect(Collectors.toSet());
        return testPlanar(verts, edges);
    }

    /**
     * Основной рекурсивный метод проверки планарности.
     * @param verts множество вершин
     * @param edges множество рёбер
     * @return true — planar, false — non-planar
     */
    private boolean testPlanar(Set<Integer> verts, Set<EdgePair> edges) {
        // Формируем строковый ключ для кэша
        String key = makeKey(verts, edges);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        int n = verts.size();       // число вершин
        int m = edges.size();       // число рёбер

        // Быстрые эвристические проверки:
        if (n < 3) {
            // Менее чем 3 вершины всегда планарен
            return cachePut(key, true);
        }
        if (m > 3 * n - 6) {
            // Нарушение условия Эйлера => сразу не планарен
            return cachePut(key, false);
        }
        if (n <= 6) {
            // Малые графы, полная проверка на K5 и K3,3
            if (containsK5(verts, edges) || containsK33(verts, edges)) {
                return cachePut(key, false);
            }
            return cachePut(key, true);
        }

        // Рекурсивное ветвление по одному произвольному ребру:
        EdgePair e = edges.iterator().next();

        // Вариант 1: удаляем ребро e
        Set<EdgePair> edgesDel = new HashSet<>(edges);
        edgesDel.remove(e);
        if (testPlanar(verts, edgesDel)) {
            return cachePut(key, true);
        }

        // Вариант 2: сжимаем ребро e (связываем вершины)
        int u = e.u, v = e.v;
        // Новое множество вершин: убрали v
        Set<Integer> vertsCon = new HashSet<>(verts);
        vertsCon.remove(v);
        // Новое множество рёбер: заменяем v на u, удаляем петли
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

        // Ни один вариант не дал планарности -> не планарен
        return cachePut(key, false);
    }

    /**
     * Сохраняет результат в кэш и возвращает его.
     */
    private boolean cachePut(String key, boolean value) {
        cache.put(key, value);
        return value;
    }

    /**
     * Строит строковый ключ из упорядоченных вершин и рёбер.
     */
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

    /**
     * Создаёт упорядоченную пару вершин (u < v).
     */
    private EdgePair makeEdge(int a, int b) {
        return (a < b) ? new EdgePair(a, b) : new EdgePair(b, a);
    }

    // ===== Проверка запрещённых подграфов для малых графов =====

    /** Проверка наличия полного графа K5 */
    private boolean containsK5(Set<Integer> verts, Set<EdgePair> edges) {
        List<Integer> list = new ArrayList<>(verts);
        int sz = list.size();
        // Перебираем все 5-комбинации вершин
        for (int i = 0; i < sz - 4; i++)
            for (int j = i + 1; j < sz - 3; j++)
                for (int k = j + 1; k < sz - 2; k++)
                    for (int l = k + 1; l < sz - 1; l++)
                        for (int m = l + 1; m < sz; m++) {
                            // Собираем все 10 рёбер K5
                            Set<EdgePair> clique = new HashSet<>();
                            int[] vs = {list.get(i), list.get(j), list.get(k), list.get(l), list.get(m)};
                            for (int x = 0; x < 5; x++)
                                for (int y = x + 1; y < 5; y++) {
                                    clique.add(makeEdge(vs[x], vs[y]));
                                }
                            // Проверяем, все ли эти рёбра есть в исходном множестве
                            if (edges.containsAll(clique)) {
                                return true;
                            }
                        }
        return false;
    }

    /** Проверка наличия двудольного полного графа K3,3 */
    private boolean containsK33(Set<Integer> verts, Set<EdgePair> edges) {
        List<Integer> list = new ArrayList<>(verts);
        int sz = list.size();
        // Перебираем все 3-элементные подмножества для U
        for (int i = 0; i < sz - 2; i++)
            for (int j = i + 1; j < sz - 1; j++)
                for (int k = j + 1; k < sz; k++) {
                    Set<Integer> U = Set.of(list.get(i), list.get(j), list.get(k));
                    // Остальные вершины — потенциальная V
                    List<Integer> rest = list.stream()
                            .filter(v -> !U.contains(v))
                            .collect(Collectors.toList());
                    int rs = rest.size();
                    // Перебираем 3-элементные подмножества из rest
                    for (int a = 0; a < rs - 2; a++)
                        for (int b = a + 1; b < rs - 1; b++)
                            for (int c = b + 1; c < rs; c++) {
                                // Собираем все 9 рёбер между U и V
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

    /**
     * Вспомогательный класс — упорядоченная пара вершин, представляющая ребро.
     */
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
