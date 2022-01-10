package repository;

import model.Hour;
import model.Population;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PopulationRepository {
    List<Hour> hours;
    List<Population> populations;

    public PopulationRepository(){
        hours = new ArrayList<>();
        populations = new ArrayList<>();
    }

    public void readInput(String path) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        while(scanner.hasNextLine()){
            hours.add(Hour.stringToHour(scanner.nextLine()));
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
}
