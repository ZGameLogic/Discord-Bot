package bot.role.dungeon.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class Node implements Comparable<Node>{

    private Vertex vertex;

    @Setter
    private double costToTravel;
    @Override
    public int compareTo(@NotNull Node o) {
        return Double.compare(o.costToTravel, costToTravel);
    }

    public boolean equalsVertex(Vertex v){
        return v.equals(vertex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return vertex.equals(node.getVertex());
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertex, costToTravel);
    }
}
