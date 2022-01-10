package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
public class Population {
    private List<Individual> individuals;
    // send this as argument for Population constructor
    private List<Hour> hours;

    public Population(Integer populationSize, List<Hour> hours){
        while (populationSize > 0) {
            Collections.shuffle(hours);
            individuals.add(new Individual(hours));
            populationSize--;
        }
    }

    public void evaluate(){
        individuals.forEach(Individual::fitness);
    }

    public List<Integer> getFitnessForAll(){
        return individuals.stream()
                .map(Individual::getFitness)
                .collect(Collectors.toList());
    }

    public List<Individual> selection(Integer newPopulationSize){
        return individuals.stream()
                .sorted(Comparator.comparingInt(Individual::getFitness))
                .skip(individuals.size() - newPopulationSize)
                .collect(Collectors.toList());
    }
}