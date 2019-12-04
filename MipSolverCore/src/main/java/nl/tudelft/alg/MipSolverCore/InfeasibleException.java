package nl.tudelft.alg.MipSolverCore;

public class InfeasibleException extends SolverException {
	private static final long serialVersionUID = 1538847419846737418L;

	public InfeasibleException() {
		super("MIP problem is not feasible.");
	}

	public InfeasibleException(String message) {
		super(message);
	}

}
