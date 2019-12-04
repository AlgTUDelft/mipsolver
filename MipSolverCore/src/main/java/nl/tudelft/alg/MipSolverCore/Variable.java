package nl.tudelft.alg.MipSolverCore;


public class Variable {
	public final static Variable CONST = new Variable("const", VarType.Real); 
	String name;
	VarType type;
	Double solution;
	
	public Variable(String name, VarType type) {
		this.name = name;
		this.type = type;
		this.solution = null;
	}

	public String getName() {
		return name;
	}

	public VarType getType() {
		return type;
	}
	
	public Double getSolution() {
		return solution;
	}
	
	public void setSolution(double s) {
		solution = s;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable other = (Variable) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return name.toString() + ": " +(solution == null ? "## " : solution.toString());
	}

}
