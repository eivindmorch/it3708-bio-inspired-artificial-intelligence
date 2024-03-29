package ga;


public class Settings {


    // GA
    static String mapName = "p04";
    static int maxIterations = 100000;
    static int iterationsPerPause = 500;
    static int popSize = 100;
    static double elitePercent = 3;
    static int tournamentSize = 2;
    static double crossoverRate = 0.8;

    static int popAlgorithm = 2;    // 1=light, 2=heavy


    // Solution
    static int clusterProbExponent = -10;
    static double mutationRate = 0.8;
    static boolean forceNumOfVehicles = true;

    static double distanceCostWeight = 1;
    static double numOfVehiclesCostWeight = 0.1;
    static double overVehicleLimitCostWeight = 10000;
    static double overDurationLimitCostWeight = 5000;
    static double overLoadLimitCostWeight = 1000000;



    // 1. CHANGE CROSSOVER IN Solution
    // 2. CHANGE MUTATION WEIGHTS IN Solution (inter/intra depot)

}
