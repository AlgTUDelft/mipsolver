package nl.tudelft.alg.MipSolverCore;


/**
 * A lagrangian relaxation model
 * @param <P> the problem class to be solved
 */
public abstract class LRModel<P extends IProblem> implements IModel {
	protected P problem;
	double timeLimit, subTimeLimit;
	
	public LRModel(P problem) {
		this.problem = problem;
		this.timeLimit = Double.MAX_VALUE;
		this.subTimeLimit = Double.MAX_VALUE;
	}
	
	/**
	 * @return the problem
	 */
	public P getProblem() {
		return problem;
	}
	
	@Override
	public void initialize(ISolver solver) {}
	
	@Override
	public boolean isSolvable() {
		return true;
	}

	/**
	 * @return the number of subproblems
	 */
	public abstract int getNSubproblems();
	
	/**
	 * Get sub problem number e
	 * @param e the number of the subproblem
	 * @param problem the lagrangian problem
	 * @return the mip model for the relaxed subproblem
	 */
	public abstract MIP getSubproblemModel(int e, LRProblem<P> problem);
	
	/**
	 * Get the master problem
	 * @param problem the lagrangian problem
	 * @return the mip model for the master problem
	 */
	public abstract MIP getMasterProblemModel(LRProblem<P> problem);
	
	/**
	 * @return Get the lagrangian problem description
	 */
	public abstract LRProblem<P> getLagrangianProblem();

	/**
	 * Set the time limits
	 * @param timeLimit The time limit for the total problem (in seconds).
	 * @param subTimeLimit The time limit for every subproblem (in seconds).
	 */
	public void setTimeLimit(double timeLimit, double subTimeLimit) {
		this.timeLimit = timeLimit;
		this.subTimeLimit = subTimeLimit;
	}	
	
	/**
	 * @return get the time limit for the complete program
	 */
	public double getTimeLimit() {
		return timeLimit;
	}
	
	/**
	 * @return get the time limit for the subproblem
	 */
	public double getSubTimeLimit() {
		return subTimeLimit;
	}

	/**
	 * Code to execute after running all the subproblems
	 */
	public void finishSubProblems() {}

}