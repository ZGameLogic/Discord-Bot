package bot.role.dungeon.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

@AllArgsConstructor
@Getter
@ToString
public class Vertex implements Comparable<Vertex>{

    private double x, y;

    public double getDistance(Vertex vertex){
        return Math.sqrt(Math.pow(vertex.getX() - x, 2) + Math.pow(vertex.getY() - y, 2));
    }

    public double getDistance(double x, double y){
        return getDistance(new Vertex(x, y));
    }

    public Vertex(double[] chords){
        x = chords[0];
        y = chords[1];
    }

    public void paintVertex(Graphics2D pane, int count){
        Color oldColor = pane.getColor();
        if(count > 2)
         pane.setColor(Color.magenta);
        pane.fillOval((int)((x + 1) * 20 - 2.5), (int)((y + 1) * 20 - 2.5), 5, 5);
        pane.setColor(oldColor);
    }

    /**
     * Gets the angle between three vertexes
     * @param leg1
     * @param leg2
     * @param origin
     * @return
     */
    public static double getAngle(Vertex leg1, Vertex leg2, Vertex origin){
        double p0c = Math.sqrt(Math.pow(origin.x-leg1.x,2)+
                Math.pow(origin.y-leg1.y,2)); // p0->c (b)
        double p1c = Math.sqrt(Math.pow(origin.x-leg2.x,2)+
                Math.pow(origin.y-leg2.y,2)); // p1->c (a)
        double p0p1 = Math.sqrt(Math.pow(leg2.x-leg1.x,2)+
                Math.pow(leg2.y-leg1.y,2)); // p0->p1 (c)
        return Math.acos((p1c*p1c+p0c*p0c-p0p1*p0p1)/(2*p1c*p0c));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return Double.compare(vertex.x, x) == 0 && Double.compare(vertex.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(@NotNull Vertex o) {
        if(x > o.getX()){
            return 1;
        } else if(x < o.getX()){
            return -1;
        } else {
            if(y > o.getY()){
                return 1;
            } else if(y < o.getY()){
                return -1;
            } else {
                return 0;
            }
        }
    }
}
