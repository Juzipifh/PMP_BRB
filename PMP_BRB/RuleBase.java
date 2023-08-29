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

import java.util.ArrayList;

import org.core.Files;

/**
 * This class contains the representation of a Rule Set
 *
 * @author Written by Alberto Fernández (University of Granada) 29/10/2007
 * @version 1.0
 * @since JDK1.5
 */
public class RuleBase {

    ArrayList<Rule> ruleBase;
    int n_variables, numRules;
    String[] names, classes;

    /**
     * Rule Base Constructor
     * @param dataBase DataBase the Data Base containing the fuzzy partitions
     * @param compatibilityType int the compatibility type for the t-norm
     * @param ruleWeight int the rule weight heuristic
     * @param names String[] the names for the features of the problem
     * @param classes String[] the labels for the class attributes
     */
    public RuleBase(double[][] refvalues, double[] ruleWeights, double[][] ruleConsequents, String[] names, String[] classes,int numRules) {
        ruleBase = new ArrayList<Rule>();
        this.names = names.clone();
        this.classes = classes.clone();
        this.n_variables=refvalues[0].length;
        this.numRules=numRules;
        for (int index = 0; index < numRules; index ++){
            Rule r = new Rule(n_variables, classes.length);
            for(int j=0;j<n_variables;j++){
                Fuzzy ante=new Fuzzy();
                ante.label=index;
                ante.name="Label_"+j;
                ante.y=1;
                if(index==0){
                    ante.x0=refvalues[index][j]-(refvalues[index+1][j]-refvalues[index][j]);
                    ante.x1=refvalues[index][j];
                    ante.x3=refvalues[index+1][j];
                }else if(index==refvalues.length-1){
                    ante.x0=refvalues[index-1][j];
                    ante.x1=refvalues[index][j];
                    ante.x3=refvalues[index][j]+(refvalues[index][j]-refvalues[index-1][j]);
                }else{
                    ante.x0=refvalues[index-1][j];
                    ante.x1=refvalues[index][j];
                    ante.x3=refvalues[index+1][j];
                }
                r.antecedent[j]=ante;
            }
            r.setConsequent(ruleConsequents[index]);
            r.setRweight(ruleWeights[index]);
            ruleBase.add(r);
        }
    }


    
    public double findmax(double a, double[] values){ // 找到数列中比a大的最小值
        double maxThana=Double.POSITIVE_INFINITY;
        for(int i=0;i<values.length;i++){
            if (values[i]>a && values[i]<maxThana){
                maxThana=values[i];
            }
        }
        if (maxThana==Double.POSITIVE_INFINITY){
            maxThana=a;
        }
        return maxThana;
    }

    public double findmin(double a, double[] values){ // 找到数列中比a小的最大值
        double minThana=Double.NEGATIVE_INFINITY;
        for(int i=0;i<values.length;i++){
            if (values[i]<a && values[i]>minThana){
                minThana=values[i];
            }
        }
        if (minThana==Double.NEGATIVE_INFINITY){
            minThana=a;
        }
        return minThana;
    }

    /**
     * It prints the rule base into an string
     * @return String an string containing the rule base
     */
    public String printString() {
        int i, j;
        String cadena = "";

        cadena += "@Number of rules: " + ruleBase.size() + "\n\n";
        for (i = 0; i < ruleBase.size(); i++) {
            String conse ="[" ;
            Rule r = ruleBase.get(i);
            cadena += (i + 1) + ": ";
            for (j = 0; j < n_variables - 1; j++) {
                cadena += names[j] + " IS " + r.antecedent[j].printString()+ " AND ";
            }
            for (int k=0;k<r.consequenceEvi.length-1;k++){
                conse+=r.consequenceEvi[k]+",";
            }
            conse+=r.consequenceEvi[r.consequenceEvi.length-1]+"]";
            cadena += names[j] + " IS " + r.antecedent[j].printString() + ": " +
                    conse + " with Rule Weight: " + r.Rweight + "\n\n";
        }

        return (cadena);
    }

    /**
     * It writes the rule base into an ouput file
     * @param filename String the name of the output file
     */
    public void writeFile(String filename) {
        String outputString = new String("");
        outputString = printString();
        Files.writeFile(filename, outputString);
    }

    // /**
    //  * Fuzzy Reasoning Method
    //  * @param example double[] the input example
    //  * @return int the predicted class label (id)
    //  */
    // public int FRM(double[] example) {
    //     if (this.inferenceType == Fuzzy_Chi.WINNING_RULE) {
    //         return FRM_WR(example);
    //     } else {
    //         return FRM_AC(example);
    //     }
    // }

    /**
     * Winning Rule FRM
     * @param example double[] the input example
     * @return int the class label for the rule with highest membership degree to the example
     */
    public int FRM(double[] example) {
        int clas = -1;
        double [] consequence =new double [classes.length];
        double max = 0.0;
        double[] fir=new double [classes.length];
        double sed=0.0;
        for (int i=0;i<ruleBase.size();i++){
            Rule r =ruleBase.get(i);
            double actWeight=r.comActWeight(example);
            for (int j=0;j<classes.length;j++){
                fir[j]+=actWeight*r.consequenceEvi[j]+1-actWeight*sum(r.consequenceEvi);
            }
            sed+=1-actWeight*sum(r.consequenceEvi);
        }
        double mu=1/(sum(fir)-(classes.length-1)*sed);
        for (int i=0;i<classes.length;i++){
            consequence[i]=mu*(fir[i]-sed)/(1-mu*sed);
            if (consequence[i]>max){
                max=consequence[i];
                clas=i;
            }
        }
        return clas;
    }

    public double[] DisFRM(double[] example) {
        double [] consequence =new double [classes.length];
        double[] fir=new double [classes.length];
        double sed=0.0;
        for (int i=0;i<ruleBase.size();i++){
            Rule r =ruleBase.get(i);
            double actWeight=r.comActWeight(example);
            for (int j=0;j<classes.length;j++){
                fir[j]+=actWeight*r.consequenceEvi[j]+1-actWeight;
            }
            sed+=1-actWeight;
        }
        double mu=1/(sum(fir)-(classes.length-1)*sed);
        for (int i=0;i<classes.length;i++){
            consequence[i]=mu*(fir[i]-sed)/(1-mu*sed);
        }
        return consequence;
    }

    public double sum(double [] example){
        double sum=0.0;
        for(int i=0;i<example.length;i++){
            sum+=example[i];
        }
        return sum;
    }

    public int getRuleNum(){
        return ruleBase.size();
    }

    public double getAveFeatureNum(){
        double featureSum=0;
        for (int i=0;i<ruleBase.size();i++){
            featureSum+=1.0*ruleBase.get(i).antecedent.length/ruleBase.size();
        }
        return featureSum;
    }

}

