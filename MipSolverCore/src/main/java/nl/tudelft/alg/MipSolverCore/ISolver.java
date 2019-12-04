package nl.tudelft.alg.MipSolverCore;

public interface ISolver {
	
	/**
	 * Builds the model as defined in model
	 * @param model the model to be build
	 * @throws SolverException when the solver cannot build the model (for example a constraint contains a variable that is not defined)
	 */
	public void build(IModel model) throws SolverException;

	/**
	 * Set whether the objective should be minimized (default) or maximized
	 * @param value true iff the objective is to minimize
	 */
	public void setMinimize(boolean value);

	/**
	 * Set the time limit in seconds
	 * @param value the time limit in seconds
	 * @throws SolverException when the value specified is not valid
	 */
	public void setTimeLimit(double value) throws SolverException;

	/**
	 * Set the MIP gap, default is 1e-4
	 * @param value the mip gap
	 * @throws SolverException when the value specified is not valid
	 */
	public void setMipGap(double value) throws SolverException;

	/**
	 * Enables or disables debugging of the solver
	 * @param value
	 * @throws SolverException
	 */
	public void setDebug(boolean value) throws SolverException;

	/**
	 * Solves the problem
	 * @return the objective value
	 * @throws SolverException when an exception occurs during solving
	 */
	public double solve() throws SolverException;

	/**
	 * Save the model to a file
	 * @param file the file name to write the model to
	 * @throws SolverException when an exception occurs while saving the file
	 */
	public void save(String file) throws SolverException;

	/**
	 * Dispose the model
	 */
	public void dispose();
}
