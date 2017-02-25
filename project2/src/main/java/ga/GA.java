package ga;

import representation.*;
import tools.*;
import java.io.IOException;
import java.util.ArrayList;


public class GA {

    private String mapName;
    private int popSize, iterations;

    public GA() {
        // SETTINGS
        mapName = "p02";
        popSize = 1000;
        iterations = 1000;

    }

    private void runAlgorithm() throws IOException {
        Map map = DataReader.readMapData(mapName);

        ArrayList<Solution> population = new ArrayList<>();
        // Initial population
        for (int i = 0; i < popSize; i++) {
            population.add(new Solution(map));
        }
        Plotter plotter = new Plotter(map);


        Solution bestSolution = population.get(0);
        // Evolution
        for (int i = 0; i < iterations; i++) {
            bestSolution = findBestSolution(population);
            System.out.println(bestSolution.getTotalDuration());
            population = clonePopulation(bestSolution);
        }
        bestSolution.printRoutes();
        plotter.plotSolution(bestSolution);
        System.out.println(bestSolution.getTotalDuration());
    }

    private Solution findBestSolution(ArrayList<Solution> population) {
        Solution bestSolution = population.get(0);
        double bestSolutionDist = bestSolution.getTotalDuration();
        for (int i = 1; i < population.size(); i++) {
            if (population.get(i).getTotalDuration() < bestSolutionDist) {
                bestSolution = population.get(i);
                bestSolutionDist = population.get(i).getTotalDuration();
            }
        }
        return bestSolution;
    }

    private ArrayList<Solution> clonePopulation(Solution solution) {
        ArrayList<Solution> clonedPopulation = new ArrayList<>();
        clonedPopulation.add(new Solution(solution, false));
        for (int i = 1; i < popSize; i++) {
            clonedPopulation.add(new Solution(solution, true));
        }
        return clonedPopulation;
    }

    public static void main(String[] args) throws IOException {
        GA ga = new GA();
        ga.runAlgorithm();
    }
}


