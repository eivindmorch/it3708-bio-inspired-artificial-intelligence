package pso;

import utility.Tools;


public class PSO {

    private int gBestId = 0;
    private int pWorstId = 0;
    Solution[] pBest = new Solution[Settings.numOfParticles];
    Particle[] swarm = new Particle[Settings.numOfParticles];


    public void runAlgorithm() {

        // Initiate swarm
        for (int i = 0; i < Settings.numOfParticles; i++) {
            Particle particle = new Particle();
            Solution solution = new Solution(particle);
            swarm[i] = particle;
            pBest[i] = solution;
            if (solution.makespan < pBest[gBestId].makespan) gBestId = i;
            else if (solution.makespan > pBest[pWorstId].makespan) pWorstId = i;
        }

        // TODO Add termination constraint
        while (true) {
            updatePBest();


            Tools.plotter.plotPSOSolution(pBest[gBestId]);
            System.out.println(pBest[gBestId].makespan + "\t" + pBest[pWorstId].makespan);
        }
    }

    private void updatePBest() {
        for (int i = 0; i < swarm.length; i++) {
            Solution solution = new Solution(swarm[i]);
            if (solution.makespan > pBest[pWorstId].makespan) continue; // Worse than all
            if (attemptReplaceGBest(solution)) continue;                // Better than gBest
            if (attemptReplaceEqual(solution)) continue;                // Equal to pBest[i]
            if (attemptReplacePWorst(solution)) continue;               // Better than PWorst
        }
    }

    private boolean attemptReplaceGBest(Solution solution) {
        if (solution.makespan < pBest[gBestId].makespan) {
            pBest[pWorstId] = pBest[gBestId];
            pBest[gBestId] = solution;
            pWorstId = findPWorstID();
            return true;
        }
        return false;
    }

    private boolean attemptReplaceEqual(Solution solution) {
        for (int i = 0; i < pBest.length; i++) {
            if (solution.makespan == pBest[i].makespan){
                pBest[i] = solution;
                return true;
            }
        }
        return false;
    }

    private boolean attemptReplacePWorst(Solution solution) {
        if (solution.makespan < pBest[pWorstId].makespan) {
            pBest[pWorstId] = solution;
            pWorstId = findPWorstID();
            return true;
        }
        return false;
    }

    private int findPWorstID() {
        int newPWorstId = 0;
        for (int i = 1; i < pBest.length; i++)
            if (pBest[i].makespan > pBest[newPWorstId].makespan) newPWorstId = i;
        return newPWorstId;
    }




}
