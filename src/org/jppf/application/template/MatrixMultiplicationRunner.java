/*
 * JPPF.
 * Copyright (C) 2005-2013 JPPF Team.
 * http://www.jppf.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jppf.application.template;

import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;

import org.jppf.client.*;
import org.jppf.server.protocol.JPPFTask;

/**
 * This is a template JPPF application runner.
 * It is fully commented and is designed to be used as a starting point
 * to write an application using JPPF.
 * @author Laurent Cohen
 */
public class MatrixMultiplicationRunner {
  /**
   * The JPPF client, handles all communications with the server.
   * It is recommended to only use one JPPF client per JVM, so it
   * should generally be created and used as a singleton.
   */
  private static JPPFClient jppfClient =  null;

  /**
   * The entry point for this application runner to be run from a Java command line.
   * @param args by default, we do not use the command line arguments,
   * however nothing prevents us from using them if need be.
   */
  public static void main(final String...args) throws FileNotFoundException, MatrixMultiplicationSolver.FailBuildFromFileException {
    if(args.length != 4) {
        System.err.println("Usage: java MatrixMultiplicationSolver ROW_JOBS COL_JOBS FILE_A FILE_B");
        throw new IllegalArgumentException();
    }
    int mJob = Integer.parseInt(args[0]);
    int nJob = Integer.parseInt(args[1]);
    int[][] a = MatrixMultiplicationSolver.buildMatrixFromFile(args[2]);
    int[][] b = MatrixMultiplicationSolver.buildMatrixFromFile(args[3]);
    try {
      // create the JPPFClient. This constructor call causes JPPF to read the configuration file
      // and connect with one or multiple JPPF drivers.
      jppfClient = new JPPFClient();

      // create a runner instance.
      MatrixMultiplicationRunner runner = new MatrixMultiplicationRunner();

      // Create a job
      JPPFJob job = runner.createJob(a, b, mJob, nJob);

      // execute a blocking job
      final List<JPPFTask> results = runner.executeBlockingJob(job);

      // execute a non-blocking job
      //runner.executeNonBlockingJob(job);

      // Collect results
      int[][] totalResult = new int[a.length][b[0].length];
      for(JPPFTask t: results){
          if(t.getException() == null){
              MatrixSubMultiplicationResult r = (MatrixSubMultiplicationResult) t.getResult();
              MatrixMultiplicationSolver.mergeSubResult(totalResult, r.matrix, r.m1, r.n1);
          } else {
              t.getException().printStackTrace();
          }
      }

      // Output results;
      System.out.println(totalResult.length + " " + totalResult[0].length);
      for(int[] r: totalResult){
          for(int c: r) System.out.print(c + " ");
          System.out.println("");
      }

    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      if (jppfClient != null) jppfClient.close();
    }
  }

  /**
   * Create a JPPF job that can be submitted for execution.
   * @return an instance of the {@link org.jppf.client.JPPFJob JPPFJob} class.
   * @throws Exception if an error occurs while creating the job or adding tasks.
   */
  public JPPFJob createJob(int[][] a, int[][] b, int mJob, int nJob) throws Exception {
    // create a JPPF job
    JPPFJob job = new JPPFJob();

    // give this job a readable unique id that we can use to monitor and manage it.
    job.setName("MatrixMultiplication");

    // add a task to the job.
    //job.addTask(new MatrixSubMultiplicationJob());

    // add more tasks here ...
    for(int i = 0 ; i < mJob ; i++){
        int m1 = a.length / mJob * i;
        int m2 = m1 + a.length / mJob + (i + 1 == mJob ? a.length % mJob : 0);
        for(int j = 0; j < nJob ; j++){
            int n1 = b[0].length / nJob * j;
            int n2 = n1 + b[0].length / nJob + (j + 1 == nJob ? b[0].length % nJob : 0);
            //jobs[i][j] = new MatrixSubMultiplicationJob(a, b, m1, m2, n1, n2);
            //jobs[i][j].start();
            job.addTask(new MatrixSubMultiplicationTask(a, b, m1, m2, n1, n2));
        }
    }

    // there is no guarantee on the order of execution of the tasks,
    // however the results are guaranteed to be returned in the same order as the tasks.
    return job;
  }

  /**
   * Execute a job in blocking mode. The application will be blocked until the job
   * execution is complete.
   * @param job the JPPF job to execute.
   * @throws Exception if an error occurs while executing the job.
   */
  public List<JPPFTask> executeBlockingJob(final JPPFJob job) throws Exception {
    // set the job in blocking mode.
    job.setBlocking(true);

    // Submit the job and wait until the results are returned.
    // The results are returned as a list of JPPFTask instances,
    // in the same order as the one in which the tasks where initially added the job.
    List<JPPFTask> results = jppfClient.submit(job);

    // process the results
    return results;
  }

  /**
   * Execute a job in non-blocking mode. The application has the responsibility
   * for handling the notification of job completion and collecting the results.
   * @param job the JPPF job to execute.
   * @throws Exception if an error occurs while executing the job.
   */
  public List<JPPFTask> executeNonBlockingJob(final JPPFJob job) throws Exception {
    // set the job in non-blocking (or asynchronous) mode.
    job.setBlocking(false);

    // this call returns immediately. We will use the collector at a later time
    // to obtain the execution results asynchronously
    JPPFResultCollector collector = submitNonBlockingJob(job);

    // the non-blocking job execution is asynchronous, we can do anything else in the meantime
    System.out.println("Doing something while the job is executing ...");
    // ...

    // We are now ready to get the results of the job execution.
    // We use JPPFResultCollector.waitForResults() for this. This method returns immediately with
    // the results if the job has completed, otherwise it waits until the job execution is complete.
    List<JPPFTask> results = collector.waitForResults();

    // process the results
    return results;
  }

  /**
   * Execute a job in non-blocking mode. The application has the responsibility
   * for handling the notification of job completion and collecting the results.
   * @param job the JPPF job to execute.
   * @return a JPPFResultCollector used to obtain the execution results at a later time.
   * @throws Exception if an error occurs while executing the job.
   */
  public JPPFResultCollector submitNonBlockingJob(final JPPFJob job) throws Exception {
    // set the job in non-blocking (or asynchronous) mode.
    job.setBlocking(false);

    // We need to be notified of when the job execution has completed.
    // To this effect, we define an instance of the TaskResultListener interface,
    // which we will register with the job.
    // Here, we use an instance of JPPFResultCollector, conveniently provided by the JPPF API.
    // JPPFResultCollector implements TaskResultListener and has a constructor that takes
    // the number of tasks in the job as a parameter.
    JPPFResultCollector collector = new JPPFResultCollector(job);
    job.setResultListener(collector);

    // Submit the job. This call returns immediately without waiting for the execution of
    // the job to complete. As a consequence, the object returned for a non-blocking job is
    // always null. Note that we are calling the exact same method as in the blocking case.
    jppfClient.submit(job);

    // finally return the result collector, so it can be used to collect the exeuction results
    // at a time of our chosing. The collector can also be obtained at any time by calling 
    // (JPPFResultCollector) job.getResultListener()
    return collector;
  }

  /**
   * Process the execution results of each submitted task. 
   * @param results the tasks results after execution on the grid.
   */
  public void processExecutionResults(final List<JPPFTask> results) {
    // process the results
    for (JPPFTask task: results) {
      // if the task execution resulted in an exception
      if (task.getException() != null) {
        // process the exception here ...
      }
      else {
        // process the result here ...
      }
    }
  }
}
