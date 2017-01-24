package task1;

import java.util.Scanner;


class Simulator1 {

    private Scanner sc;
    private int trials, steps;
    private boolean stepByStep;

    private Simulator1(){
        sc = new Scanner(System.in);
        trials = 1000;
        steps = 50;
        stepByStep = false;
    }

    private void runSimulation(){
        BaselineAgent agent = new BaselineAgent();
        int totalScore = 0;
        for (int i = 1; i <= trials; i++) {
            int trialScore = runTrial(agent);
            System.out.println(String.format("%s%5d%s%4d", "Trial", i, "  avg score:", trialScore));
            totalScore += trialScore;
        }
        System.out.println(String.format("%s%.1f", "--------------------------\nTotal avg. score: ", (double)totalScore/trials));
    }

    private int runTrial(BaselineAgent agent){
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

    private int divideRoundUp(int dividend, int divisor){
        int quotient = dividend/divisor;
        if (dividend % divisor >= (divisor / 2)) quotient += 1;
        return quotient;
    }

    public static void main(String[] args) {
        Simulator1 simulator1 = new Simulator1();
        simulator1.runSimulation();
    }
}