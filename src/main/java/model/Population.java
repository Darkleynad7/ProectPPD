package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Population {
    private List<Individual> individuals;

    public Population(Integer populationSize, List<Hour> hours){

    }

    public void evaluate(){
        individuals.forEach(Individual::fitness);
    }

    public List<Integer> getFitnessForAll(){
        return individuals.stream()
                .map(Individual::getFitness)
                .collect(Collectors.toList());
    }

    public void selection(Integer newPopulationSize){
        individuals = individuals.stream()
                .sorted(Comparator.comparingInt(Individual::getFitness))
                .skip(individuals.size() - newPopulationSize)
                .collect(Collectors.toList());
    }
}
