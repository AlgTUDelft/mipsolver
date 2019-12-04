package nl.tudelft.alg.MipSolverCore;


public enum CMP {
	SMALLEREQ("<="),
	EQ("="),
	LARGEREQ(">=");
	
	private String disp;
	
	private CMP(String disp) {
		this.disp = disp;
	}
	
	@Override
	public String toString() {
		return disp;
	}
}
