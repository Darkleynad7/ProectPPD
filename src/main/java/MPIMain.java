/*import mpi.MPI;

import java.util.Arrays;

public class MPIMain {
    public static void main(String[] args) {
        int[] a = new int[4];
        MPI.Init(args);
        if(MPI.COMM_WORLD.Rank() == 0){
            a = new int[]{1, 2, 3, 4};
            System.out.println("PLM");
        }

        int[] recvBuf = new int[1];

        MPI.COMM_WORLD.Scatter(a, 0, 1, MPI.INT,
                recvBuf, 0, 1, MPI.INT, 0);

        System.out.println(MPI.COMM_WORLD.Rank() + " " + Arrays.toString(recvBuf));
        MPI.Finalize();
    }
}
*/