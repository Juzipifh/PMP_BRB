package keel.Algorithms.Fuzzy_Rule_Learning.AdHoc.PMP_BRB;

import java.util.Random;

public class Individual {
    double [][] RefValues;
    double [][] ruleConsequents;
    double [] ruleWeights;
    double fitness;
    int numRules;
    int maxNumRules;
    int minNumRules;
    RuleBase ruleBase;

    myDataset train;

    public Individual(){

    }

    public Individual(myDataset train, double [][] RefValues, double [][] ruleConsequents,double [] ruleWeights, int numRules){
        this.RefValues=RefValues;
        this.ruleConsequents=ruleConsequents;
        this.ruleWeights=ruleWeights;
        this.train=train;
        this.numRules=numRules;
        ruleBase=new RuleBase(RefValues, ruleWeights, ruleConsequents, train.getNames(), train.getClasses(),this.numRules);
    }

    public Individual(int numRules, int maxR,int minR){
        this.maxNumRules=maxR;
        this.minNumRules=minR;
        this.numRules=numRules;
    }

    public void generate(myDataset train, int maxNumR){
        Random r=new Random();
        this.train=train;
        int n_variables=train.getnInputs();
        this.RefValues=new double[maxNumR][n_variables];
        for (int i=0;i<n_variables;i++){
            double range=train.getRanges()[i][1]-train.getRanges()[i][0];
            for(int j=0;j<maxNumR;j++){
                RefValues[j][i]=train.getRanges()[i][0]+r.nextDouble()*range;
            }
        }
        for(int i=0;i<this.RefValues[0].length;i++){ // Ensure the ranges in the rules
            double[] line=new double[RefValues.length];
            for(int d=0;d<RefValues.length;d++){
                line[d]=RefValues[d][i];
            }
            int p,q;
            p=findA(train.getRanges()[i][0],line);
            q=findA(train.getRanges()[i][1],line);
            if(p==-1){
                p=r.nextInt(numRules);
                RefValues[p][i]=train.getRanges()[i][0];
            }
            if(q==-1){
                q=r.nextInt(numRules);
                while(q==p){
                    q=r.nextInt(numRules);
                }
                RefValues[q][i]=train.getRanges()[i][1];
            }
       }

        this.ruleWeights=new double [maxNumR];
        this.ruleConsequents=new double [maxNumR][train.getnClasses()];

        for(int i=0;i<maxNumR;i++){
            ruleWeights[i]=r.nextDouble();
            double[] ruleCon=new double[train.getnClasses()];
            for(int j=0;j<ruleCon.length;j++){
                ruleCon[j]=r.nextDouble();
            }
            if(sum(ruleCon)>1){
                for(int j=0;j<ruleCon.length;j++){
                    ruleConsequents[i][j]=ruleCon[j]/sum(ruleCon);
                }
            }else{
                ruleConsequents[i]=ruleCon.clone();
            }
        }
        ruleBase=new RuleBase(RefValues,ruleWeights,ruleConsequents,train.getNames(),train.getClasses(),numRules);
    }

    public double max(double[] X){
        double max=-1;
        for(int i=0;i<X.length;i++){
            if(X[i]>max){
                max=X[i];
            }
        }
        return max;
    }
    public double sum(double[] X){
        double sum=0;
        for(int i=0;i<X.length;i++){
            sum+=X[i];
        }
        return sum;
    }

    public double MSE(myDataset test){
        int hits=0;
        for( int i =0;i<test.getnData();i++){
            String output = new String("?");
            int classOut = ruleBase.FRM(test.getExample(i));
            if (classOut >= 0) {
                output = train.getOutputValue(classOut);
            }
            if (test.getOutputAsString(i).equalsIgnoreCase(output)){
                hits++;
            }
        }
        double mes=1-1.0*hits/test.size();
        return mes;
    }

    public Individual Mutation(Individual ind1,Individual ind2,double F){
        Random r=new Random();
        int newNumRules=(int)(this.numRules+F*(ind1.numRules-ind2.numRules));
        if(newNumRules>maxNumRules||newNumRules<minNumRules){
            newNumRules=minNumRules+r.nextInt(maxNumRules-minNumRules);
        }
        double [][] newRefValues=new double[RefValues.length][RefValues[0].length];
        for (int i=0;i<RefValues.length;i++){
            for(int j=0;j<RefValues[0].length;j++){
                double newx=0;
                newx=this.RefValues[i][j]+F*(ind1.RefValues[i][j]-ind2.RefValues[i][j]);
                if(newx<train.getRanges()[j][0] || newx>train.getRanges()[j][1]){
                    newx=train.getRanges()[j][0]+r.nextDouble()*(train.getRanges()[j][1]-train.getRanges()[j][0]);
                }
                newRefValues[i][j]=newx;
            }
        }
        for(int i=0;i<newRefValues[0].length;i++){
            double[] line=new double[newNumRules];
            for(int d=0;d<newNumRules;d++){
                line[d]=newRefValues[d][i];
            }
            int p,q;
            p=findA(train.getRanges()[i][0],line);
            q=findA(train.getRanges()[i][1],line);
            if(p==-1){
                p=r.nextInt(newNumRules);
                newRefValues[p][i]=train.getRanges()[i][0];
            }
            if(q==-1){
                q=r.nextInt(newNumRules);
                while(q==p){
                    q=r.nextInt(newNumRules);
                }
                newRefValues[q][i]=train.getRanges()[i][1];
            }
       }

        double [][] newruleConsequents=new double[this.ruleConsequents.length][ruleConsequents[0].length];
        double [] newruleWeights=new double[ruleWeights.length];
        for(int i=0;i<this.ruleConsequents.length;i++){
            double [] consequence=new double[ruleConsequents[0].length];
            for(int j=0;j<this.ruleConsequents[0].length;j++){
                double newx=0;
                newx=this.ruleConsequents[i][j]+F*(ind1.ruleConsequents[i][j]-ind2.ruleConsequents[i][j]);
                if(newx<0|| newx>1){
                    newx=r.nextDouble();
                }
                consequence[j]=newx;
            }
            if(sum(consequence)>1){
                for(int j=0;j<this.ruleConsequents[0].length;j++){
                    newruleConsequents[i][j]=consequence[j]/sum(consequence);
                }
            }else{
                newruleConsequents[i]=consequence.clone();
            }
            double newx=0;
            newx=this.ruleWeights[i]+F*(ind1.ruleWeights[i]-ind2.ruleWeights[i]);
            if(newx<0||newx>1){
                newx=r.nextDouble();
            }
            newruleWeights[i]=newx;   
        }
        Individual newInd=new Individual(this.train, newRefValues, newruleConsequents, newruleWeights,newNumRules);
        newInd.maxNumRules=this.maxNumRules;
        newInd.minNumRules=this.minNumRules;
        return newInd;
    }

    public int findA(double a, double[] line){
        int find=-1;
        for(int i=0;i<line.length;i++){
            if(line[i]==a){
                find=i;
                break;
            }
        }
        return find;
    }

    public Individual Crossover(Individual ind1, double CR){
        Random r=new Random();

        int newNumRules;
        if(r.nextDouble()<CR){
            newNumRules=ind1.numRules;
        }else{
            newNumRules=this.numRules;
        }

        double [][] newRefValues=new double[RefValues.length][RefValues[0].length];
        for (int i=0;i<RefValues.length;i++){
            for(int j=0;j<RefValues[0].length;j++){
                if(r.nextDouble()<CR)
                    newRefValues[i][j]=ind1.RefValues[i][j];
                else{
                    newRefValues[i][j]=this.RefValues[i][j];
                }
            }
        }
        for(int i=0;i<this.RefValues[0].length;i++){
            double[] line=new double[newNumRules];
            for(int d=0;d<newNumRules;d++){
                line[d]=newRefValues[d][i];
            }
            int p,q;
            p=findA(train.getRanges()[i][0],line);
            q=findA(train.getRanges()[i][1],line);
            if(p==-1){
                p=r.nextInt(newNumRules);
                newRefValues[p][i]=train.getRanges()[i][0];
            }
            if(q==-1){
                q=r.nextInt(newNumRules);
                while(q==p){
                    q=r.nextInt(newNumRules);
                }
                newRefValues[q][i]=train.getRanges()[i][1];
            }
       }

        double [][] newruleConsequents=new double[this.ruleConsequents.length][ruleConsequents[0].length];
        double [] newruleWeights=new double[ruleWeights.length];
        for(int i=0;i<this.ruleConsequents.length;i++){
            double [] consequence=new double[ruleConsequents[0].length];
            for(int j=0;j<this.ruleConsequents[0].length;j++){
                if(r.nextDouble()<CR)
                    consequence[j]=ind1.ruleConsequents[i][j];
                else{
                    consequence[j]=this.ruleConsequents[i][j];
                }
            }
            if(sum(consequence)>1){
                for(int j=0;j<this.ruleConsequents[0].length;j++){
                    newruleConsequents[i][j]=consequence[j]/sum(consequence);
                }
            }else{
                newruleConsequents[i]=consequence.clone();
            }
            if(r.nextDouble()<CR)
                newruleWeights[i]=ind1.ruleWeights[i];
            else{
                newruleWeights[i]=this.ruleWeights[i];
            }
        }
        Individual newInd=new Individual(this.train, newRefValues, newruleConsequents, newruleWeights,newNumRules);
        newInd.maxNumRules=this.maxNumRules;
        newInd.minNumRules=this.minNumRules;
        return newInd;
    }

    public void setFitness(double fit){
        this.fitness=fit;
    }

    public double calFitness(myDataset train){
        this.fitness=this.MSE(train);
        return this.fitness;
    }

    public double getFitness(){
        return this.fitness;
    }
    
}
