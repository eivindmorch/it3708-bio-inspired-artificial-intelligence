package ba;

import representation.JSP;
import representation.Operation;
import representation.Solution;


public class BASolution extends Solution implements Comparable<BASolution>{

    int[] foodSource;
    double neighbourhoodSize;
    int roundsWithoutImprovement;

    BASolution(int[] foodSource) {
        this.foodSource = foodSource;
        this.neighbourhoodSize = Settings.initialNeighbourhoodSize;
        this.roundsWithoutImprovement = 0;
        this.makespan = 0;
        double[] jobEndTime = new double[JSP.numOfJobs];
        double[] machineEndTime = new double[JSP.numOfMachines];
        operationStartTimes = new double[JSP.numOfJobs][JSP.numOfMachines];
        int[] jobCount = new int[JSP.numOfJobs];

        for (int i = 0; i < foodSource.length; i++) {
            int job = foodSource[i];
//            Operation op = JSP.getOperation(job * JSP.numOfMachines + jobCount[job]);
            Operation op = JSP.jobs[job][jobCount[job]];
            jobCount[job]++;
            double maxStartTime = Math.max(jobEndTime[op.job], machineEndTime[op.machine]);
            operationStartTimes[op.job][op.jobOpIndex] = maxStartTime;
            jobEndTime[op.job] = maxStartTime + op.duration;
            machineEndTime[op.machine] = maxStartTime + op.duration;
            if (maxStartTime + op.duration > makespan) makespan = maxStartTime + op.duration;
        }
    }

//    BA2Solution(BA2Solution solution) {
//        this.foodSource = solution.foodSource.clone();
//        this.neighbourhoodSize = solution.neighbourhoodSize;
//        this.roundsWithoutImprovement = solution.roundsWithoutImprovement;
//        this.makespan = solution.makespan;
//        this.operationStartTimes = solution.operationStartTimes.clone();
//    }

    @Override
    public int compareTo(BASolution o) {
        if (this.makespan < o.makespan) return -1;
        if (this.makespan > o.makespan) return 1;
        return 0;
    }

    @Override
    public String toString() {
//        return Arrays.toString(foodSource);
        return Double.toString(makespan);
    }
}
