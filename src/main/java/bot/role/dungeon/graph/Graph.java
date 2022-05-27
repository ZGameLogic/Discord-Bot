package bot.role.dungeon.graph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Graph {

    private List<Vertex> vertexes;
    private List<Edge> edges;
    private List<Edge> minimumSpanningTree;
    private int[][] map;

    public Graph(List<Vertex> roomCenters, int width , int height, int[][] map) {
        edges = new LinkedList<>();
        minimumSpanningTree = new LinkedList<>();
        vertexes = roomCenters;
        this.map = map;
        bowyerWatsonAlgorithm(width, height);
    }

    private void bowyerWatsonAlgorithm(int width , int height){
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

    private void primsAlgorithm(){
        // TODO create a minimum spanning tree (Prim's Algorithm)
    }

    private void randomHallways(){
        // TODO Randomly choose remaining edges for hallways
    }


    public List<int[]> getHallways(){
        List<int[]> hallways = new LinkedList<>();
        for(Edge e : minimumSpanningTree){
            hallways.add(e.toArray());
        }
        return hallways;
    }

    public List<Edge> getEdges(){
        return edges;
    }

    public List<Edge> getMinimumSpanningTree(){
        return minimumSpanningTree;
    }

}
