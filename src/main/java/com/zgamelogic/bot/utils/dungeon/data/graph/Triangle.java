package com.zgamelogic.bot.utils.dungeon.data.graph;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

@ToString
public class Triangle {

    private Vertex v1, v2, v3;
    @Getter
    @EqualsAndHashCode.Exclude
    private Circumcircle circumcircle;

    public Triangle(Vertex v1, Vertex v2, Vertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        circumcircle = new Circumcircle(v1, v2, v3);
    }

    public Triangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        this(new Vertex(x1, y1), new Vertex(x2, y2), new Vertex(x3, y3));
    }


    public Triangle(Edge e1, Edge e2, Edge e3){
        Set<Vertex> vertexes = new HashSet<>();
        vertexes.addAll(e1.getVertexes());
        vertexes.addAll(e2.getVertexes());
        vertexes.addAll(e3.getVertexes());
        Vertex[] points = vertexes.toArray(new Vertex[vertexes.size()]);
        v1 = points[0];
        v2 = points[1];
        v3 = points[2];
        circumcircle = new Circumcircle(v1, v2, v3);
    }

    public List<Edge> split(Vertex vertex){
        List<Edge> edges = new LinkedList<>();
        edges.add(new Edge(v1, v2));
        edges.add(new Edge(v2, v3));
        edges.add(new Edge(v3, v1));

        edges.add(new Edge(v1, vertex));
        edges.add(new Edge(v2, vertex));
        edges.add(new Edge(v3, vertex));

        return edges;
    }

    public boolean containsEdge(Edge edge){
        List<Edge> edges = new LinkedList<>();
        edges.add(new Edge(v1, v2));
        edges.add(new Edge(v2, v3));
        edges.add(new Edge(v3, v1));
        return edges.contains(edge);
    }

    public List<Edge> getEdges(){
        List<Edge> edges = new LinkedList<>();
        edges.add(new Edge(v1, v2));
        edges.add(new Edge(v2, v3));
        edges.add(new Edge(v3, v1));
        return edges;
    }

    public boolean vertexInside(int[] chords){
        return vertexInside(new Vertex(chords[0], chords[1]));
    }

    public boolean vertexInside(Vertex vertex){
        double dX = vertex.getX() - v3.getX();
        double dY = vertex.getY() - v3.getY();
        double dX21 = v3.getX() - v2.getX();
        double dY12 = v2.getY() - v3.getY();
        double D = dY12 * (v1.getX()-v3.getX()) + dX21 * (v1.getY() - v3.getY());
        double s = dY12*dX + dX21*dY;
        double t = (v3.getY() - v1.getY()) * dX + (v1.getX() - v3.getX()) * dY;
        if (D < 0) return s <= 0 && t <= 0 && s + t >= D;
        return s >= 0 && t >= 0 && s + t <= D;
    }

    public List<Vertex> getVertexes(){
        LinkedList<Vertex> vertexes = new LinkedList<>(Arrays.asList(v1, v2, v3));
        Collections.sort(vertexes);
        return vertexes;
    }

    public boolean sharesVertex(Triangle triangle){
        for(Vertex v : getVertexes()){
            for(Vertex v2 : triangle.getVertexes()){
                if(v.equals(v2)){
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triangle triangle = (Triangle) o;
        return getVertexes().containsAll(triangle.getVertexes());

    }

    @Override
    public int hashCode() {
        return Objects.hash(getVertexes());
    }
}
