//import ru.leti.IsMaximallyPlanar;
//import ru.leti.wise.task.graph.util.FileLoader;
//import org.junit.jupiter.api.Test;
//
//import java.io.FileNotFoundException;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
//public class IsMaximallyPlanarTest {
//    @Test
//    public void IsMaximallyPlanarTest() throws FileNotFoundException {
//        IsMaximallyPlanar isMaxPlanar = new IsMaximallyPlanar();
//        var graph1 = FileLoader.loadGraphFromJson("src/test/resources/graph-data-1746817699370.json");
//
//        assertThat(isMaxPlanar.run(graph1)).isTrue();
//    }
//}
import ru.leti.IsMaximallyPlanar;
import ru.leti.wise.task.graph.util.FileLoader;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class IsMaximallyPlanarTest {

    @Test
    public void testGraph1() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_1_empty.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isTrue(); // Тривиален
    }

    @Test
    public void testGraph2() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_2_single_node.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isTrue(); // Тривиален
    }

    @Test
    public void testGraph3() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_3_two_nodes_no_edge.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isFalse(); // Нет рёбер — больше нельзя добавить
    }

    @Test
    public void testGraph4() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_4_two_nodes_one_edge.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isTrue();
    }

    @Test
    public void testGraph5() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_5_path_P3.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isFalse(); // Можно добавить ребро
    }

    @Test
    public void testGraph6() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_6_K3.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isTrue();
    }

    @Test
    public void testGraph7() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_7_C4.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isFalse(); // Можно добавить диагональ
    }

    @Test
    public void testGraph8() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_8_K4.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isTrue();
    }

    @Test
    public void testGraph9() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_9_K5.json");
        // Граф непланарен — пропускаем или ожидаем false по определению
        assertThat(new IsMaximallyPlanar().run(graph)).isFalse();
    }

    @Test
    public void testGraph10() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_10_K2_3.json");
        // Граф непланарен — пропускаем или ожидаем false по определению
        assertThat(new IsMaximallyPlanar().run(graph)).isFalse();
    }

    @Test
    public void testGraph11() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_11_pentagon_triangulation.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isTrue();
    }

    @Test
    public void testGraph12() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_12_tree_5.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isFalse(); // Это дерево, не максимальный
    }

    @Test
    public void testGraph13() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_13_incoherent.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isFalse();
    }

    @Test
    public void testGraph14() throws FileNotFoundException {
        var graph = FileLoader.loadGraphFromJson("src/test/resources/graph_14_K3_3.json");
        assertThat(new IsMaximallyPlanar().run(graph)).isFalse();
    }
}

