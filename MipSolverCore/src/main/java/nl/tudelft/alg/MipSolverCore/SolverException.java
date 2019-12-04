package nl.tudelft.alg.MipSolverCore;

public class SolverException extends Exception {
	private static final long serialVersionUID = 1621613048753782969L;

	public SolverException() {
		super("An exception occured in running the solver");
	}

	public SolverException(String message) {
		super(message);
	}

	public SolverException(Exception e) {
		super(e);
	}

	public SolverException(String message, Exception e) {
		super(message, e);
	}

}
