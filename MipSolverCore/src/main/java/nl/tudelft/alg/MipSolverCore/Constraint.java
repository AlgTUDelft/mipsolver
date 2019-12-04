package nl.tudelft.alg.MipSolverCore;


public class Constraint {
	LinExp left;
	LinExp right;
	CMP comparator;
	String name;
	
	/**
	 * Add a constraint to the model
	 * @param left the left hand side expression of the constraint
	 * @param right the right hand side expression of the constraint
	 * @param cmp the comparator (<=, =, >=) of the constraint
	 * @param name the name of the constraint
	 */
	public Constraint(LinExp left, LinExp right, CMP comparator, String name) {
		this.left = left;
		this.right = right;
		this.comparator = comparator;
		this.name = name;
	}
	
	/**
	 * Add a constraint to the model
	 * @param left the left hand side expression of the constraint
	 * @param value the right hand side value of the constraint
	 * @param cmp the comparator (<=, =, >=) of the constraint
	 * @param name the name of the constraint
	 */
	public Constraint(LinExp left, int value, CMP comparator, String name) {
		this(left, new LinExp().addTerm(Variable.CONST, value), comparator, name);
	}

	/**
	 * @return the left
	 */
	public LinExp getLeft() {
		return left;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the right
	 */
	public LinExp getRight() {
		return right;
	}

	/**
	 * @return the comparator
	 */
	public CMP getComparator() {
		return comparator;
	}
	
	@Override
	public String toString() {
		return name + ": " + left.toString() + " " + comparator.toString() + " " + right.toString();
	}
}
