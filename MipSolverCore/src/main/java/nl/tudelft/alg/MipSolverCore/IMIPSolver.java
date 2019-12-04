package nl.tudelft.alg.MipSolverCore;


public interface IMIPSolver extends ISolver {
	
	/**
	 * Sets the objective function
	 * @throws SolverException when an error occurs in setting the objective function
	 */
	public abstract void setObjectiveFunction() throws SolverException;

	/**
	 * Relax the integer variables, so that the model becomes a linear problem
	 * @param value set to true to relax the integer variables, or false to keep integer variables
	 * @throws SolverException when an exception occurs
	 */
	public abstract void setSolveAsLP(boolean value) throws SolverException;
	
	/**
	 * When the model is unbounded, this method returns the unbounded ray (after running solve)
	 * The unbounded ray is a vector that can be added to the variables indefinitely 
	 * and keeps improving the objective.
	 * @return the unbounded ray of an unbounded model
	 */
	public abstract double[] getUnboundedRay();

	/**
	 * The the log file
	 * @param mipLogFile the file name of the log file
	 * @throws SolverException when an exception occurs in setting the log file
	 */
	public abstract void setLogFile(String mipLogFile) throws SolverException;

}