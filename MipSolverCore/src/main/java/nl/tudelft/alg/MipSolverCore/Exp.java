package nl.tudelft.alg.MipSolverCore;

import java.util.Set;

public abstract class Exp {
	
	/**
	 * Add a term to the expression
	 * @param x the variable to add
	 * @return this expression
	 */
	public abstract Exp addTerm(Variable x);
	
	/**
	 * Add a variable multiplied by a value to the expression
	 * @param x the variable to add
	 * @param value the value to multiply the variable with
	 * @return this expression
	 */
	public abstract Exp addTerm(Variable x, double value);
	
	/**
	 * Add a constant to the expression
	 * @param value the constant to add
	 * @return this expression
	 */
	public abstract Exp addTerm(double value);
	
	/**
	 * Get the coefficient for a variable in this expression
	 * @param x the variable to get the coefficient of
	 * @return the coefficient
	 */
	public abstract Double get(Variable x);
	
	/**
	 * Get all the variables in this expression
	 * @return all variables in this expression
	 */
	public abstract Set<Variable> getVariables();
	
	/**
	 * Add a linear expression to this expression
	 * @param add the linear expression to add
	 * @return this expression
	 */
	public Exp addLinExp(LinExp add) {
		for(Variable v: add.getVariables()) {
			addTerm(v, add.get(v));
		}
		return this;
	}
	
	/**
	 * Add a (number of) linear expression(s) to this expression
	 * @param adds the linear expression(s) to add
	 * @return this expression
	 */
	public Exp addLinExps(LinExp... adds) {
		for(LinExp add: adds) {
			addLinExp(add);
		}
		return this;
	}
	
	/**
	 * Subtract a linear expression from this expression
	 * @param sub the linear expression to subtract
	 * @return this expression
	 */
	public Exp subtractLinExp(LinExp sub) {
		for(Variable v: sub.getVariables()) {
			addTerm(v, -sub.get(v));
		}
		return this;
	}
	
	/**
	 * Subtract a (number of) linear expression(s) from this expression
	 * @param subs the linear expression(s) to subtract
	 * @return this expression
	 */
	public Exp subtractLinExps(LinExp... subs) {
		for(LinExp sub: subs) {
			subtractLinExp(sub);
		}
		return this;
	}
}
