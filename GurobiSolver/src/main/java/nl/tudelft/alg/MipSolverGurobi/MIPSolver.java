package nl.tudelft.alg.MipSolverGurobi;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBExpr;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBQuadExpr;
import gurobi.GRBVar;
import nl.tudelft.alg.MipSolverCore.CMP;
import nl.tudelft.alg.MipSolverCore.Constraint;
import nl.tudelft.alg.MipSolverCore.Exp;
import nl.tudelft.alg.MipSolverCore.IMIPSolver;
import nl.tudelft.alg.MipSolverCore.IModel;
import nl.tudelft.alg.MipSolverCore.InfeasibleException;
import nl.tudelft.alg.MipSolverCore.LinExp;
import nl.tudelft.alg.MipSolverCore.MIP;
import nl.tudelft.alg.MipSolverCore.QuadExp;
import nl.tudelft.alg.MipSolverCore.SolverException;
import nl.tudelft.alg.MipSolverCore.VarType;
import nl.tudelft.alg.MipSolverCore.Variable;
import nl.tudelft.alg.MipSolverCore.VariablePair;

public class MIPSolver implements IMIPSolver {
	static GRBEnv env;
	static final boolean DEBUG = true;
	GRBModel model;
	Map<Variable, GRBVar> varMap;
	MIP mipInstance;
	boolean minimize = true;
	boolean solveAsLP = false;
	double mipgap = 1e-4;
	
	public MIPSolver() throws SolverException {
    	if(env == null) {
			try {
				env = new GRBEnv("mip1.log");
				resetParams();
			} catch (GRBException e) {
				throw new SolverException(e);
			}
		}
    }
    
	/**
	 * Reset the Gurobi environment parameters
	 */
	private static void resetParams() throws GRBException {
		env.resetParams();
		env.set(GRB.IntParam.LogToConsole, DEBUG ? 1 : 0);
		env.set(GRB.IntParam.InfUnbdInfo, DEBUG ? 1 : 0);
    }
    

	@Override
	public void build(IModel mipInstance) throws SolverException {
		assert (mipInstance instanceof MIP);
		this.mipInstance = (MIP) mipInstance;
		try {
			if(model!=null) model.dispose();
			model = new GRBModel(env);
			model.set(GRB.StringAttr.ModelName, this.mipInstance.getName());
			model.set(GRB.DoubleParam.MIPGap, mipgap);
			model.set(GRB.DoubleParam.TimeLimit, this.mipInstance.getTimeLimit());
			
			addVariables();
	
			setObjectiveFunction();
	
			// Add constraints
			for(Constraint c: this.mipInstance.getConstraints()) {
				addConstraint(model, c);
			}
			for(Variable[] sos: this.mipInstance.getSOSs()) {
				double[] w = IntStream.range(0, sos.length).mapToDouble(i -> i).toArray();
				model.addSOS(getVars(sos), w, GRB.SOS_TYPE2);
			}
						
		} catch (GRBException e) {
			throw new SolverException("Gurobi error " + e.getErrorCode() + ": " + e.getMessage(), e);

		}
	}
	
	/**
	 * Add the variables to the gurobi model from the mip model
	 */
	private void addVariables() throws GRBException {
		// Create variables
		varMap = new HashMap<Variable, GRBVar>();
		for(Variable v: mipInstance.getVars()) {
			GRBVar var;
			if (v.getType() == VarType.PositiveContinuous)
				var = model.addVar(0, Double.POSITIVE_INFINITY, 0.0, GRB.CONTINUOUS, v.getName());
			else if (v.getType() == VarType.NegativeContinuous)
				var = model.addVar(Double.NEGATIVE_INFINITY, 0.0, 0.0, GRB.CONTINUOUS, v.getName());
			else if (v.getType() == VarType.Binary && !solveAsLP)
				var = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, v.getName());
			else if (v.getType() == VarType.BinaryContinuous || (v.getType() == VarType.Binary && solveAsLP))
				var = model.addVar(0.0, 1.0, 0.0, GRB.CONTINUOUS, v.getName());
			else
				var = model.addVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0, GRB.CONTINUOUS, v.getName());
			varMap.put(v, var);
		}
		// Integrate new variables
		model.update();
	}


	@Override
	public void setObjectiveFunction() throws SolverException {
		GRBExpr obj = createExpr(mipInstance.getObjectiveFunction());
		try {
			if(minimize)
				model.setObjective(obj, GRB.MINIMIZE);
			else
				model.setObjective(obj, GRB.MAXIMIZE);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public void setDebug(boolean value) throws SolverException {
		try {
			env.set(GRB.IntParam.LogToConsole, value ? 1 : 0);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public void setMipGap(double value) throws SolverException {
		mipgap = value;
		try {
			model.getEnv().set(GRB.DoubleParam.MIPGap, value);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public void setMinimize(boolean value) {
		minimize = value;
	}
	
	@Override
	public void setSolveAsLP(boolean value) throws SolverException {
		solveAsLP = value;
		try {
			if(value) {
				env.set(GRB.DoubleParam.Heuristics, 0.0);
				env.set(GRB.IntParam.Cuts, 0);
				env.set(GRB.IntParam.Presolve, 0);
			} else {
				resetParams();
			}
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}
	
	/**
	 * Turn on the Gurobi feature to get extra output when a model is infeasible or unbounded
	 * @param value true to turn on, or false to turn off
	 */
	public static void setUnboundInfo(boolean value) throws SolverException {
		try {
			env.set(GRB.IntParam.InfUnbdInfo, value ? 1 : 0);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public double[] getUnboundedRay() {
		try {
			return model.get(GRB.DoubleAttr.UnbdRay, model.getVars());
		} catch (GRBException e) {
			return null;
		}
	}
	
	/**
	 * Print the unbounded variables
	 * @throws SolverException when an exception occurs in Gurobi
	 */
	private void debugUnboundedVariables() throws SolverException {
		double[] r = getUnboundedRay();
		if(r == null) {
			if (DEBUG) System.out.println("Failed to obtain unbounded ray");
			return;
		}
		GRBVar[] vs = model.getVars();
		for(int i=0; i<vs.length; i++) {
			if(Math.abs(r[i]) > 1e-3) {
				try {
					System.out.println(vs[i].get(GRB.StringAttr.VarName));
				} catch (GRBException e) {
					throw new SolverException(e);
				}
			}
		}
	}

	@Override
	public double solve() throws SolverException, InfeasibleException {
		try {
			model.optimize();
			int optimstatus = model.get(GRB.IntAttr.Status);
			if (optimstatus == GRB.Status.INF_OR_UNBD) {
				throw new InfeasibleException("Model is infeasible or unbounded");
			} else if (optimstatus == GRB.Status.INFEASIBLE) {
				throw new InfeasibleException("Model is infeasible");
			} else if (optimstatus == GRB.Status.UNBOUNDED) {
				if (DEBUG) {
					System.out.println("Model is unbounded");
					debugUnboundedVariables();
				}
				if (model.get(GRB.IntParam.PreDual) == 0 && model.get(GRB.IntParam.DualReductions) == 0)
					throw new InfeasibleException("Model is unbounded");
				model.set(GRB.IntParam.PreDual, 0);
				model.set(GRB.IntParam.DualReductions, 0);
				return solve();
			}
			if (optimstatus == GRB.Status.TIME_LIMIT) {
				System.out.println("Time limit reached with mip gap " + model.get(GRB.DoubleAttr.MIPGap));
			}
			try {
				mipInstance.setMipGap(model.get(GRB.DoubleAttr.MIPGap));
			} catch (GRBException e) {} //PASS model contains no integer variables, and therefore has no MIPGap
			for (Variable v : mipInstance.getVars()) {
				v.setSolution(varMap.get(v).get(GRB.DoubleAttr.X));
			}
			mipInstance.writeSolution();
			return model.get(GRB.DoubleAttr.ObjVal);
		} catch (GRBException e) {
			throw new SolverException("Exception in solving the model. Gurobi error " + e.getErrorCode() + ": " + e.getMessage(), e);
		} catch (InfeasibleException e) {
			debugModel(e);
			throw e;
		}
	}

	/**
	 * Debug the model
	 * @param e the infeasibility exception that occurred during solving the model
	 * @throws SolverException when an exception occurs in gurobi
	 */
	private void debugModel(InfeasibleException e) throws SolverException {
		System.out.println("Exception in solving the model: " + e.getMessage());
		e.printStackTrace();

		try {
			model.write("debug.lp");
			model.computeIIS();
			for (GRBConstr c : model.getConstrs()) {
				if (c.get(GRB.IntAttr.IISConstr) > 0) {
					System.out.println(c.get(GRB.StringAttr.ConstrName));
				}
			}

			// Print the names of all of the variables in the IIS set.
			for (GRBVar v : model.getVars()) {
				if (v.get(GRB.IntAttr.IISLB) > 0 || v.get(GRB.IntAttr.IISUB) > 0) {
					System.out.println(v.get(GRB.StringAttr.VarName));
				}
			}
		} catch (GRBException e1) {
			throw new SolverException(
					"Error in showing debug information Gurobi error "
							+ e1.getErrorCode() + ": " + e1.getMessage(),
					e1);
		}
		
	}
	
	/**
	 * Add a constraint to the gurobi model
	 * @param m the gurobi model
	 * @param c the constraint to be added
	 * @return the resulting gurobi constraint
	 * @throws SolverException when an exception occurs in Gurobi, 
	 * or when a variable in a constraint does not exist
	 */
	private GRBConstr addConstraint(GRBModel m, Constraint c) throws SolverException {
		try {
			return m.addConstr(createLinExpr(c.getLeft()), GRBcomparator(c.getComparator()), createLinExpr(c.getRight()), c.getName());
		} catch(GRBException e) {
			throw new SolverException("Exception in adding constraint " + c, e);
		}
	}
	
	/**
	 * Create a Gurobi expression
	 * @param exp the expression to transform into a gurobi expression
	 * @return the gurobi expression
	 * @throws SolverException when an exception occurs in gurobi
	 */
	private GRBExpr createExpr(Exp exp) throws SolverException {
		if(exp instanceof LinExp) {
			return createLinExpr((LinExp) exp);
		} 
		return createQuadExpr((QuadExp) exp);
	}
	
	/**
	 * Create a Gurobi linear expression
	 * @param exp the linear expression to transform into a gurobi linear expression
	 * @return the gurobi linear expression
	 * @throws SolverException when an exception occurs in gurobi
	 */
	private GRBLinExpr createLinExpr(LinExp exp) throws SolverException {
		GRBLinExpr grbLinExpr = new GRBLinExpr();
		for(Variable v: exp.getVariables()) {
			if(varMap.containsKey(v))
				grbLinExpr.addTerm(exp.get(v), varMap.get(v));
			else if(v == Variable.CONST)
				grbLinExpr.addConstant(exp.get(v));
			else
				throw new SolverException("adding unknown variable " + v.getName() + " in " + exp.toString());
		}	
		return grbLinExpr;
	}
	
	/**
	 * Get the gurobi variable
	 * @param v the variable to get
	 * @return the gurobi variable
	 */
	private GRBVar getVar(Variable v) {
		if(varMap.containsKey(v))
			return varMap.get(v);
		return null;
	}
	
	/**
	 * Get gurobi variables
	 * @param v the variables to get
	 * @return the gurobi variables
	 */
	private GRBVar[] getVars(Variable[] vs) {
		return Arrays.stream(vs).map(v -> getVar(v)).toArray(GRBVar[]::new);
	}
	
	/**
	 * Create a Gurobi quadratic expression
	 * @param exp the quadratic expression to transform into a gurobi quadratic expression
	 * @return the gurobi quadratic expression
	 * @throws SolverException when an exception occurs in gurobi
	 */
	private GRBQuadExpr createQuadExpr(QuadExp exp) {
		GRBQuadExpr grbQuadExpr = new GRBQuadExpr();
		for(VariablePair v: exp.getVariablePairs()) {
			if(!v.isConstant()) {
				GRBVar[] vars = v.getVariables().stream().map(w -> varMap.get(w)).toArray(GRBVar[]::new);
				if(vars.length == 1)
					grbQuadExpr.addTerm(exp.get(v), vars[0]);
				else
					grbQuadExpr.addTerm(exp.get(v), vars[0], vars[1]);
			} else 
				grbQuadExpr.addConstant(exp.get(v));
		}	
		return grbQuadExpr;
	}
	
	/**
	 * Get the Gurobi comparator
	 * @param cmp the comparator
	 * @return the gurobi comparator
	 */
	private char GRBcomparator(CMP cmp) {
		switch(cmp) {
		case SMALLEREQ: return GRB.LESS_EQUAL;
		case LARGEREQ: return GRB.GREATER_EQUAL;
		default: return GRB.EQUAL;
		}
	}
	
	/**
	 * Get the gurobi model
	 */
	public GRBModel getModel() {
		return model;
	}
	
	/**
	 * Get the gurobi environment
	 */
	public static GRBEnv getEnv() {
		return env;
	}
	
	@Override
	public void save(String file) throws SolverException {
		try {
			model.update();
			model.write(file);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public void dispose() {
		if(model!= null)
			model.dispose();
	}

	@Override
	public void setLogFile(String mipLogFile) throws SolverException {
		try {
   		if(env != null) 
   			env.set(GRB.StringParam.LogFile, mipLogFile);
   		else {
   			env = new GRBEnv(mipLogFile);
   			resetParams();
   		}
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}
	
}
