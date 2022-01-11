import model.Individual;
import model.Population;
import mpi.MPI;
import repository.PopulationRepositoryCopy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MPIMain {
    private static final PopulationRepositoryCopy populationRepository = new PopulationRepositoryCopy("src/main/resources/input.in");
    private static final Integer populationSize = 256;
    private static final Float mutationProbability = 0.04F;
    private static final Float crossoverProbability = 0.8F;
    private static Integer noOfGenerations = 256;
    private static final Population population = populationRepository.createPopulation(populationSize);
    private static Individual bestIndividual;
    private static Integer bestFitness = Integer.MIN_VALUE;
    private static Date start;
    private static Date finish;
    public static void main(String[] args) {

        MPI.Init(args);
        String[] individuals = population.getIndividuals().stream().map(Individual::modelToString).toArray(String[]::new);

        if(MPI.COMM_WORLD.Rank() == 0)start = new Date();

        String[] receivedIndividuals = new String[populationSize/4];
        while(noOfGenerations > 0)
        {

            MPI.COMM_WORLD.Scatter(individuals, 0, populationSize/4, MPI.OBJECT,
                    receivedIndividuals, 0, populationSize/4, MPI.OBJECT, 0);

            List<Individual> localIndividuals = Arrays.stream(receivedIndividuals).map(Individual::stringToModel).collect(Collectors.toList());


            Population localPopulation = new Population(populationSize/4, populationRepository.getHours());
            localPopulation.setIndividuals(localIndividuals);

            localPopulation.evaluate();
            List<Individual> parents = localPopulation.selection(populationSize/4 * 3/4);
            List<Individual> children = new ArrayList<>();
            for(int i = 0; i < parents.size(); i+=2){
                List<Individual> childs = parents.get(i).crossover(parents.get(i + 1), crossoverProbability);
                if(childs != null) children.addAll(childs);
            }

            children.forEach(c -> c.mutate(mutationProbability));
            parents.addAll(children);
            localPopulation.setIndividuals(parents);
            localPopulation.evaluate();
            localPopulation.setIndividuals(localPopulation.selection(populationSize/4));

            receivedIndividuals = localPopulation.getIndividuals().stream().map(Individual::modelToString).toArray(String[]::new);

            MPI.COMM_WORLD.Gather(receivedIndividuals, 0, populationSize/4, MPI.OBJECT,
                    individuals, 0, populationSize/4, MPI.OBJECT, 0);


            noOfGenerations--;

            if(MPI.COMM_WORLD.Rank() == 0){
                List<Individual> gatheredIndividuals = Arrays.stream(individuals).map(Individual::stringToModel).collect(Collectors.toList());
                population.setIndividuals(gatheredIndividuals);
                Individual localBest = population.selection(1).get(0);
                if(localBest.getFitness() >= bestFitness){
                    bestIndividual = localBest;
                    bestFitness = localBest.getFitness();
                }
                System.out.println(localBest.getFitness());

                individuals = population.getIndividuals().stream().map(Individual::modelToString).toArray(String[]::new);
            }
        }

        if(MPI.COMM_WORLD.Rank() == 0){
            finish = new Date();
            System.out.println("Time = " + (finish.getTime() - start.getTime()));
        }

        MPI.Finalize();
    }
}
