package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hour {
    private Professor professor;
    private Group group;
    private Integer hours;
    private String subject;
}
