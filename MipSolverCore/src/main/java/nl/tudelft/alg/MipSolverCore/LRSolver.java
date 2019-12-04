package nl.tudelft.alg.MipSolverCore;

/**
 * A lagrangian elaxation Solver
 * @param <P> the problem class to solve
 */
public class LRSolver<P extends IProblem> implements ISolver {
	IMIPSolver mipsolver;
	boolean minimize = true;
	boolean debug = false;
	double timeLimit = Double.MAX_VALUE;
	double mipgap = 1e-4;
	LRModel<P> instance;
	
	public LRSolver(IMIPSolver mipsolver) {
		this.mipsolver = mipsolver;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void build(IModel instance) {
		assert (instance instanceof LRModel<?>);
		this.instance = (LRModel<P>) instance;
	}

	@Override
	public void setMinimize(boolean value) {
		this.minimize = value;
	}

	@Override
	public void setTimeLimit(double value) {
		this.timeLimit = value;
	}

	@Override
	public void setMipGap(double value) {
		this.mipgap = value;
	}

	@Override
	public void setDebug(boolean value) {
		this.debug = value;
	}
	
	/**
	 * Build a mip model and solve it
	 * @param model the mip model to solve
	 * @return the objective value
	 * @throws SolverException when an exception occurs in building or solving the model
	 */
	protected double mipBuildAndSolve(MIP model) throws SolverException {
		model.initialize();
		mipsolver.build(model);
		mipsolver.setMipGap(mipgap);
		mipsolver.setTimeLimit(timeLimit);
		return mipsolver.solve();
	}

	@Override
	public double solve() throws SolverException {
		LRProblem<P> relax = instance.getLagrangianProblem();
		while(relax.checkend()) {
			for(int e = 0; e< instance.getNSubproblems(); e++) {
				MIP model = instance.getSubproblemModel(e, relax);
				mipBuildAndSolve(model);
			}
			relax.setlowerObj();
			relax.addTolowerObj();
			MIP model = instance.getMasterProblemModel(relax);
			mipBuildAndSolve(model);
			relax.updateMultipliers();
			relax.newiteration();
		}
		return relax.getBestSolution();
	}

	@Override
	public void save(String file) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		mipsolver.dispose();
	}

}
