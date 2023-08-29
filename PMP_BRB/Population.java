package keel.Algorithms.Fuzzy_Rule_Learning.AdHoc.PMP_BRB;

import java.util.Random;

public class Population {
    public int NP;
    public double F;
    public double CR;
    int maxNumR=0;

    public myDataset train;
    public myDataset test;

    private Individual[] individuals;
    private Individual[] indMutation;
    private Individual[] indCrossover;

    private double[] fitness=new double[NP];

    public double[] getFitness(){
        return fitness;
    }
    public Individual[] getIndividuals(){
        return individuals;
    }
    public Individual[] getindMutation(){
        return indMutation;
    }
    public Individual[] getindCrossover(){
        return indCrossover;
    }
    public void setindividuals(Individual[] ind){
        this.individuals=ind;
    }
    public void setindMutation(Individual[] ind){
        this.indMutation=ind;
    }
    public void setindCrossover(Individual[] ind){
        this.indCrossover=ind;
    }
    public void setFitness(double [] fit){
        this.fitness=fit;
    }

    public Population(myDataset train, myDataset test, int minR, int maxR, int np, double f,double cr){
        Random r=new Random();
        this.train=train;
        this.test=test;
        this.NP=np;
        this.F=f;
        this.CR=cr;
        individuals=new Individual[NP];
        indMutation=new Individual[NP];
        indCrossover=new Individual[NP];
        fitness=new double[NP];
        maxNumR=maxR;
        for(int i=0;i<NP;i++){
            int nR=minR+r.nextInt(maxR-minR);
            individuals[i]=new Individual(nR,maxR,minR);
            // fitness[i]=individuals[i].calFitness(test);
        }
        for(int i=0;i<NP;i++){
            individuals[i].generate(train, maxNumR);
            fitness[i]=individuals[i].calFitness(test);
        }
    }

    public void Mutation(){
        Random r=new Random();
        for(int i=0;i<NP;i++){
            int r1 = 0, r2 = 0, r3 = 0;
			while (r1 == i || r2 == i || r3 == i || r1 == r2 || r1 == r3
					|| r2 == r3) {// 取r1,r2,r3
				r1 = r.nextInt(NP);
				r2 = r.nextInt(NP);
				r3 = r.nextInt(NP);
			}
            this.indMutation[i]=this.getIndividuals()[r1].Mutation(getIndividuals()[r2], getIndividuals()[r3], F);
        }
    }

    public void Crossover(){
        Random r=new Random();
        for(int i=0;i<NP;i++){
            int r1=0, r2=0;
            while(r1==r2){
                r1=r.nextInt(NP);
                r2=r.nextInt(NP);
            }
            this.indCrossover[i]=this.getindMutation()[r1].Crossover(this.getIndividuals()[r2],CR);
        }
    }

    public void Selection(){
        Individual[] temindividuals=new Individual[NP];
        Individual[] newindCrossover=new Individual[NP];
        double[] fitness=new double[NP];

        double[] crossOverFit=new double[NP];

        temindividuals=this.getIndividuals();
        newindCrossover=this.getindCrossover();
        fitness=this.getFitness();
        Random r=new Random();
        for(int i=0;i<NP;i++){
            int r1=r.nextInt(NP);
            crossOverFit[r1]=newindCrossover[r1].calFitness(test);
            if(crossOverFit[r1]<fitness[i]){
                temindividuals[i]=newindCrossover[r1];
                fitness[i]=crossOverFit[r1];
            }
        }
        this.setindividuals(temindividuals);
        this.setFitness(fitness);
    }

    public Individual saveBest(){
        double [] fit=new double[NP];
        fit=this.getFitness();
        int min=-1;
        double minfit=2;
        for(int i=0;i<NP;i++){
            if(minfit>fit[i]){
                min=i;
                minfit=fit[min];
            }
        }
        return this.getIndividuals()[min];
    }
}
