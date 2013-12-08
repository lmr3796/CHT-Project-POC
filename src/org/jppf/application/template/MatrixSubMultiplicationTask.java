package org.jppf.application.template;

import org.jppf.server.protocol.JPPFTask;

/**
 * A job unit that can be run in a thread
 */
public class MatrixSubMultiplicationTask extends JPPFTask implements Runnable{
    private MatrixSubMultiplicationResult m;
    private final int [][] a, b;
    private final int m1, m2, n1, n2;

    public MatrixSubMultiplicationResult getResult(){return m;}

    public MatrixSubMultiplicationTask(int[][] a, int[][] b, int m1, int m2, int n1, int n2) {
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
            m = new MatrixSubMultiplicationResult(m1, n1, MatrixMultiplicationSolver.solve(a, b, m1, m2, n1, n2));
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
}
