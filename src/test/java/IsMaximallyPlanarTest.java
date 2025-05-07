import ru.leti.IsMaximallyPlanar;
import ru.leti.wise.task.graph.util.FileLoader;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class IsMaximallyPlanarTest {
    @Test
    public void IsMaximallyPlanarTest() throws FileNotFoundException {
        IsMaximallyPlanar isMaxPlanar = new IsMaximallyPlanar();
        var graph1 = FileLoader.loadGraphFromJson("src/test/resources/notMax1.json");

        assertThat(isMaxPlanar.run(graph1)).isTrue();
    }
}
