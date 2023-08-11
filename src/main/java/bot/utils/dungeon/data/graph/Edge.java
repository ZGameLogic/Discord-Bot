package bot.utils.dungeon.data.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Getter
public class Edge implements Comparable<Edge> {

    private Vertex startVertex;
    private Vertex endVertex;

    public Edge(int x1, int y1, int x2, int y2){
        this(new Vertex(x1, y1), new Vertex(x2, y2));
    }

    public double length(){
        return startVertex.getDistance(endVertex);
    }

    public List<Vertex> getVertexes(){
        return new LinkedList<>(Arrays.asList(new Vertex[]{startVertex, endVertex}));
    }

    public void paintEdge(Graphics2D pane){
        Color oldColor = pane.getColor();
        pane.setColor(Color.cyan);
        pane.drawLine((int) ((startVertex.getX() + 1) * 20 - 2.5), (int) ((startVertex.getY() + 1) * 20 - 2.5), (int) ((endVertex.getX() + 1) * 20 - 2.5), (int) ((endVertex.getY() + 1) * 20 - 2.5));
        pane.setColor(oldColor);
    }

    public boolean connects(Edge edge){
        return edge.connects(startVertex) || edge.connects(endVertex);
    }

    public boolean connects(Vertex vertex){
        return startVertex.equals(vertex) || endVertex.equals(vertex);
    }

    public int[] toArray(){
        return new int[]{(int)startVertex.getX(), (int)startVertex.getY(), (int)endVertex.getX(), (int)endVertex.getY()};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return (startVertex.equals(edge.startVertex) && endVertex.equals(edge.endVertex)) ||
                (startVertex.equals(edge.endVertex) && endVertex.equals(edge.startVertex));
    }

    @Override
    public int hashCode() {
        return Objects.hash(startVertex, endVertex);
    }

    @Override
    public int compareTo(@NotNull Edge o) {
        return Double.compare(length(), o.length());
    }
}
