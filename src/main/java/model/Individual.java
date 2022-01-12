package model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import lombok.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Individual{
    private List<Gene> geneList;
    private Integer fitness;


    // HELPER FUNCTIONS

    BiFunction<Stream<Hour>, Integer, Stream<List<Hour>>> split = (ss, k) ->
            StreamSupport.stream(Iterables.partition(ss.collect(Collectors.toList()), k).spliterator(), false);

    Function<Gene, Stream<List<Hour>>> hours4groups = g ->
            g.getHourList().stream().collect(Collectors.groupingBy(h -> h.getGroup().getIdentifier())).values().stream();

    Function<Gene, Stream<List<Hour>>> hours4teachers = g ->
            g.getHourList().stream().collect(Collectors.groupingBy(h -> h.getProfessor().getName())).values().stream();

    Predicate<List<Hour>> hasDups = hs ->
            hs.stream()
                    .map(Hour::getStartingHour)
                    .filter(i -> Collections.frequency(hs, i) > 1)
                    .collect(Collectors.toSet()).size() > 0;

    Function<List<Hour>, Stream<Integer>> getIntervalBetweenMinAndMax = hs ->
            Stream.iterate
                    (hs.stream().map(Hour::getStartingHour).min(Comparator.comparingInt(i -> i)).orElse(0)
                            , h -> h <= hs.stream().map(Hour::getStartingHour).max(Comparator.comparingInt(i -> i)).orElse(0)
                            , h -> h + 1);

    BiFunction<Gene, Function<Gene, Stream<List<Hour>>>, Integer> gaps = (g, f) ->
            f.apply(g).map(hs -> Sets.difference
                                    (getIntervalBetweenMinAndMax.apply(hs).collect(Collectors.toSet())
                                            , new HashSet<>(hs))
                            .size())
                    .reduce(0, Integer::sum);

    Function<Gene, Integer> gaps4students = g -> gaps.apply(g, hours4groups);

    Function<Gene, Integer> gaps4teachers = g -> gaps.apply(g, hours4teachers);



    // PREDICATES

    Predicate<Gene> allGroupsStartAt8 = g ->
            hours4groups.apply(g).allMatch(hs ->
                    hs.stream()
                            .map(Hour::getStartingHour)
                            .min(Comparator.comparingInt(a -> a))
                            .map(h -> h == 8)
                            .orElse(false));

    Predicate<Gene> overlappingClasses4groups = g ->
            hours4groups.apply(g).anyMatch(hs -> hasDups.test(hs));

    Predicate<Gene> overlappingClasses4teachers = g ->
            hours4teachers.apply(g).anyMatch(hs -> hasDups.test(hs));



    // CONSTRUCTOR

    Individual(List<Hour> hss) {
        geneList = Streams.zip
                        (split.apply(hss.stream(), hss.size() / 5)
                                , Stream.of(DayType.MONDAY, DayType.TUESDAY, DayType.WEDNESDAY, DayType.THURSDAY, DayType.FRIDAY)
                                , Gene::new
                        )
                .map(g -> assignStartingHours.apply(g)
                ).collect(Collectors.toList());
    }


    public Individual(List<Gene> genes, Integer fitness) {
        geneList = genes;
        this.fitness = fitness;
    }

    Function<Gene, Integer> latestHour = gene ->
            gene.getHourList().stream()
                    .max(Comparator.comparingInt(Hour::getStartingHour))
                    .map(Hour::getStartingHour)
                    .orElse(0);

    Function<Gene, Gene> assignStartingHours = gene ->
            new Gene(gene.getHourList()
                    .stream()
                    .collect(Collectors.groupingBy(h -> h.getGroup().getIdentifier()))
                    .values()
                    .stream()
                    .flatMap(hs ->
                            Streams.zip(Stream.iterate(8, i -> i + 1), hs.stream(), (i, h) ->
                                    Hour.builder()
                                    .subject(h.getSubject())
                                    .group(h.getGroup())
                                    .professor(h.getProfessor())
                                    .startingHour(i)
                                    .build()))
                    .collect(Collectors.toList())
            , gene.dayType);



    // INDIVIDUAL FUNCTIONS

    public void fitness() {
        // 5
        Supplier<Integer> basicCharacteristicsScore = () ->
        {
            if (geneList.stream()
                    .allMatch(g -> overlappingClasses4groups.negate()
                            .and(overlappingClasses4teachers.negate())
                            .and(allGroupsStartAt8).test(g))) {
                return 1;
            } else {
                return 0;
            }
        };

        // -80
        Supplier<Integer> studentsGapScore = () ->
                geneList.stream()
                        .map(g -> gaps4students.apply(g))
                        .reduce(0, Integer::sum);

        // -70
        Supplier<Integer> teacherGapScore = () ->
                geneList.stream()
                        .map(g -> gaps4teachers.apply(g))
                        .reduce(0, Integer::sum);

        // 10
        Supplier<Integer> latestHourForEveryWeek = () ->
                geneList.stream()
                        .map(g -> latestHour.apply(g))
                        .map(lh -> lh - 14)
                        .reduce(0, Integer::sum);

        // fitness function
        this.fitness = 1000 * basicCharacteristicsScore.get() - teacherGapScore.get() - latestHourForEveryWeek.get() - 5 * studentsGapScore.get();
    }

    public void mutate(Float mutationProbability, Random r) {
        if (r.nextInt(100) < mutationProbability * 100) {
            // System.out.println("MUTATION");
            Integer size = geneList.size();
            Integer pos1 = r.nextInt(size);
            Integer pos2 = r.nextInt(size);
            Gene dayA = geneList.get(pos1);
            Gene dayB = geneList.get(pos2);
            if(r.nextInt()%2 == 0) {
                // two hours get swapped

                Hour hourA = dayA.hourList.get(dayA.hourList.size() - 1);
                Hour hourB = dayB.hourList.get(dayB.hourList.size() - 1);

                Integer aux = hourA.getStartingHour();
                hourA.setStartingHour(hourB.getStartingHour());
                hourB.setStartingHour(aux);

                dayA.setHourList(Stream.concat(dayA.getHourList().subList(0, dayA.getHourList().size() - 1).stream(), Stream.of(hourB)).collect(Collectors.toList()));
                dayB.setHourList(Stream.concat(dayB.getHourList().subList(0, dayB.getHourList().size() - 1).stream(), Stream.of(hourA)).collect(Collectors.toList()));

                geneList.set(pos1, dayA);
                geneList.set(pos2, dayB);
            }else{
                // move one class around

                if(dayA.getHourList().size() > 1){
                    Hour hourA = dayA.hourList.get(dayA.hourList.size() - 1);

                    dayA.setHourList(dayA.getHourList().subList(0, dayA.getHourList().size() - 1));
                    dayB.setHourList(Stream.concat(dayB.getHourList().stream(), Stream.of(hourA)).collect(Collectors.toList()));

                    geneList.set(pos1, dayA);
                    geneList.set(pos2, dayB);
                }
            }
        }
    }

    Function<Hour, Hour> deepCopyHour = h -> Hour.builder().startingHour(h.getStartingHour()).group(h.getGroup()).professor(h.getProfessor()).subject(h.getSubject()).build();

    Function<Gene, Gene> deepCopyGene = g -> Gene.builder().dayType(g.getDayType()).hourList(g.getHourList().stream().map(h -> deepCopyHour.apply(h)).collect(Collectors.toList())).build();

    Function<Individual, Individual> deepCopyIndividual = ind ->
    {
        Individual newind = new Individual();
        newind.geneList = ind.geneList.stream().map(g -> deepCopyGene.apply(g)).collect(Collectors.toList());
        return newind;
    };

    public List<Individual> crossover(Individual other, Float crossoverProbability) {
        if (new Random().nextInt(100) < crossoverProbability * 100) {
            // pass
        }
        return List.of(deepCopyIndividual.apply(this), deepCopyIndividual.apply(other));
    }

    Supplier<Stream<String>> groups = () ->
            geneList.stream().flatMap(g -> g.getHourList().stream()).collect(Collectors.groupingBy(h -> h.getGroup().getIdentifier())).keySet().stream();

    @Override
    public String toString() {
        groups.get().forEach(group -> {
            System.out.println();
            System.out.println(group);
            geneList.forEach(g -> {
                System.out.println("dayType =");
                System.out.println(g.dayType);
                System.out.println(g.hourList.stream().filter(h -> (Objects.equals(h.getGroup().getIdentifier(), group))).collect(Collectors.toList()));
            });
        });
        return "";
    }

    public static String modelToString(Individual individual){
        String string = "";
        for(Gene gene: individual.geneList){
            string += Gene.modelToString(gene) + "\n***";
        }
        string += "\n" + (individual.fitness == null ? 0 : individual.fitness);
        return string;
    }

    public static Individual stringToModel(String string){
        String[] parts = string.split("\\*\\*\\*");
        Integer fitness = Integer.valueOf(parts[parts.length - 1].strip());
        parts = Arrays.copyOf(parts, parts.length - 1);
        List<Gene> genes = Arrays.stream(parts).map(Gene::stringToModel).collect(Collectors.toList());
        return new Individual(genes, fitness);

    }
}