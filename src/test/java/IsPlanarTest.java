import ru.leti.IsPlanar;
import ru.leti.wise.task.graph.util.FileLoader;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class IsPlanarTest {
    @Test
    public void IsPlanarTest() throws FileNotFoundException {
        IsPlanar isPlanar = new IsPlanar();
        var graph1 = FileLoader.loadGraphFromJson("src/test/resources/gr1.json");

        assertThat(isPlanar.run(graph1)).isTrue();
    }
}
