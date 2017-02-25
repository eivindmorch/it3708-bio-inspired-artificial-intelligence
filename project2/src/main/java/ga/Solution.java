package ga;

import representation.*;

import java.util.ArrayList;
import java.util.Random;


public class Solution {

    private Map map;
    private ArrayList<Customer>[] clustering;
    private ArrayList<ArrayList<Unit>>[] routes;
    private Random random;
    private int clusterProbExponent;

    public Solution(Map map) {

        // SETTINGS
        clusterProbExponent = -10;

        this.map = map;
        random = new Random();
        clustering = clusterCustomersToDepots();
        printClustering();
        clustering = sortClusterByCustomerDistance(clustering);
        printClustering();
        routes = calculateAllRoutes();
        printRoutes();
    }

    private ArrayList<Customer>[] clusterCustomersToDepots() {

        // Set exponential probability to be assigned to depot based on distance
        ArrayList<Customer>[] clusters = new ArrayList[map.numOfDepots];
        for (int i = 0; i < map.numOfDepots; i++) {
            clusters[i] = new ArrayList<>();
        }

        for (Customer customer : map.customers) {
            double[] depotProbArray = new double[map.numOfDepots];

            // Find distances and calculate probabilities to choose depot
            double totalDistanceToAllDepots = 0;
            for (int i = 0; i < map.numOfDepots; i++) {
                totalDistanceToAllDepots += Math.pow(map.getDistance(customer, map.depots[i]), clusterProbExponent);
            }
            for (int i = 0; i < map.numOfDepots; i++) {
                double distance = map.getDistance(customer, map.depots[i]);
                double prob = Math.pow(distance, clusterProbExponent)/totalDistanceToAllDepots;
                depotProbArray[i] = prob;
            }
            // Choose depot
            double randDouble = random.nextDouble();
            for (int i = 0; i < map.numOfDepots; i++) {
                if (depotProbArray[i] > randDouble) {
                    clusters[i].add(customer);
                    break;
                }
                randDouble -= depotProbArray[i];
            }
        }
        return clusters;
    }

    private ArrayList<Customer>[] sortClusterByCustomerDistance(ArrayList<Customer>[] clustering) {
        ArrayList<Customer>[] sortedClustering = new ArrayList[map.numOfDepots];
        for (int i = 0; i < map.numOfDepots; i++) {
            sortedClustering[i] = sortDepotCustomers(map.depots[i], clustering[i]);
        }
        return sortedClustering;
    }

    private ArrayList<Customer> sortDepotCustomers(Depot depot, ArrayList<Customer> depotCustomers) {
        ArrayList<Customer> sortedCustomers = new ArrayList<>();
        ArrayList<Customer> depotCustomersPool = new ArrayList<>(depotCustomers);
        double maxDuration = depot.maxRouteDuration;
        double maxLoad = depot.maxLoadPerVehicle;
        double duration = 0;
        double load = 0;

        Customer firstCustomer =  findClosestCustomer(depot, depotCustomersPool);
        sortedCustomers.add(firstCustomer);
        depotCustomersPool.remove(firstCustomer);

        while (depotCustomersPool.size() > 0) {
            Customer lastCustomer = sortedCustomers.get(sortedCustomers.size()-1);
            Customer closestCustomer = findClosestCustomer(lastCustomer, depotCustomersPool);
            double stepDistance = map.getDistance(lastCustomer, closestCustomer);
            double depotDistance = map.getDistance(closestCustomer, depot);
            double stepService = closestCustomer.serviceDuration;
            double stepDemand = closestCustomer.demand;
            if ((maxDuration > 0 && duration + stepDistance + depotDistance + stepService > maxDuration)
                    || load + stepDemand > maxLoad) {
                // Add while loop with moving towards depot with finding the one with shortest (stepDist + depotDist)
                closestCustomer = findClosestCustomer(depot, depotCustomersPool);
                duration = 0;
                load = 0;
            }
            sortedCustomers.add(closestCustomer);
            duration += stepDistance + stepService;
            load += stepDemand;
            depotCustomersPool.remove(closestCustomer);
        }
        return sortedCustomers;
    }

    private ArrayList<ArrayList<Unit>>[] calculateAllRoutes() {
        ArrayList<ArrayList<Unit>>[] allRoutes = new ArrayList[map.numOfDepots];
        for (int i = 0; i < map.numOfDepots; i++) {
            allRoutes[i] = calculateDepotRoutes(i);
        }
        return allRoutes;
    }

    private ArrayList<ArrayList<Unit>> calculateDepotRoutes(int depotNumber) {
        Depot depot = map.depots[depotNumber];
        ArrayList<ArrayList<Unit>> depotRoutes = new ArrayList<>();
        double maxDuration = depot.maxRouteDuration;
        double maxLoad = depot.maxLoadPerVehicle;
        double duration = 0;
        double load = 0;

        ArrayList<Customer> depotCustomers = clustering[depotNumber];
        ArrayList<Unit> route = new ArrayList<>();
        route.add(depot);

        for (int i = 0; i < depotCustomers.size(); i++) {

            Unit lastUnit = route.get(route.size()-1);
            Customer nextCustomer = depotCustomers.get(i);
            double stepDistance = map.getDistance(lastUnit, nextCustomer);
            double depotDistance = map.getDistance(nextCustomer, depot);
            double stepService = nextCustomer.serviceDuration;
            double stepDemand = nextCustomer.demand;

            if ((maxDuration > 0 && duration + stepDistance + depotDistance + stepService > maxDuration)
                    || load + stepDemand > maxLoad) {
                route.add(depot);
                depotRoutes.add(route);
                route = new ArrayList<>();
                route.add(depot);
                duration = 0;
                load = 0;
            }
            route.add(nextCustomer);
            duration += stepDistance + stepService;
            load += stepDemand;
        }
        route.add(depot);
        depotRoutes.add(route);
        return depotRoutes;
    }

    private Customer findClosestCustomer(Unit unit, ArrayList<Customer> customers) {
        Customer closestCustomer = customers.get(0);
        double shortestDistance = map.getDistance(closestCustomer, unit);

        for (int i = 1; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            double customerDistance = map.getDistance(customer, unit);
            if (customerDistance < shortestDistance) {
                closestCustomer = customer;
                shortestDistance = customerDistance;
            }
        }
        return closestCustomer;
    }

//    public ArrayList<ArrayList<Unit>>[] getRoutes() {
//        ArrayList<ArrayList<Unit>>[] routesWithDepot = new ArrayList[map.numOfDepots];
//        for (int i = 0; i < routes.length; i++) {
//            ArrayList<ArrayList<Unit>> depot = new ArrayList<>();
//            for (int j = 0; j < routes[i].size(); j++) {
//                ArrayList<Unit> route = new ArrayList<>();
//                route.add(map.depots[i]);
//                route.addAll(routes[i].get(j));
//                route.add(map.depots[i]);
//                for (int k = 0; k < route.size(); k++) {
//                    System.out.print(route.get(k) + " ");
//                }
//                System.out.println();
//                depot.add(route);
//            }
//            routesWithDepot[i] = depot;
//        }
//        return routesWithDepot;
//    }

    public ArrayList<ArrayList<Unit>>[] getRoutes() {
        return routes;
    }

    double getTotalDuration() {
        double totalDuration = map.totalServiceDuration;
        for (int i = 0; i < routes.length; i++) {
            Depot depot = map.depots[i];
            for (int j = 0; j < routes[i].size(); j++) {
                totalDuration += getRouteDuration(depot, routes[i].get(j));
            }
        }
        return totalDuration;
    }

    double getRouteDuration(Depot depot, ArrayList<Unit> route) {
        double routeDuration = map.getDistance(depot, route.get(0));
        for (int k = 0; k < route.size()-1; k++) {
            routeDuration += map.getDistance(route.get(k), route.get(k+1));
        }
        routeDuration += map.getDistance(route.get(route.size()-1), depot);
        return routeDuration;
    }

    private void printClustering() {
        for (int i = 0; i < clustering.length; i++) {
            for (int j = 0; j < clustering[i].size(); j++) {
                System.out.print(clustering[i].get(j) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private void printRoutes() {
        for (int i = 0; i < routes.length; i++) {
            for (int j = 0; j < routes[i].size(); j++) {
                System.out.print(routes[i].get(j) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

}

