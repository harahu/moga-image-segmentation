package moga;

public class EdgeCost implements Comparable<EdgeCost> {
    private int a;
    private int b;
    private int cost;

    public EdgeCost(int a, int b, int cost) {
        this.a = a;
        this.b = b;
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    @Override
    public int compareTo(EdgeCost ec) {
        if(this.cost < ec.getCost()) return -1;
        else if(this.cost > ec.getCost()) return 1;
        return 0;
    }

    @Override
    public String toString() {
        return "(" + Integer.toString(a) + ", " + Integer.toString(b) + ", " + Integer.toString(cost) + ")";
    }
}
