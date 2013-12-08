package org.jppf.application.matrixmultiplication;

public class MatrixSubMultiplicationResult implements java.io.Serializable {
    public final int m1, n1; 
    public final int [][] matrix;
    public MatrixSubMultiplicationResult(int m1, int n1, int [][] matrix) {
        this.m1 = m1;
        this.n1 = n1;
        this.matrix = matrix;
        return;
    }
}
