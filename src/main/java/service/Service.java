package service;

import repository.PopulationRepository;

import java.util.List;

public class Service {
    private PopulationRepository populationRepository;
    private Integer populationSize;
    private Float mutationProbability;
    private Float crossoverProbability;
    private List<Integer> fitnessOfPopulation;
}
