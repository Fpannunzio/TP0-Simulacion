

public class Particle {

    private int id;
    private double x;
    private double y;
    private double radius;

    private double cellPositionX;
    private double cellPositionY;

    public Particle(int id, double x, double y, double radius) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }


    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getRadius() {
        return this.radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getCellPositionX() {
        return this.cellPositionX;
    }

    public void setCellPositionX(double cellPositionX) {
        this.cellPositionX = cellPositionX;
    }

    public double getCellPositionY() {
        return this.cellPositionY;
    }

    public void setCellPositionY(double cellPositionY) {
        this.cellPositionY = cellPositionY;
    }

}