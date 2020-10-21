package nl.tudelft.alg.MipSolverCore;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public abstract class MIP implements IModel {
	protected List<Variable> vars;
	protected List<Constraint> constraints;
	protected List<Variable[]> soss;
	protected Exp objectiveFunction;
	protected double minimum = 0;
	double mipgap = 1e-6;
	double timeLimit = Double.MAX_VALUE;
	protected ISolver solver;
	
	public MIP() {
		constraints = new ArrayList<Constraint>();
		vars = new ArrayList<Variable>();
		soss = new ArrayList<Variable[]>();
		objectiveFunction = new LinExp();
	}
	
	@Override
	public void initialize(ISolver solver) {
		this.solver = solver;
		initiliazeVars();
		setVars();
		setConstraints();
		setObjectiveFunction();
	}
	
	/**
	 * Set the constraints
	 */
	protected abstract void setConstraints();

	/**
	 * Set the objective function
	 */
	protected abstract void setObjectiveFunction();

	/**
	 * Set the variables
	 */
	protected abstract void setVars();

	/**
	 * Initialize the variables
	 */
	protected abstract void initiliazeVars();

	/**
	 * Write the model solution back to the problem instance
	 * @throws SolverException when an error occurs in writing the solution
	 */
	public abstract void writeSolution() throws SolverException;
	
	/**
	 * Add a constraint to the model
	 * @param c the constraint to add
	 */
	public void addConstraint(Constraint c) {
		constraints.add(c);
	}
	
	/**
	 * Add a constraint to the model
	 * @param left the left hand side expression of the constraint
	 * @param right the right hand side expression of the constraint
	 * @param cmp the comparator (<=, =, >=) of the constraint
	 * @param name the name of the constraint
	 */
	public void addConstraint(LinExp left, LinExp right, CMP cmp, String name) {
		constraints.add(new Constraint(left, right, cmp, name));
	}
	
	public List<Constraint> getConstraints() {
		return constraints;
	}
	
	public List<Variable[]> getSOSs() {
		return soss;
	}

	public Exp getObjectiveFunction() {
		return objectiveFunction;
	}

	public List<Variable> getVars() {
		return vars;
	}
	
	public double getMinimum() {
		return minimum;
	}
	
	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}
	
	public void setMipGap(double mipgap) {
		this.mipgap = mipgap;
	}
	
	public double getMipGap() {
		return this.mipgap;
	}
	
	public void setTimeLimit(double timeLimit) {
		this.timeLimit = timeLimit;
	}
	
	public double getTimeLimit() {
		return timeLimit;
	}
	
	/**
	 * Initializes a Variable array with name name, type vType, and with dimensions dims
	 * @param name the base name of all the variables in the array
	 * @param vType the type of the variables
	 * @param dims the dimensions of the resulting array
	 * @return a Variable array with dimensions dims, of type vType with base name name
	 */
	public Object newVarArray(String name, VarType vType, int... dims) {
		if(dims.length == 0)
			return new Variable(name,vType);
		Object array = Array.newInstance(Variable.class, dims);
		initVarArray((Object[]) array, name, vType);
		return array;
	}
	
	/**
	 * Initializes a Variable array, with type vType, and beginning with name name
	 * @param ar the Variable array to initialize
	 * @param name the base name of all the variables in the array
	 * @param vType the type of the variables
	 */
	protected void initVarArray(Object[] ar, String name, VarType vType) {
		for(int i=0; i<ar.length; i++) {
			if(ar[i] != null && ar[i].getClass().isArray())
				initVarArray((Object[]) ar[i], name+"_"+i, vType);
			else
				ar[i] = new Variable(name+"_"+i, vType);
		}
	}
	
	/**
	 * Add constrains to the MIP model to fix a list of variables
	 * @param value the value to fix the variables to
	 * @param ar a Variable or a Variable array, or multiple Variable arrays
	 */
	public void fixVariables(double value, Object... ar) {
		if(ar.length == 1 && ar[0].getClass().isArray())
			ar = (Object[]) ar[0];
		for(Object a: ar) {
			if(a != null && a.getClass().isArray())
				fixVariables(value, a);
			else if(a instanceof Variable)
				fixVariable(a, value);
		}
	}
	
	/**
	 * Add constrains to the MIP model to fix a list of variables
	 * @param var the Variable, or Variable array to fix
	 * @param val the values, or value array to fix the variable to
	 */
	public void fixVariable(Object var, Object val) {
		fixVariable(var,val, CMP.EQ);
	}
	
	/**
	 * Add constrains to the MIP model to fix a list of variables
	 * @param var the Variable, or Variable array to fix
	 * @param val the values, or value array to fix the variable to
	 * @param cmp the comparator
	 */
	public void fixVariable(Object var, Object val, CMP cmp) {
		if(var.getClass().isArray()) {
			Object[] vars = (Object[]) var;
			for(int i=0; i<vars.length; i++)
				fixVariable(vars[i], 
					val.getClass().isArray() ? Array.get(val, i): val, cmp);
		} else if(var instanceof Variable) {
			addConstraint(new LinExp(var), new LinExp(val), cmp, "fix_"+((Variable) var).getName());
		} else {
			assert false;
		}
	}
	
	/**
	 * Add a Variable, or list of Variable (arrays) to the model
	 * @param vs the Variable, or list of Variable (arrays) to add
	 */
	public void addVars(Object... vs) {
		if(vs.length == 1 && vs[0].getClass().isArray())
			vs = (Object[]) vs[0];
		for(Object v: vs) {
			if(v != null && v.getClass().isArray())
				addVars(v);
			else if(v instanceof Variable)
				vars.add((Variable) v);
		}
	}
	
	/**
	 * Write results from the solver back to a double array
	 * @param mVars the list of Variables in the model
	 * @param pVars the array where the results should be written to
	 */
	protected void writeVarsBack(Object mVars, Object pVars) {
		writeVarsBack(mVars, pVars, double.class);
	}
	
	/**
	 * Write results from the solver back to an array
	 * @param mVars the list of Variables in the model
	 * @param pVars the array where the results should be written to
	 * @param type the type of the resulting array
	 */
	protected void writeVarsBack(Object mVars, Object pVars, Class<?> type) {
		if(mVars.getClass().isArray()) {
			Object[] m = (Object[]) mVars;
			for(int i=0; i<m.length; i++) {
				if(m[i].getClass().isArray()) {
					writeVarsBack(m[i], ((Object[]) pVars)[i], type);
				} else {
					Double d = ((Variable) m[i]).getSolution();
					if(type.equals(boolean.class))
						Array.set(pVars, i, d > 0.1);
					else if(type.equals(int.class))
						Array.set(pVars, i, d.intValue());
					else
						Array.set(pVars, i, d.doubleValue());
				}
			}
		} else {
			pVars = ((Variable) mVars).getSolution();
		}
	}
	
	/**
	 * Write results from the solver back to a double array
	 * @param mVars the list of Variables in the model
	 */
	public Object writeVarsBack(Object mVars) {
		return writeVarsBack(mVars, double.class);
	}
	
	/**
	 * Write results from the solver back to an array
	 * @param mVars the list of Variables in the model
	 * @param type the base type of the resulting array
	 */
	public Object writeVarsBack(Object mVars, Class<?> type) {
		Object pVars = getSimilarArray(mVars, type);
		writeVarsBack(mVars, pVars, type);
		return pVars;
	}
	
	/**
	 * get an array of similar dimensions as the input object m, and of type type
	 * @param m the input array whose dimensions need to be copied
	 * @param type the base type of the resulting array
	 * @return an array with the dimensions of m, and with base type type
	 */
	private Object getSimilarArray(Object m, Class<?> type) {
		if(m.getClass().isArray()) {
			Object[] a = (Object[]) m;
			if(a.length == 0 || !a[0].getClass().isArray()) {
				return Array.newInstance(type, a.length);
			} else {
				List<Object> res = new ArrayList<>(a.length);
				for(int i=0; i<a.length; i++) {
					res.add(getSimilarArray(a[i], type));
				}
				Class<?> cpt = res.get(0).getClass();
				Object[] o = (Object[]) Array.newInstance(cpt, a.length);
				for(int i=0; i<a.length; i++) {
					o[i] = res.get(i);
				}
				return o;				
			}
		} else {
			return null;
		}
	}
	
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public boolean isSolvable() {
		return true;
	}
}
