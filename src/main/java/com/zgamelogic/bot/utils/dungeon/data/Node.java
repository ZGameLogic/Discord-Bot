package com.zgamelogic.bot.utils.dungeon.data;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
public class Node implements Comparable<Node> {
    private int x, y;
    @Setter
    private double g, h;
    @Setter
    private Node parent;

    public static Node endNode;

    /**
     * This should only be used for the start and end nodes
     * @param x X pos of the node
     * @param y Y pos of the node
     */
    public Node(int x, int y){
        this.x = x;
        this.y = y;
        g = 0;
        if(endNode != null) {
            h = generateH();
        } else {
            h = -1; // this should only be for the end node
        }
    }

    /**
     * Generates a node. Creates a G and H value as well.
     * @param x X pos of the node
     * @param y y pos of the node
     * @param parent the parent of the node
     * @param cost the code it takes to traverse this node
     */
    public Node(int x, int y, Node parent, int cost){
        this.x = x;
        this.y = y;
        g = parent.getG() + cost;
        this.parent = parent;
        if(endNode != null) {
            h = generateH();
        } else {
            h = -1; // this should only be for the end node
        }
    }

    private double generateH(){
        return Math.sqrt(Math.pow(endNode.getX() - x, 2) + Math.pow(endNode.getY() - y, 2));
    }

    public double getF(){
        return h + g;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Node && equalsNode((Node)obj);
    }

    public boolean equalsNode(Node node){
        return node.getX() == x && node.getY() == y;
    }

    @Override
    public int compareTo(@NotNull Node o) {
        if(getF() < o.getF()){
            return 1;
        } else if (getF() > o.getF()){
            return -1;
        }
        return 0;
    }
}