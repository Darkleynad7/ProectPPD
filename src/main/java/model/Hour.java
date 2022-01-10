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

    public static Hour stringToHour(String[] line){
        Hour hour = new Hour();
        hour.setGroup(new Group(line[0]));
        hour.setProfessor(new Professor(line[1]));
        hour.setSubject(line[2]);
        return hour;
    }
}
