package model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import lombok.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Individual {
    private List<Gene> geneList;
    private Integer fitness;


    // HELPER FUNCTIONS

    BiFunction<Stream<Hour>, Integer, Stream<List<Hour>>> split = (ss, k) ->
            StreamSupport.stream(Iterables.partition(ss.collect(Collectors.toList()), k).spliterator(), false);

    Function<Gene, Stream<List<Hour>>> hours4groups = g ->
            g.getHourList().stream().collect(Collectors.groupingBy(Hour::getGroup)).values().stream();

    Function<Gene, Stream<List<Hour>>> hours4teachers = g ->
            g.getHourList().stream().collect(Collectors.groupingBy(Hour::getProfessor)).values().stream();

    Predicate<List<Hour>> hasDups = hs ->
            hs.stream()
                    .filter(i -> Collections.frequency(hs, i) > 1)
                    .collect(Collectors.toSet()).size() > 0;

    Function<List<Hour>, Stream<Integer>> getIntervalBetweenMinAndMax = hs ->
            Stream.iterate
                    (hs.stream().map(Hour::getStartingHour).min(Comparator.comparingInt(i -> i)).orElse(0)
                            , h -> h <= hs.stream().map(Hour::getStartingHour).max(Comparator.comparingInt(i -> i)).orElse(0)
                            , h -> h + 1);

    Function<Gene, Integer> gaps4teachers = g ->
            hours4teachers.apply(g).map(hs -> Sets.difference
                                    (getIntervalBetweenMinAndMax.apply(hs).collect(Collectors.toSet())
                                            , new HashSet<>(hs))
                            .size())
                    .reduce(0, Integer::sum);


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

    // only relevant if the groups start at 8
    Predicate<Gene> groupsThatStartAt8HaveGaps = g ->
            hours4groups.apply(g)
                    .map(hs -> hs.stream().map(Hour::getStartingHour).collect(Collectors.toSet()))
                    .map(set -> Sets.difference(Stream.iterate(8, i -> i + 1).limit(set.size()).collect(Collectors.toSet()), set))
                    .anyMatch(set -> set.size() > 0);


    // CONSTRUCTOR
    Individual(List<Hour> hss) {
        geneList = Streams.zip
                        (split.apply(hss.stream(), hss.size() / 5)
                                , Stream.of(DayType.MONDAY, DayType.TUESDAY, DayType.WEDNESDAY, DayType.THURSDAY, DayType.FRIDAY)
                                , Gene::new
                        )
                .map(g ->
                        new Gene(Streams.zip
                                (Stream.iterate(8, i -> i + 1)
                                        , g.getHourList().stream()
                                        , (i, h) -> Hour.builder()
                                                .subject(h.getSubject())
                                                .group(h.getGroup())
                                                .professor(h.getProfessor())
                                                .startingHour(i)
                                                .build()
                                ).collect(Collectors.toList())
                                , g.dayType)
                ).collect(Collectors.toList());
    }


    // INDIVIDUAL FUNCTIONS

    public void fitness() {
        Integer basicCharacteristicsScore =
                geneList.stream()
                        .map(g -> {
                            if (overlappingClasses4groups.negate()
                                    .and(overlappingClasses4teachers.negate())
                                    .and(allGroupsStartAt8).test(g)) {
                                return 1;
                            } else {
                                return 0;
                            }
                        })
                        .reduce(0, Integer::sum);

        Integer studentsGapScore =
                geneList.stream()
                        .map(g -> {
                            if (groupsThatStartAt8HaveGaps.test(g)) {
                                return 1;
                            } else {
                                return 0;
                            }
                        })
                        .reduce(0, Integer::sum);


        Integer teacherGapScore =
                geneList.stream()
                        .map(g -> gaps4teachers.apply(g))
                        .reduce(0, Integer::sum);

        // fitness function
        this.fitness = 20 * basicCharacteristicsScore - 5 * teacherGapScore + 10 * studentsGapScore;
    }

    public void mutate(Float mutationProbability) {
        if (new Random().nextInt(100) < mutationProbability * 100) {
            Random r = new Random();
            Integer size = geneList.size();
            Integer i = r.nextInt(size);
            Gene dayA = geneList.get(i);
            Gene dayB = geneList.get(size - i - 1);
            Hour hourA = dayA.hourList.get(0);
            Hour hourB = dayB.hourList.get(0);

            Integer aux = hourA.getStartingHour();
            hourA.setStartingHour(hourB.getStartingHour());
            hourB.setStartingHour(aux);

            dayA.setHourList(Stream.concat(dayA.getHourList().subList(0, dayA.getHourList().size()).stream(), Stream.of(hourB)).collect(Collectors.toList()));
            dayB.setHourList(Stream.concat(dayB.getHourList().subList(0, dayB.getHourList().size()).stream(), Stream.of(hourA)).collect(Collectors.toList()));

            geneList.set(i, dayA);
            geneList.set(size - i - 1, dayB);
        }
    }

    public List<Individual> crossover(Individual other, Float crossoverProbability) {
        if (new Random().nextInt(100) < crossoverProbability * 100) {
            // pass
        }
        return List.of(this, other);
    }

    @Override
    public String toString() {

    }
}