import service.Service;

import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args){
        Service service = new Service();
        try {
            service.run("src/main/resources/input.in");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
