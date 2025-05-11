package ru.leti;

import ru.leti.wise.task.plugin.graph.GraphProperty;
import ru.leti.wise.task.graph.model.Graph;
import ru.leti.wise.task.graph.model.Vertex;
import ru.leti.wise.task.graph.model.Edge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Проверка максимальной планарности графа:
 */
public class IsMaximallyPlanar implements GraphProperty {

    @Override
    public boolean run(Graph graph) {
        int n = graph.getVertexCount();
        int m = graph.getEdgeCount();
        // 1) Тривиальные случаи: 0 или 1 вершина — максимально планарен
        if (n <= 1) {
            return true;
        }
        // 2) Граф должен быть неориентированным
        if (graph.isDirect()) {
            return false;
        }
        // 3) Должен быть планарным
        if (!new IsPlanar().run(graph)) {
            return false;
        }
        if (n==2){
            return (m==1);
        }
        // 4) Числовой критерий максимальной планарности для n>=3: m == 3n-6
        return (m == 3 * n - 6);
    }
}


