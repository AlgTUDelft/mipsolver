package nl.tudelft.alg.MipSolverCore;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class that holds a mutable representation of a linear expression
 */
public class LinExp extends Exp {
	Map<Variable, Double> expr;
	
	/**
	 * Create an empty linear expression. This linear expression has value zero.
	 */
	public LinExp(){
		super();
		expr = new HashMap<Variable, Double>();
	}
	
	/**
	 * Create a new linear expression. Terms can be added one by one in the order of constant, variable, constant, variable, etc.
	 * If the last constant is not followed by another variable, this constant is added as a constant to the expression.
	 * @param terms The terms to add to the expression
	 */
	public LinExp(Object... terms) {
		this();
		if (terms == null) return;
		double multiplier = 1.0;
		boolean expecting = false;
		for(Object o: terms) {
			if(o instanceof Double) {
				multiplier *= (Double) o;
				expecting = true;
			} else if(o instanceof Integer) {
				multiplier *= (Integer) o;
				expecting = true;
			} else if(o instanceof Boolean) {
				multiplier *= ((Boolean) o) ? 1 : 0;
				expecting = true;
			} else if(o instanceof Variable) {
				addTerm((Variable) o, multiplier);
				multiplier = 1.0;
				expecting = false;
			} else {
				assert false;
			}
		}
		if(expecting) addTerm(multiplier);
	}
	
	@Override
	public LinExp addTerm(Variable x, double value){
		if(value == 0.0)
			return this;
		double current = 0;
		if(expr.containsKey(x))
			current = get(x);
		expr.put(x, current + value);
		return this;
	}
	
	/**
	 * Add a negated term to this expression. This means that in effect (1-x)*value is added to the linear expression.
	 * Useful for adding negated binary variables
	 * @param x the (negated) variable to add to the expression.
	 * @param value the value to multiply the negated variable with.
	 * @return this expression
	 */
	public LinExp addNegationTerm(Variable x, double value){
		addTerm(value);
		addTerm(x, -value);
		return this;
	}
	
	@Override
	public LinExp addTerm(Variable x){
		expr.put(x, 1.0);
		return this;
	}
	
	/**
	 * Add a negated term to this expression. This means that in effect (1-x) is added to the linear expression.
	 * Useful for adding negated binary variables
	 * @param x the (negated) variable to add to the expression.
	 * @return this expression
	 */
	public LinExp addNegationTerm(Variable x){
		addNegationTerm(x, 1.0);
		return this;
	}
	
	@Override
	public LinExp addTerm(double value) {
		expr.put(Variable.CONST, get(Variable.CONST) + value);
		return this;
	}
	
	@Override
	public Double get(Variable x) {
		if(!expr.containsKey(x)) return 0.0;
		return expr.get(x);
	}
	
	@Override
	public Set<Variable> getVariables() {
		return expr.keySet();
	}

	/**
	 * Multiply this linear expression by a scalar m
	 * @param m the scalar to multiply this expression by
	 * @return this expression
	 */
	public LinExp multiplyBy(double m) {
		for(Map.Entry<Variable, Double> e: expr.entrySet()) {
			Variable v = e.getKey();
			Double d = e.getValue();
			expr.put(v, d*m);
		}
		return this;
	}
	
	@Override
	public LinExp addLinExp(LinExp add) {
		return (LinExp) super.addLinExp(add);
	}
	
	@Override
	public LinExp subtractLinExp(LinExp sub) {
		return (LinExp) super.subtractLinExp(sub);
	}
	
	/**
	 * Add a negated linear expression. In effect this means adding (1-x)*value
	 * This is useful for adding linear expressions that consist of binary variables.
	 * @param x the linear expression to add negated
	 * @param value the value to multiply the negated expression with
	 * @return this expression
	 */
	public LinExp addNegationLinExp(LinExp x, double value){
		addTerm(value);
		addLinExp(x.multiplyBy(-value));
		return this;
	}
	
	/**
	 * @return true if this linear expression is a constant (ie. does not contain any variables)
	 */
	public boolean isConstant() {
		return expr.size() == 0 ||
			expr.size() == 1 && expr.containsKey(Variable.CONST);
	}
	
	/**
	 * Multiply this linear expression by another. This method works only if one of this or the other linear expression is 'constant'
	 * @param other the linear expression to multiply with
	 * @return this expression
	 */
	public LinExp multiplyBy(LinExp other) {
		assert this.isConstant() || other.isConstant();
		if(this.isConstant()) {
			double c = this.get(Variable.CONST);
			this.addTerm(-c);
			for(Variable v: other.getVariables()) {
				this.addTerm(v, c);
			}
		} else {
			double c = other.get(Variable.CONST);
			this.multiplyBy(c);
		}
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		double _const = 0;
		boolean first = true;
		for(Map.Entry<Variable, Double> e: expr.entrySet()) {
			if(e.getKey() == Variable.CONST) {
				_const = e.getValue();
				continue;
			}
			if(Math.abs(e.getValue()) < 1e-6) continue;
			if(!first || e.getValue() < 0) res.append((e.getValue() >= 0 ? " + " : " - "));
			if(Math.abs(Math.abs(e.getValue()) - 1) >= 1e-6)
				res.append(String.format("%.4g ", Math.abs(e.getValue())));
			res.append(e.getKey().getName());
			first = false;
		}
		if(first || Math.abs(_const) >= 1e-6) {
			if(!first || _const < 0) res.append((_const >= 0 ? " + " : " - "));
			res.append(String.format("%.4g", Math.abs(_const)));
		}
		return res.toString();
	}
}
