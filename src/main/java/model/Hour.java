package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Hour{
    private Group group;
    private Professor professor;
    private String subject;
    private Integer startingHour;

    public static Hour stringToHour(String[] line){
        Hour hour = new Hour();
        hour.setGroup(new Group(line[0]));
        hour.setProfessor(new Professor(line[1]));
        hour.setSubject(line[2]);
        return hour;
    }

    public static String modelToString(Hour hour) {
        return hour.group.getIdentifier() + " - " + hour.professor.getName() + " - " + hour.subject + " - " + hour.startingHour + "\n";
    }

    public static Hour stringToModel(String string){
        String[] parts = string.split(" - ");
        return new Hour(new Group(parts[0]), new Professor(parts[1]), parts[2], Integer.parseInt(parts[3]));
    }
}
