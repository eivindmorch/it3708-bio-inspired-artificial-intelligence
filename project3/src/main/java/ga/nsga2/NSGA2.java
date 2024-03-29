package ga.nsga2;

import ga.Settings;
import utility.ImageWriter;
import utility.Tools;
import java.util.ArrayList;
import java.util.HashSet;


public class NSGA2 {

    ArrayList<ArrayList<NSGA2Chromosome>> rankedPopulation = new ArrayList<>();
    ArrayList<NSGA2Chromosome> population;

    public void runAlgorithm() {

        // INITIALISE
        if (Settings.initPop == Settings.InitialisePopulation.HEAVY) createInitialPopulation1();
        else if (Settings.initPop == Settings.InitialisePopulation.LIGHT) createInitialPopulation2();
        for (NSGA2Chromosome chromosome : population) chromosome.calculateCost();
        rankPopulationByNonDomination();
        selectNewPopulationFromRankedPopulation();
        int generation = 1;

        // GENERATION
        while (true) {
            outputStuff(generation);
            ArrayList<NSGA2Chromosome> offspring = createOffspringPopulation();
            for (NSGA2Chromosome chromosome : offspring) {
                chromosome.mergeSegmentsSmallerThanK(Settings.minimumSegmentationSize);
                chromosome.calculateCost();
            }
            population.addAll(offspring);
            rankPopulationByNonDomination();
            selectNewPopulationFromRankedPopulation();

            generation++;
        }
    }

    private void createInitialPopulation2() {
        NSGA2Chromosome mstChromosome = new NSGA2Chromosome();
        population = new ArrayList<>(Settings.populationSize * 2);
        for (int i = 0; i < Settings.populationSize * 2; i++) {
            NSGA2Chromosome chromosome = new NSGA2Chromosome(mstChromosome);
            chromosome.removeKRandomEdges(Tools.random.nextInt(100));
            chromosome.mergeSegmentsSmallerThanK(Settings.minimumSegmentationSize);
            population.add(chromosome);
        }
    }

    private void createInitialPopulation1() {
        NSGA2Chromosome mstChromosome = new NSGA2Chromosome();
        population = new ArrayList<>(Settings.populationSize * 2);
        NSGA2Chromosome cloneChromosome = new NSGA2Chromosome(mstChromosome);
        for (int i = 0; i < Settings.populationSize; i++) {
            if (i % (Settings.populationSize / 10) == 0) {
                System.out.println();
                mstChromosome.removeKLargestEdges(2000);
                mstChromosome.calculateSegmentation();
                cloneChromosome = new NSGA2Chromosome(mstChromosome);
                cloneChromosome.mergeSegmentsSmallerThanK(1000);
            }
            System.out.print(i + " ");
            NSGA2Chromosome chromosome = new NSGA2Chromosome(cloneChromosome);
            chromosome.removeKRandomEdges(Tools.random.nextInt(10)); // TODO Merge her?
            chromosome.mergeSegmentsSmallerThanK(1000 + Tools.random.nextInt(1000));
//            ImageWriter.writeChromosomeImageRandomRgb(chromosome, i);
            population.add(chromosome);
        }
        System.out.println();
    }

    // TODO Calculate score before this
    private ArrayList<NSGA2Chromosome> createOffspringPopulation() {
        ArrayList<NSGA2Chromosome> offspringPopulation = new ArrayList<>(Settings.populationSize);
        for (int i = 0; i < Settings.populationSize; i++) {
            NSGA2Chromosome p1 = binaryTournament(population);
            NSGA2Chromosome p2 = binaryTournament(population);
            offspringPopulation.add(new NSGA2Chromosome(p1, p2));
        }
        return offspringPopulation;
    }

    private NSGA2Chromosome binaryTournament(ArrayList<NSGA2Chromosome> population) {
        NSGA2Chromosome c1 = population.get(Tools.random.nextInt(population.size()));
        NSGA2Chromosome c2 = population.get(Tools.random.nextInt(population.size()));
//        if (c1.compare(c2) < 0) return c1;
        // TODO Sjekk at denne blir riktig
        if (NSGA2Chromosome.nonDominationRankAndCrowdingDistanceComparator().compare(c1, c2) < 0 ) return c1;
        else return c2;
    }

//    private void rankPopulationByNonDomination() {
//
//        rankedPopulation.clear();
//
//        ArrayList<NSGA2Chromosome> rank = new ArrayList<>();
//        for (NSGA2Chromosome c1 : population) {
//            c1.dominates = new HashSet<>();
//            c1.numOfDominators = 0;
//            for (NSGA2Chromosome c2 : population) {
//                // TODO loop diagonal and check both ways
//                if (c1.dominates(c2)) c1.dominates.add(c2);
//                else if (c2.dominates(c1)) c1.numOfDominators++;
//            }
//            if (c1.numOfDominators == 0) {
//                c1.rank = 0;
//                rank.add(c1);
//            }
//        }
//        rankedPopulation.add(rank);
//
//        int rankNum = 0;
//        while (!rank.isEmpty()) {
//            ArrayList<NSGA2Chromosome> newRank = new ArrayList<>();
//            for (NSGA2Chromosome chromosome : rank) {
//                for (NSGA2Chromosome dominated : chromosome.dominates) {
//                    dominated.numOfDominators--;
//                    if (dominated.numOfDominators == 0) {
//                        dominated.rank = rankNum + 1;
//                        newRank.add(dominated);
//                    }
//                }
//            }
//            rankNum++;
//            if (!newRank.isEmpty())rankedPopulation.add(newRank);
//            rank = newRank;
//        }
//    }

    private void rankPopulationByNonDomination() {

        // Generate numOfDominators and domnates set
        for (NSGA2Chromosome c1 : population) {
            c1.dominates = new HashSet<>();
            c1.numOfDominators = 0;
            for (NSGA2Chromosome c2 : population) {
                if (c1.dominates(c2)) c1.dominates.add(c2);
                else if (c2.dominates(c1)) c1.numOfDominators++;
            }
        }
//        for (NSGA2Chromosome chromosome : population)
//            System.out.print(chromosome.numOfDominators + " ");
//        System.out.println();

        // Add to ranks depending on domination relationships
        rankedPopulation.clear();
        int totalRanked = 0;
        while (totalRanked < Settings.populationSize) {
            ArrayList<NSGA2Chromosome> rank = new ArrayList<>();
            int i = 0;
            while (i < population.size()){
                if(population.get(i).numOfDominators == 0){
                    NSGA2Chromosome chromosome = population.remove(i);
                    chromosome.rank = rankedPopulation.size();
                    rank.add(chromosome);
                }
                else i++;
            }
            // TODO Change loop to init first to not do this for last unecessary
            for (NSGA2Chromosome dominator : rank)
                for (NSGA2Chromosome dominated : dominator.dominates)
                    dominated.numOfDominators--;
            rankedPopulation.add(rank);
            totalRanked += rank.size();
        }
        System.out.println("Rank0 size: " + rankedPopulation.get(0).size());
    }


    private void selectNewPopulationFromRankedPopulation() {
        population.clear();
        for (ArrayList<NSGA2Chromosome> rank : rankedPopulation) {
            assignCrowdingDistance(rank);
            if (rank.size() <= Settings.populationSize - population.size()) {
                population.addAll(rank);
            }
            else {
                ArrayList<NSGA2Chromosome> rankCopy = new ArrayList<>(rank);
                if (Settings.useTournamentForSurvivalSelection) {
                    while (population.size() < Settings.populationSize) {
                        NSGA2Chromosome winner = binaryTournament(rankCopy);
                        rankCopy.remove(winner);
                        population.add(winner);
                    }
                }
                else {
                    System.out.print("Sorting...");
                    rankCopy.sort(NSGA2Chromosome.crowdingDistanceComparator());
                    System.out.println("done");
                    System.out.print("Collecting best...");
                    while (population.size() < Settings.populationSize) {
                        population.add(rankCopy.remove(0));
                    }
                    System.out.println("done");
                }
                return;
            }
        }
    }

    private void assignCrowdingDistance(ArrayList<NSGA2Chromosome> rank) {
        for (NSGA2Chromosome chromosome : rank) chromosome.crowdingDistance = 0;
        if (Settings.useOverallDeviation) assignCrowdingDistancePerObjective(rank, 0);
        if (Settings.useEdgeValue) assignCrowdingDistancePerObjective(rank, 1);
        if (Settings.useConnectivity) assignCrowdingDistancePerObjective(rank, 2);
    }

    private void assignCrowdingDistancePerObjective(ArrayList<NSGA2Chromosome> rank, int index) {
        if (index == 0) rank.sort(NSGA2Chromosome.overallDeviationComparator());
        else if (index == 1) rank.sort(NSGA2Chromosome.edgeValueComparator());
        else if (index == 2) rank.sort(NSGA2Chromosome.connectivityComparator());

        rank.get(0).crowdingDistance = Double.POSITIVE_INFINITY;
        rank.get(rank.size() - 1).crowdingDistance = Double.POSITIVE_INFINITY;
        double span = Math.abs(rank.get(0).cost[index] - rank.get(rank.size() - 1).cost[index]);
        for (int i = 1; i < rank.size() - 1; i++) {
            rank.get(i).crowdingDistance += Math.abs(rank.get(i-1).cost[index] - rank.get(i+1).cost[index]) / span;
        }
    }

    private void outputStuff(int generation) {
        System.out.println("\nGeneration: " + generation);
        System.out.println("Population: " + population.size());
//            for (NSGA2Chromosome chromosome : rankedPopulation.get(0)) {
//                System.out.print(String.format("%10f\t%10f\t%10f\t\t\t\n", chromosome.cost[0], chromosome.cost[1], chromosome.cost[2]));
//            }
//            System.out.println();

        if (generation % Settings.generationsPerPause == 0) {
            ArrayList<NSGA2Chromosome> printRank = new ArrayList<>(rankedPopulation.get(0));
            if (Settings.printOnlyFive) {
                printRank = new ArrayList<>(5);
                printRank.add(rankedPopulation.get(0).get(0));
                printRank.add(rankedPopulation.get(0).get(rankedPopulation.get(0).size() -1));
                for (int i = 1; i < 4; i++) {
                    printRank.add(rankedPopulation.get(0).get(i*(rankedPopulation.get(0).size()/4)-1));
                }
            }
            Tools.printObjectiveValues(printRank);
            ImageWriter.writeAllNSGA2Chromosomes(rankedPopulation.get(0));
            if (Settings.openImagesToWindow) ImageWriter.openAllChromosomesInWindow(printRank);
            Tools.plotter.plotFront(rankedPopulation.get(0));
        }
    }


}
