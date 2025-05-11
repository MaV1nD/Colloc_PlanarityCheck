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
        int n = graph.getVertexCount();
        int m = graph.getEdgeCount();

        // Тривиальные случаи: граф с 0,1,2 вершинами не может иметь "лишних" ребер
        if (n < 3) {
            return true;
        }

        // 1) Должен быть неориентированным
        if (graph.isDirect()) {
            return false;
        }

        // 2) Должен быть планарным
        if (!new IsPlanar().run(graph)) {
            return false;
        }

        // 3) Числовой критерий: ровно 3n - 6 рёбер
        return (m == 3 * n - 6);
    }
}