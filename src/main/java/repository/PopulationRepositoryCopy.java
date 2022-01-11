package repository;

import model.Hour;
import model.Population;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PopulationRepositoryCopy {
    List<Hour> hours;
    List<Population> populations;

    public PopulationRepositoryCopy(String path){
        hours = new ArrayList<>();
        populations = new ArrayList<>();
        try {
            readInput(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void readInput(String path) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        while(scanner.hasNextLine()){
            String[] parts = scanner.nextLine().split(" - ");
            for(int i = 0; i < Integer.parseInt(parts[3]); i++)
                hours.add(Hour.stringToHour(parts));
        }
    }

    public List<Hour> getHours() {
        return hours;
    }

    public List<Population> getPopulations() {
        return populations;
    }

    public Population createPopulation(Integer populationSize){
        Population population = new Population(populationSize, hours);
        populations.add(population);
        return population;
    }

    public List<Population> split(Population population, Integer populationSize) {
        List<Population> populations = new ArrayList<>();
        for(int i = 0; i < populationSize; i+= populationSize/4){
            Population lPop = new Population(populationSize/4, hours);
            lPop.setIndividuals(population.getIndividuals().subList(i, i + populationSize/4));
            populations.add(lPop);
        }
        return populations;
    }
}
