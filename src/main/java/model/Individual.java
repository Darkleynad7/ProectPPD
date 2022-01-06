package model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class Individual {
    private List<Gene> geneList;
    private Integer fitness;

    Individual(List<Hour> hours){
    }

    public void fitness(){
        fitness = 0;
    }

    public void mutate(Float mutationProbability){

    }

    public List<Individual> crossover(Individual other, Float crossoverProbability){
        return new ArrayList<>();
    }
}
