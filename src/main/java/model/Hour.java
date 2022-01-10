package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hour {
    private Group group;
    private Professor professor;
    private String subject;
    private Integer hours;

    public static Hour stringToHour(String line){
        String[] parts = line.split(" - ");
        return new Hour(new Group(parts[0]), new Professor(parts[1]), parts[2], Integer.parseInt(parts[3]));
    }
}
