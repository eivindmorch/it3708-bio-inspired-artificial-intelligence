package task4;

import task1.World;

import java.util.Scanner;


class Simulator4 {

    private Scanner sc;
    private int trials, trainingRounds, steps;
    private boolean stepByStep;

    private Simulator4(){
        sc = new Scanner(System.in);
        trainingRounds = 100;
        trials = 100;
        steps = 50;
        stepByStep = false;
    }

    private void runSimulation(){
        ExtendedReinforcedNeuralAgent agent = new ExtendedReinforcedNeuralAgent();
        double totalScore = 0;
        for (int i = 1; i <= trainingRounds; i++) {
            double roundAvgScore = runTrainingRound(agent);
            System.out.println(String.format("%s%5d%s%6.1f", "Training round", i, "  avg score:", roundAvgScore));
            totalScore += roundAvgScore;
        }
        System.out.println(String.format("%s%.1f", "--------------------------\nTotal avg. score: ", totalScore/trainingRounds));
    }

    private double runTrainingRound(ExtendedReinforcedNeuralAgent agent){
        double roundScore = 0;
        for (int i = 1; i <= trials; i++) {
            int trialScore = runTrial(agent);
            roundScore += trialScore;
        }
        return roundScore/trials;
    }

    private int runTrial(ExtendedReinforcedNeuralAgent agent){
        World world = new World();
        agent.registerNewWorld(world);
        world.placeAgentRandom();
        if (stepByStep) {
            System.out.println("Initial world:");
            printGrid(world.getGrid());
            System.out.println();
        }
        int step = 1;
        while(!world.simulationEnd && step <= steps) {
            if (stepByStep) {
                sc.nextLine();
                agent.step();
                printGrid(world.getGrid());
                System.out.println("Step " + step + " score: " + agent.getScore() + "\n");
            }
            else agent.step();
            step++;
        }
        return agent.getScore();
    }

    private void printGrid(char[][] grid){
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    private int divideToIntRoundUp(double dividend, int divisor){
        int quotient = (int)dividend/divisor;
        if (dividend % divisor > (divisor / 2)) quotient += 1;
        return quotient;
    }

    public static void main(String[] args) {
        Simulator4 simulator4 = new Simulator4();
        simulator4.runSimulation();
    }
}