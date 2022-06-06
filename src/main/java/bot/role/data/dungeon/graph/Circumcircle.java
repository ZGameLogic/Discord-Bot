package bot.role.data.dungeon.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@AllArgsConstructor
@Getter
public class Circumcircle {
    private double centerX, centerY;
    private double radius;

    public Circumcircle(Vertex a, Vertex b, Vertex c){
        double sideA = a.getDistance(b);
        double sideB = b.getDistance(c);
        double sideC = c.getDistance(a);

        radius = (sideA * sideB * sideC) /
                Math.sqrt((sideA + sideB + sideC) * (sideC + sideB - sideA) * (sideC + sideA - sideB) * (sideA + sideB - sideC));
        double aA = Vertex.getAngle(b, c, a);
        double aB = Vertex.getAngle(a, c, b);
        double aC = Vertex.getAngle(a, b, c);

        double denominator = Math.sin(2 * aA) + Math.sin(2 * aB) + Math.sin(2 * aC);

        centerX = ((a.getX() * Math.sin(2 * aA) + b.getX() * Math.sin(2 * aB) + c.getX() * Math.sin(2 * aC)) /
                denominator);
        centerY = ((a.getY() * Math.sin(2 * aA) + b.getY() * Math.sin(2 * aB) + c.getY() * Math.sin(2 * aC)) /
                denominator);
    }

    public boolean containsVertext(Vertex vertex){
        return (int)(vertex.getDistance(centerX, centerY) * 10000) <= (int)(radius * 10000);
    }

    public void paintCircle(Graphics2D pane){
        Color oldColor = pane.getColor();
        pane.setColor(Color.green);
        pane.drawOval((int)((centerX - radius) * 20), (int)((centerY - radius) * 20), (int)radius * 40, (int)radius * 40);
        pane.setColor(oldColor);
    }
}
