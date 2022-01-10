package model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import lombok.Data;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Data
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
                    ( hs.stream().map(Hour::getHours).min(Comparator.comparingInt(i -> i)).orElse(0)
                    , h -> h <= hs.stream().map(Hour::getHours).max(Comparator.comparingInt(i -> i)).orElse(0)
                    , h -> h + 1);

     Function<Gene, Integer> gaps = g ->
             hours4teachers.apply(g).map(hs -> Sets.difference(hs.stream().collect(Collectors.toSet()), getIntervalBetweenMinAndMax.apply(hs).collect(Collectors.toSet()))



    // PREDICATES

   Predicate<Gene> allGroupsStartAt8           = g ->
           hours4groups.apply(g).allMatch(hs ->
                   hs.stream()
                           .map(Hour::getHours)
                           .min(Comparator.comparingInt(a -> a))
                           .map(h -> h == 8)
                           .orElse(false));

   Predicate<Gene> overlappingClasses4groups   = g ->
           hours4groups.apply(g).anyMatch(hs -> hasDups.test(hs));

   Predicate<Gene> overlappingClasses4teachers = g ->
           hours4teachers.apply(g).anyMatch(hs -> hasDups.test(hs));

   // only relevant if the groups start at 8
   Predicate<Gene> groupsThatStartAt8HaveGaps = g ->
           hours4groups.apply(g)
           .map(hs -> hs.stream().map(Hour::getHours).collect(Collectors.toSet()))
           .map(set -> Sets.difference(Stream.iterate(8, i -> i + 1).limit(set.size()).collect(Collectors.toSet()), set))
           .anyMatch(set -> set.size() > 0);



   // CONSTRUCTOR
   Individual(List<Hour> hss){
        geneList = Streams.zip
                        ( split.apply(hss.stream(), hss.size()/5)
                        , Stream.of(DayType.MONDAY, DayType.TUESDAY, DayType.WEDNESDAY, DayType.THURSDAY, DayType.FRIDAY)
                        , Gene::new
                        ).collect(Collectors.toList());
    }



    // INDIVIDUAL FUNCTIONS

    public void fitness(){
   geneList.stream().map(g -> if (overlappingClasses4groups.and(overlappingClasses4teachers).and(allGroupsStartAt8).test(g)) )
    }

    // move hours
    public void mutate(Float mutationProbability) {
       if (new Random().nextInt(100) < mutationProbability*100){
           Random r = new Random();
           Integer size = geneList.size();
           Integer i = r.nextInt(size);
           Gene g1 = geneList.get(i);
           Gene g2 = geneList.get(size - i - 1);
           g1.setHourList(Stream.concat(g1.getHourList().stream(), Stream.of(g2.getHourList().get(g2.getHourList().size() - 1))).collect(Collectors.toList()));
           g2.setHourList(g2.getHourList().subList(0, g2.getHourList().size() - 1));
           geneList.set(i, g2);
           geneList.set(size - i - 1, g1);
       }
    }

    // excange hours
    public List<Individual> crossover(Individual other, Float crossoverProbability){
       if (new Random().nextInt()%100 < crossoverProbability){

       }
    }
}
V