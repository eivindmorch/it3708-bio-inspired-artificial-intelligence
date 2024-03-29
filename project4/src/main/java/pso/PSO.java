package pso;

import representation.JSP;
import utility.Tools;


public class PSO {

    private int gBestId = 0;
    private int pWorstId = 0;
    private PSOSolution[] pBest = new PSOSolution[Settings.numOfParticles];
    private Particle[] swarm = new Particle[Settings.numOfParticles];


    public void runAlgorithm() {
        initiateSwarm();
        while (true) {
            updateParticleVelocities();
            for (int i = 0; i < Settings.numOfParticles; i++) moveParticle(i);
            updatePBest();
            Tools.plotter.plotSolution(pBest[gBestId]);
            System.out.println(pBest[gBestId].makespan + "\t" + pBest[pWorstId].makespan);
        }
    }

    private void initiateSwarm() {
        for (int i = 0; i < Settings.numOfParticles; i++) {
            Particle particle = new Particle();
            PSOSolution solution = new PSOSolution(particle);
            swarm[i] = particle;
            pBest[i] = solution;
            if (solution.makespan < pBest[gBestId].makespan) gBestId = i;
            else if (solution.makespan > pBest[pWorstId].makespan) pWorstId = i;
        }
    }

    private void updatePBest() {
        for (int i = 0; i < swarm.length; i++) {
            PSOSolution solution = new PSOSolution(swarm[i]);
            if (solution.makespan > pBest[pWorstId].makespan) continue; // Worse than all
            if (attemptReplaceGBest(solution)) continue;                // Better than gBest
            if (attemptReplaceEqual(solution)) continue;                // Equal to pBest[i]
            if (attemptReplacePWorst(solution)) continue;               // Better than PWorst
        }
    }

    private boolean attemptReplaceGBest(PSOSolution solution) {
        if (solution.makespan < pBest[gBestId].makespan) {
            pBest[pWorstId] = pBest[gBestId];
            pBest[gBestId] = solution;
            pWorstId = findPWorstID();
            return true;
        }
        return false;
    }

    private boolean attemptReplaceEqual(PSOSolution solution) {
        for (int i = 0; i < pBest.length; i++) {
            if (solution.makespan == pBest[i].makespan){
                pBest[i] = solution;
                return true;
            }
        }
        return false;
    }

    private boolean attemptReplacePWorst(PSOSolution solution) {
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

    // TODO - Test
    private void moveParticle(int particleId) {
        Particle particle = swarm[particleId];
        for (int i = 0; i < JSP.numOfMachines; i++) {
            int l = Tools.random.nextInt(JSP.numOfJobs);
            for (int j = 0; j < JSP.numOfJobs; j++) {
                double prob = Tools.random.nextDouble();
                if (prob <= Settings.gBestFactor) {
                    particle.moveToward(i, l, pBest[gBestId]);
                }
                else if (prob - Settings.gBestFactor <= Settings.pBestFactor) {
                    particle.moveToward(i, l, pBest[particleId]);
                }
                l++;
                if (l == JSP.numOfJobs) l = 0;
            }
        }
        if (Tools.random.nextDouble() <= Settings.mutationRate) particle.mutate();
    }

    private void updateParticleVelocities() {
        for (Particle particle : swarm) {
            for (int i = 0; i < JSP.numOfMachines; i++) {
                for (int j = 0; j < JSP.numOfJobs; j++) {
                    if (Tools.random.nextDouble() >= Settings.inertiaWeight) particle.velocityMatrix[i][j] = 0;
                }
            }
        }
    }

}
