/**
 * A job unit that can be run in a thread
 */
public class MatrixSubMultiplicationJob extends Thread {
    private int[][] m;
    private final int [][] a, b;
    public final int m1, m2, n1, n2; // TODO public?!

    public int[][] getResult(){return m;}

    public MatrixSubMultiplicationJob(int[][] a, int[][] b, int m1, int m2, int n1, int n2) {
        this.a = a;
        this.b = b;
        this.m1 = m1;
        this.m2 = m2;
        this.n1 = n1;
        this.n2 = n2;
    }

    @Override
    public void run() {
        try {
            m = MatrixMultiplicationSolver.solve(a, b, m1, m2, n1, n2);
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
}
