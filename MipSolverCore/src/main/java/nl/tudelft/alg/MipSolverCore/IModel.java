package nl.tudelft.alg.MipSolverCore;

public interface IModel {
	
	/**
	 * Prints the solution
	 */
	void printSolution();

	/**
	 * Initializes the model. Should be called before the solve method is called
	 * @param solver The solver to use if subproblems needs to be solved
	 * @throws SolverException when an error occurred in model initialization
	 */
	void initialize(ISolver solver) throws SolverException;
	
	/**
	 * @return whether this model can solve the given problem given its problem configuration
	 */
	boolean isSolvable();
}
