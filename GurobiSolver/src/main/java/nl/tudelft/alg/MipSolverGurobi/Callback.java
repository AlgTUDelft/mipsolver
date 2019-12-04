package nl.tudelft.alg.MipSolverGurobi;


import gurobi.GRB;
import gurobi.GRBCallback;
import gurobi.GRBException;

public class Callback extends GRBCallback {
	double objBound;
	
	public Callback(double objBound) {
		this.objBound = objBound;
	}

	@Override
	protected void callback() {
		try {
			if (where == GRB.CB_MIPSOL) {
				double  obj = getDoubleInfo(GRB.CB_MIPSOL_OBJ);
				if (obj <= objBound) 
					abort();
	        }
		} catch (GRBException e) {
			System.out.println("Error during callback");
			e.printStackTrace();
		}
	}	     
}
