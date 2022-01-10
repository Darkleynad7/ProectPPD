package service;

import model.Individual;
import model.Population;
import repository.PopulationRepository;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Service {
    private PopulationRepository populationRepository;
    private Integer populationSize;
    private Float mutationProbability;
    private Float crossoverProbability;
    private List<Integer> fitnessOfPopulation;
    private Population population;

    public Service(){
        populationRepository = new PopulationRepository();
        population = populationRepository.createPopulation(populationSize);
    }

    private Individual iteration(){
        population.evaluate();
        List<Individual> parents = population.selection(populationSize/2);
        List<Individual> children = new ArrayList<>();
        for(int i = 0; i < parents.size(); i+=2){
            List<Individual> childs = parents.get(i).crossover(parents.get(i + 1), crossoverProbability);
            if(childs != null) children.addAll(childs);
        }

        children.forEach(c -> c.mutate(mutationProbability));
        parents.addAll(children);
        population.setIndividuals(parents);
        population.evaluate();
        population.setIndividuals(population.selection(populationSize));

        //returns no1 individual
        return population.selection(1).get(0);
    }

    public Individual solver(Integer noOfGenerations){
        Individual bestIndividual = null;
        Integer bestFitness = 0;
        for(int i = 0; i < noOfGenerations; i++){
            Individual localBestIndividual = iteration();
            if(localBestIndividual.getFitness() > bestFitness){
                bestIndividual = localBestIndividual;
                bestFitness = localBestIndividual.getFitness();
            }
        }
        return bestIndividual;
    }

    public void run(String path) throws FileNotFoundException {
        populationRepository.readInput(path);
        populationRepository.getHours().forEach(System.out::println);
    }
}
