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

        createInitialPopulation();
        for (NSGA2Chromosome chromosome : population) {
            chromosome.calculateCost();
        }
        rankPopulationByNonDomination();
        selectNewPopulationFromRankedPopulation();

        int generation = 1;
        while (true) {
            System.out.println(generation);
            System.out.println(population.size());
            System.out.println(rankedPopulation.get(0).size());

            System.out.println();
//            for (NSGA2Chromosome chromosome : rankedPopulation.get(0)) {
//                System.out.print(String.format("%10f\t%10f\t%10f\t\t\t\n", chromosome.cost[0], chromosome.cost[1], chromosome.cost[2]));
//            }
            System.out.println();

            if (generation % Settings.generationsPerPause == 0) {
                Tools.plotter.plotFront((rankedPopulation.get(0)));
                Tools.printPause(generation, rankedPopulation.get(0).get(rankedPopulation.get(0).size()/2));
            }
            population.addAll(createOffspringPopulation());
            for (NSGA2Chromosome chromosome : population) {
                chromosome.calculateCost();
            }
            rankPopulationByNonDomination();
            selectNewPopulationFromRankedPopulation();
            
            generation++;
        }

    }

    private void createInitialPopulation() {
        NSGA2Chromosome mstChromosome = new NSGA2Chromosome();
        population = new ArrayList<>(Settings.populationSize * 2);
        for (int i = 0; i < Settings.populationSize; i++) {
            NSGA2Chromosome chromosome = new NSGA2Chromosome(mstChromosome);
            chromosome.removeKRandomEdges(100);
            population.add(chromosome);
        }
        ImageWriter.writeChromosomeImageRandomRgb(population.get(0), 0);
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

    private void rankPopulationByNonDomination() {
        // Generate numOfDominators and domnates set
        for (NSGA2Chromosome c1 : population) {
            c1.dominates = new HashSet<>();
            c1.numOfDominators = 0;
            for (NSGA2Chromosome c2 : population) {
                // TODO loop diagonal and check both ways
                if (c1.dominates(c2)) c1.dominates.add(c2);
                else if (c2.dominates(c1)) c1.numOfDominators++;
            }
        }
        for (NSGA2Chromosome chromosome : population) {
            System.out.print(chromosome.numOfDominators + " ");
        }
        System.out.println();
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

        for (ArrayList<NSGA2Chromosome> ch : rankedPopulation){
            System.out.println(ch);
            System.out.println(ch.size());
            System.out.println();
        }
    }


    private void selectNewPopulationFromRankedPopulation() {
        population.clear();
        for (ArrayList<NSGA2Chromosome> rank : rankedPopulation) {
            if (rank.size() <= Settings.populationSize - population.size()) {
                assignCrowdingDistance(rank);
                population.addAll(rank);
            }
            else {
                assignCrowdingDistance(rank);
                while (population.size() < Settings.populationSize) population.add(binaryTournament(rank));
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
        if (index == 0) rank.sort(NSGA2Chromosome.overallDistanceComparator());
        else if (index == 1) rank.sort(NSGA2Chromosome.edgeValueComparator());
        else if (index == 2) rank.sort(NSGA2Chromosome.connectivityComparator());

        rank.get(0).crowdingDistance = Double.POSITIVE_INFINITY;
        rank.get(rank.size() - 1).crowdingDistance = Double.POSITIVE_INFINITY;
        double span = Math.abs(rank.get(0).cost[index] - rank.get(rank.size() - 1).cost[index]);
        for (int i = 1; i < rank.size() - 1; i++) {
            rank.get(i).crowdingDistance += Math.abs(rank.get(i-1).cost[index] - rank.get(i+1).cost[index]) / span;
        }
    }


}
