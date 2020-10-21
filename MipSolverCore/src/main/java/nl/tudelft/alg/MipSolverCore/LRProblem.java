package nl.tudelft.alg.MipSolverCore;

public abstract class LRProblem<P extends IProblem> {
	protected P problem;
	LRModel<P> instance;
	protected int maxIter,
		iteration,
		bestIter,
		counter1,counter2;
	protected double maxStep,
			bestObj,
			lowerObj,
			upperObj,
			idealGAP,
			parB, parC, parD, parF, parFi;
	protected double[] gap,upobject,lowobject;
	double[] lowerObjPerSubproblem;
	
	public LRProblem(LRModel<P> instance) {
		this.instance = instance;
		this.problem = instance.getProblem();
		InitializeGradientParameters(); 
		lowerObjPerSubproblem = new double[instance.getNSubproblems()];
		
	}
	
	public void InitializeGradientParameters() {
		maxIter = 6;
		maxStep = 1;
		iteration = 0;
		counter1 = 0;
		counter2 = 0;
		idealGAP = 0.01;
		
		gap=new double[maxIter];
		upobject=new double[maxIter];
		lowobject=new double[maxIter];
		
		parFi = 0.25;
		parB = 2;
		parC = 15;
		parD = 2;
		parF = 2;
	}

	public boolean checkend() {
		if (iteration == 0) return true;
		this.gap[iteration-1] = Math.abs((bestObj-lowerObj)/bestObj);
		System.out.println(String.format("%d/%d:\t%f\t%f\t%f", iteration, maxIter, bestObj, lowerObj, this.gap[iteration-1]));
		return !(gap[iteration-1] < idealGAP || iteration+1 > maxIter);
	}

	public void setlowerObj() {
		lowerObj = 0;
		for(int e = 0; e<instance.getNSubproblems(); e++) 
			lowerObj += lowerObjPerSubproblem[e];
	}

	public abstract void addTolowerObj();

	public abstract void updateMultipliers();

	public boolean checkBesttoPrint() {
		return (bestIter == iteration);
	}

	public void newiteration() {
		lowobject[iteration]=lowerObj;
		upobject[iteration]=upperObj;
		this.iteration +=1;	
	}

	public double getBestSolution() {
		return bestObj;
	}
	
	public void setBestSolution(double sol) {
		this.bestObj = sol;
		this.bestIter = iteration;
	}
	
	
	public void setUPObj(double sol) {
		this.upperObj = sol;
	}
	
	public int getIteration() {
		return iteration;
	}
	
	public int getMaxIterations() {
		return maxIter;
	}
	
	public void setLBObjPerSubproblem(int e, double sol) {
		this.lowerObjPerSubproblem[e] = sol;
	}
}
