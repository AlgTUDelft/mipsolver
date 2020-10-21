package nl.tudelft.alg.MipSolverCore;

/**
 * A lagrangian relaxation Solver
 * @param <P> the problem class to solve
 */
public class LRSolver<P extends IProblem> implements ISolver {
	IMIPSolver mipsolver;
	boolean minimize = true;
	boolean debug = false;
	double timeLimit = Double.MAX_VALUE;
	double subTimeLimit = Double.MAX_VALUE;
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
		model.initialize(mipsolver);
		mipsolver.build(model);
		mipsolver.setMipGap(mipgap);
		if(debug) mipsolver.save("mip.lp");
		return mipsolver.solve();
	}

	@Override
	public double solve() throws SolverException {
		long start = System.nanoTime();
		LRProblem<P> relax = instance.getLagrangianProblem();
		while(relax.checkend()) {
			for(int e = 0; e < instance.getNSubproblems(); e++) {
				double remaining = instance.getTimeLimit() - (System.nanoTime() - start) / 1e9;
				if(remaining <= 0) break;
				MIP model = instance.getSubproblemModel(e, relax);
				model.setTimeLimit(Math.min(remaining, instance.getSubTimeLimit()));
				mipBuildAndSolve(model);
			}
			instance.finishSubProblems();
			double remaining = instance.getTimeLimit() - (System.nanoTime() - start) / 1e9;
			if(remaining <= 0) break;
			relax.setlowerObj();
			relax.addTolowerObj();
			MIP model = instance.getMasterProblemModel(relax);
			model.setTimeLimit(Math.min(remaining, instance.getTimeLimit()));
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
