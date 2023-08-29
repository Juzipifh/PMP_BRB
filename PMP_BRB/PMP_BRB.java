/***********************************************************************

	This file is part of KEEL-software, the Data Mining tool for regression, 
	classification, clustering, pattern mining and so on.

	Copyright (C) 2004-2010
	
	F. Herrera (herrera@decsai.ugr.es)
    L. Sánchez (luciano@uniovi.es)
    J. Alcalá-Fdez (jalcala@decsai.ugr.es)
    S. García (sglopez@ujaen.es)
    A. Fernández (alberto.fernandez@ujaen.es)
    J. Luengo (julianlm@decsai.ugr.es)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see http://www.gnu.org/licenses/
  
**********************************************************************/

package keel.Algorithms.Fuzzy_Rule_Learning.AdHoc.PMP_BRB;

import java.io.IOException;
import org.core.*;

/**
 * <p>It contains the implementation of the Chi algorithm</p>
 *
 * @author Written by Alberto Fernández (University of Granada) 02/11/2007
 * @version 1.0
 * @since JDK1.5
 */
public class PMP_BRB {

  myDataset train, val, test;
  String outputTr, outputTst, fileDB, fileRB;
  int nClasses, minR, maxR;
  int MaxCycle, NP;
  double F, CR;
  RuleBase ruleBase;

  private boolean somethingWrong = false; //to check if everything is correct.

  /**
   * Default constructor
   */
  public PMP_BRB() {
  }

  /**
   * It reads the data from the input files (training, validation and test) and parse all the parameters
   * from the parameters array.
   * @param parameters parseParameters It contains the input files, output files and parameters
   */
  public PMP_BRB(parseParameters parameters) {

    train = new myDataset();
    val = new myDataset();
    test = new myDataset();
    try {
      System.out.println("\nReading the training set: " +
                         parameters.getTrainingInputFile());
      train.readClassificationSet(parameters.getTrainingInputFile(), true);
      System.out.println("\nReading the validation set: " +
                         parameters.getValidationInputFile());
      val.readClassificationSet(parameters.getValidationInputFile(), false);
      System.out.println("\nReading the test set: " +
                         parameters.getTestInputFile());
      test.readClassificationSet(parameters.getTestInputFile(), false);
    } catch (IOException e) {
      System.err.println(
          "There was a problem while reading the input data-sets: " +
          e);
      somethingWrong = true;
    }

    //We may check if there are some numerical attributes, because our algorithm may not handle them:
    //somethingWrong = somethingWrong || train.hasNumericalAttributes();
    somethingWrong = somethingWrong || train.hasMissingAttributes();

    outputTr = parameters.getTrainingOutputFile();
    outputTst = parameters.getTestOutputFile();

    fileDB = parameters.getOutputFile(0);
    fileRB = parameters.getOutputFile(1);

    //Now we parse the parameters
    minR = Integer.parseInt(parameters.getParameter(0));
    maxR = Integer.parseInt(parameters.getParameter(1));
    MaxCycle = Integer.parseInt(parameters.getParameter(2));
    NP=Integer.parseInt(parameters.getParameter(3));
    F=Double.parseDouble(parameters.getParameter(4));
    CR=Double.parseDouble(parameters.getParameter(5));

  }

  /**
   * It launches the algorithm
   */
  public void execute() {
    if (somethingWrong) { //We do not execute the program
      System.err.println("An error was found, the data-set have missing values");
      System.err.println("Please remove those values before the execution");
      System.err.println("Aborting the program");
      //We should not use the statement: System.exit(-1);
    }
    else {
      //We do here the algorithm's operations

      nClasses = train.getnClasses();

      int gen=0;
      //Initialize:
      Population pop=new Population(train, val, minR, maxR,NP,F,CR);

      Individual indBest=new Individual();
      //Optimization
      while(gen<=MaxCycle){
        pop.Mutation();
        pop.Crossover();
        pop.Selection();
        gen++;
        indBest=pop.saveBest();
      }
      ruleBase=indBest.ruleBase;
      System.out.println("Number of rules:"+ruleBase.getRuleNum()+"\n");
      System.out.println("Number of features:"+ruleBase.getAveFeatureNum()+"\n");
      ruleBase.writeFile(this.fileRB);

      //Finally we should fill the training and test output files
      double accTra = doOutput(this.val, this.outputTr);
      double accTst = doOutput(this.test, this.outputTst);
      // double accTra=1.0-indBest.fitness;
      // double accTst=accTra;
      System.out.println("Accuracy obtained in training: "+accTra);
      System.out.println("Accuracy obtained in test: "+accTst);
      System.out.println("Algorithm Finished");
    }
  }

  /**
   * It generates the output file from a given dataset and stores it in a file
   * @param dataset myDataset input dataset
   * @param filename String the name of the file
   *
   * @return The classification accuracy
   */
  private double doOutput(myDataset dataset, String filename) {
    String output = new String("");
    int hits = 0;
    output = dataset.copyHeader(); //we insert the header in the output file
    //We write the output for each example
    for (int i = 0; i < dataset.getnData(); i++) {
      //for classification:
      String classOut = this.classificationOutput(dataset.getExample(i));
      output += dataset.getOutputAsString(i) + " " + classOut + "\n";
      if (dataset.getOutputAsString(i).equalsIgnoreCase(classOut)){
        hits++;
      }
    }
    Files.writeFile(filename, output);
    return (1.0*hits/dataset.size());
  }

  /**
   * It returns the algorithm classification output given an input example
   * @param example double[] The input example
   * @return String the output generated by the algorithm
   */
  private String classificationOutput(double[] example) {
    String output = new String("?");
    /**
      Here we should include the algorithm directives to generate the
      classification output from the input example
     */
    int classOut = ruleBase.FRM(example);
    if (classOut >= 0) {
      output = train.getOutputValue(classOut);
    }
    return output;
  }

}

