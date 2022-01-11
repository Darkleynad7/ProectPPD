package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gene{
    List<Hour> hourList;
    DayType dayType;

    public static String modelToString(Gene gene) {
        String string = "";
        for(Hour hour: gene.hourList){
            string += Hour.modelToString(hour);
        }
        string += gene.dayType;
        return string;
    }

    public static Gene stringToModel(String string){
        String[] parts = string.split("\n");
        DayType dayType = DayType.valueOf(parts[parts.length - 1]);
        parts = Arrays.copyOf(parts, parts.length - 1);
        List<Hour> hours = Arrays.stream(parts).map(Hour::stringToModel).collect(Collectors.toList());
        return new Gene(hours, dayType);
    }
}
