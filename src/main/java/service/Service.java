package service;

import model.Individual;
import model.Population;
// import mpi.MPI;
import repository.PopulationRepository;

import javax.management.MBeanParameterInfo;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Service {
    private PopulationRepository populationRepository;
    private Integer populationSize = 256;
    private Float mutationProbability = 0.01F;
    private Float crossoverProbability = 0.8F;
    private List<Integer> fitnessOfPopulation;
    private Population population;

    public Service(){
        populationRepository = new PopulationRepository();
    }

    private Individual iteration(){
        population.evaluate();
        List<Individual> parents = population.selection(populationSize*3/4);
        List<Individual> children = new ArrayList<>();

        // parents.get(0).toString();

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

    private Individual iterationThreadPool(){
        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Runnable> runnables = new ArrayList<>();
        List<Population> populationParts = new ArrayList<>();
        for(int i = 0; i < populationSize; i += populationSize/4){
            Population population = new Population();
            population.setIndividuals(this.population.getIndividuals().subList(i, i + populationSize/4));
            populationParts.add(population);
            runnables.add(() -> {
                population.evaluate();
                List<Individual> parents = population.selection(populationSize/8);
                List<Individual> children = new ArrayList<>();
                for(int j = 0; j < parents.size(); j+=2){
                    List<Individual> childs = parents.get(j).crossover(parents.get(j + 1), crossoverProbability);
                    if(childs != null) children.addAll(childs);
                }

                children.forEach(c -> c.mutate(mutationProbability));
                parents.addAll(children);
                population.setIndividuals(parents);
                population.evaluate();
                population.setIndividuals(population.selection(populationSize/4));
            });
        }
        for(Runnable runnable: runnables)
            pool.execute(runnable);

        pool.shutdown();
        while (!pool.isTerminated()){}

        List<Individual> gatheredIndividuals = new ArrayList<>();
        populationParts.forEach(p -> gatheredIndividuals.addAll(p.getIndividuals()));
        population.setIndividuals(gatheredIndividuals);
        return population.selection(1).get(0);
    }

    public Individual solver(Integer noOfGenerations){
        Individual bestIndividual = null;
        Integer bestFitness = Integer.MIN_VALUE;
        for(int i = 0; i < noOfGenerations; i++){
            Individual localBestIndividual = iteration();
            if(localBestIndividual.getFitness() >= bestFitness){
                bestIndividual = localBestIndividual;
                bestFitness = localBestIndividual.getFitness();
            }
            System.out.println(population.getIndividuals().size() + " " + localBestIndividual.getFitness());
        }
        return bestIndividual;
    }

    public void run(String path) throws FileNotFoundException {
        populationRepository.readInput(path);
        population = populationRepository.createPopulation(populationSize);
        Individual result = solver(100);
        result.toString();
        System.out.println(result.getFitness());
    }
}
