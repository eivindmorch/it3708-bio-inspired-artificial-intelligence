package aco;


public class Edge {
    public final int from, to;
    public double pheromone;


    public Edge(int from, int to) {
        this.from = from;
        this.to = to;
        this.pheromone = Settings.basePheromone;
    }

    @Override
    public String toString() {
        return "From " + from  + " to " + to + " - Pheromone: " + pheromone;
    }
}


