package bot.utils.dungeon.data.graph;

import lombok.Getter;

import java.util.*;
import java.util.List;

public class Graph {

    private List<Vertex> vertexes;
    private List<Edge> edges;
    @Getter
    private List<Edge> minimumSpanningTree;
    private int width, height;

    public Graph(List<Vertex> roomCenters, int width , int height) {
        edges = new LinkedList<>();
        minimumSpanningTree = new LinkedList<>();
        vertexes = roomCenters;
        this.width = width;
        this.height = height;
        bowyerWatsonAlgorithm();
        primsAlgorithm();
    }

    private void primsAlgorithm(){
        List<Edge> mst = new LinkedList<>(); // minimal spanning tree
        Queue<Edge> choices; // list of edges we get to pick from
        Set<Vertex> nodes = new HashSet<>(); // nodes that have been added to the tree
        nodes.add(getBottomLeftVertex());
        while(mst.size() < vertexes.size() - 1){
            choices = new PriorityQueue<>();
            for(Vertex node : nodes){ // add all options to pick from
                List<Edge> toAdd = getConnectedEdges(node);
                List<Edge> notToAdd = new LinkedList<>();
                for(Edge e : toAdd){ // remove options that would take us backward
                    List<Vertex> points = new LinkedList<>(e.getVertexes());
                    points.remove(node);
                    if(nodes.contains(points.get(0))){
                        notToAdd.add(e);
                    }
                }
                toAdd.removeAll(notToAdd);
                choices.addAll(toAdd);
            }
            choices.removeAll(mst); // remove all existing edges
            Edge nextSmallestEdge = choices.remove(); // next edge to add to tree
            nodes.addAll(nextSmallestEdge.getVertexes()); // add the nodes in
            mst.add(nextSmallestEdge); // add the edge in to mst
        }
        minimumSpanningTree = mst;
    }

    private void bowyerWatsonAlgorithm(){
        Triangle superTriangle = new Triangle(0, -height, width * 2, height, 0, height); // must be large enough to completely contain all the points in pointList
        Set<Triangle> triangles = new HashSet<>();
        triangles.add(superTriangle);
        for(Vertex vertex : vertexes){ // add all the points one at a time to the triangulation
            List<Triangle> badTriangles = new LinkedList<>();
            for(Triangle t : triangles){ // first find all the triangles that are no longer valid due to the insertion
                if(t.getCircumcircle().containsVertext(vertex)){
                    badTriangles.add(t);
                }
            }
            List<Edge> polygon = new LinkedList<>();
            for(Triangle badTriangle : badTriangles){ // find the boundary of the polygonal hole
                for(Edge edge : badTriangle.getEdges()) { // go through each edge of bad triangle
                    boolean shouldAdd = true;
                    for (Triangle checkT : badTriangles) { // go through all the bad triangles
                        if (!checkT.equals(badTriangle) && checkT.containsEdge(edge)){ // if the current triangle isn't the bad one, and the current triangle contains the edge
                            shouldAdd = false;
                        }
                    }
                    if(shouldAdd) {
                        polygon.add(edge);
                    }
                }
            }
            triangles.removeAll(badTriangles);
            for(Edge edge : polygon){ // re-triangulate the polygonal hole
                Edge e1 = edge;
                Edge e2 = new Edge(vertex, edge.getEndVertex());
                Edge e3 = new Edge(vertex, edge.getStartVertex());
                Triangle newTriangle = new Triangle(e1, e2, e3);
                triangles.add(newTriangle);
            }
            edges = new LinkedList<>();
            for(Triangle t : triangles){
                edges.addAll(t.getEdges());
            }
        }

        // Delete all triangles connected to the super triangle
        List<Edge> toDelete = new LinkedList<>();
        for(Edge edge : edges){
            for(Edge te : superTriangle.getEdges()){
                if(edge.connects(te)){
                    toDelete.add(edge);
                    break;
                }
            }
        }
        edges.removeAll(toDelete);
        this.edges = new LinkedList<>(edges);
    }

    private List<Edge> getConnectedEdges(Vertex vertex){
        List<Edge> edges = new LinkedList<>();
        for(Edge e : this.edges){
            if(e.connects(vertex)){
                edges.add(e);
            }
        }
        return edges;
    }

    public List<int[]> getHallways(){
        List<int[]> hallways = new LinkedList<>();
        for(Edge e : minimumSpanningTree){
            hallways.add(e.toArray());
        }
        return hallways;
    }

    public Vertex getBottomLeftVertex(){
        Vertex topRight = new Vertex(width, 0);
        Vertex furthest = vertexes.get(0);
        for(Vertex v : vertexes){
            if(v.getDistance(topRight) > furthest.getDistance(topRight)){
                furthest = v;
            }
        }
        return furthest;
    }
}
