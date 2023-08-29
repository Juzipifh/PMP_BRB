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


/**
 * <p>This class contains the structure of a Fuzzy Rule</p>
 *
 * @author Written by Alberto Fernández (University of Granada) 29/10/2007
 * @version 1.0
 * @since JDK1.5
 */

public class Rule {

  Fuzzy[] antecedent;
  double [] consequenceEvi;
  double Rweight;

  /**
   * Default constructor
   */
  public Rule() {
  }

  public Rule(int n_variables, int n_classes) {
    antecedent = new Fuzzy[n_variables];
    consequenceEvi = new double [n_classes];
  }

  public void setConsequent(double [] conse){
    this.consequenceEvi=conse;
  }

   
  public double comActWeight(double[] example) {
    double mul=1.0;
    for (int i=0;i<example.length;i++){
      if(antecedent[i]!=null){
        mul*=antecedent[i].Fuzzify(example[i]);
      }
    }
    return mul*Rweight;
  }

  public void setRweight(double weight){
    Rweight=weight;
  }
 
}


