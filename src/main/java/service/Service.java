package service;

import model.DayType;
import model.Individual;
import model.Population;
// import mpi.MPI;
import repository.PopulationRepository;

import javax.management.MBeanParameterInfo;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
* Algorighm
* We have implemented an evolutionary algorithm for determining a desirable
* timetable for multiple groups of students.
*
* The input for this algorithm is a set of classes which contain
* information about the group, teacher, subject and starting hour
*
* The initial population will be randomly generated in the following way:
*    - take the list of classes
*    - shuffle it
*    - split it into multiple parts for each day of the week
*    - assign hours starting from 8 independently for each of the groups
*    - individual is created from those classes
*
* The way we mutate an individual is by swapping two classes from different
* days.
*
* An individual is a potential solution to the problem, meaning a complete
* timetable.
*
* The genes are a set of classes, for multiple groups and independent of each other
*
* The selection process is based on a fitness function whose value is based on a few
* characteristics: how many gaps there are for each group of students,
* all groups starting at 8, overlapping classes for both students and teachers
*
* We run multiple generations of the population, during each one of them
* we select the best set of individuals based on the value of their fitness function
*
* The input is read for a file
*
* No synchronization was needed due to working on decoupled chunks of data
*
*
* */


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
                List<Individual> parents = population.selection(populationSize/4*3/4);
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
        Date start = new Date();
        Individual result = solver(64);
        Date finish = new Date();
        result.toString();
        System.out.println(result.getFitness());
        System.out.println("Time = " + (finish.getTime() - start.getTime()));
    }
}
