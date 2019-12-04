package nl.tudelft.alg.MipSolverCore;

/**
 * A lagrangian relaxation model
 * @param <P> the problem class to be solved
 */
public abstract class LRModel<P extends IProblem> implements IModel {
	protected P problem;
	
	public LRModel(P problem) {
		this.problem = problem;
	}
	
	/**
	 * @return the problem
	 */
	public P getProblem() {
		return problem;
	}
	
	@Override
	public void initialize() {}
	
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

}