package org.jppf.application.template;

import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;

// TODO One class for one role.
// TODO I think that move main function out of this class is better.
public final class MatrixMultiplicationSolver {
    public static class DimensionNotMatchException extends IllegalArgumentException{}
    public static class ArrayNotUniformException extends IllegalArgumentException{}
    public static class FailBuildFromFileException extends IllegalArgumentException{}

    private static boolean matrixIsUniform(int[][] a) {
        for(int[] row: a) if(!(row.length > 0 && row.length == a[0].length)){ return false;}
        return true;
    }

    private static boolean matrixDimensionMatch(int[][] a, int [][] b){
        return a[0].length == b.length;
    }

    /**
     * A partial solver solves, matrix multiplication a*b, which is size of N*N
     * The solve range is {[x1, x2) x [y1, y2)}
     */
    public static int[][] solve(int[][] a, int[][] b, int m1, int m2, int n1, int n2) {
        // Sanity check
        if(!(m1 >= 0 && m2 <= a.length && m2 > m1)) throw new IllegalArgumentException();
        if(!(n1 >= 0 && n2 <= b[0].length && n2 > n1)) throw new IllegalArgumentException();
        if(!matrixIsUniform(a)) throw new ArrayNotUniformException();
        if(!matrixIsUniform(b)) throw new ArrayNotUniformException();
        if(!matrixDimensionMatch(a, b)) throw new DimensionNotMatchException();

        int[][] ans = new int[m2-m1][n2-n1];
        for(int i = 0 ; i < ans.length ; i++)
            for(int j = 0 ; j < ans[i].length ; j++)
                for(int k = 0 ; k < a[0].length ; k++) // Sums up a[m1+i][0~N-1] * b[0~N-1][n1+j]
                    ans[i][j] += a[m1+i][k] * b[k][n1+j];
        return ans;
    }

    // I plan to use it as a tool
    public static int[][] buildMatrixFromFile(String fileName)
            throws FileNotFoundException, FailBuildFromFileException {
        Scanner scanner = new Scanner(new BufferedReader(new FileReader(fileName)));
        int m = scanner.nextInt(); // TODO What if parse error?
        int n = scanner.nextInt();
        int [][] data = new int[m][n];
        for(int i = 0 ; i < data.length ; i++) for(int j = 0 ; j < data[i].length ; j++){
            if(!scanner.hasNextInt()) throw new FailBuildFromFileException();
            data[i][j] = scanner.nextInt();
        }
        return data;
    }
    

    /**
     * Place sub[0][0] at total[m][n]
     */
    private static void mergeSubResult(int[][] total, int[][] sub, int m, int n){
        for(int i = 0 ; i < sub.length ; i++)
            for(int j = 0 ; j < sub[i].length ; j++)
                total[m+i][j+n] = sub[i][j];
        return;
    }

    // TODO when you use one line comment to illustrate what you are
    // doing.  You could consider use small methods with a descriptive
    // name.
    public static void main(String[] args) throws FileNotFoundException, FailBuildFromFileException{
        if(args.length != 4) {
            System.err.println("Usage: java MatrixMultiplicationSolver ROW_JOBS COL_JOBS FILE_A FILE_B");
            throw new IllegalArgumentException();
        }
        int mJob = Integer.parseInt(args[0]);
        int nJob = Integer.parseInt(args[1]);
        int[][] a = buildMatrixFromFile(args[2]);
        int[][] b = buildMatrixFromFile(args[3]);

        // Deploy the jobs
        MatrixSubMultiplicationJob[][] jobs = new MatrixSubMultiplicationJob[mJob][nJob];
        for(int i = 0 ; i < mJob ; i++){
            int m1 = a.length / mJob * i;
            int m2 = m1 + a.length / mJob + (i + 1 == mJob ? a.length % mJob : 0);
            for(int j = 0; j < nJob ; j++){
                int n1 = b[0].length / nJob * j;
                int n2 = n1 + b[0].length / nJob + (j + 1 == nJob ? b[0].length % nJob : 0);
                jobs[i][j] = new MatrixSubMultiplicationJob(a, b, m1, m2, n1, n2);
                jobs[i][j].start();
            }
        }

        // Wait till complete
        for(Thread[] jobRow: jobs) for(Thread j: jobRow)
            try {j.join();} catch (InterruptedException e) {}

        // Collect results
        int[][] totalResult = new int[a.length][b[0].length];
        for(int i = 0; i < mJob ; i++)
            for(int j = 0; j < nJob ; j++)
                mergeSubResult(totalResult, jobs[i][j].getResult().matrix, jobs[i][j].getResult().m1, jobs[i][j].getResult().n1);

        // Output results;
        System.out.println(totalResult.length + " " + totalResult[0].length);
        for(int[] r: totalResult){
            for(int c: r) System.out.print(c + " ");
            System.out.println("");
        }

        return;
    }
}

