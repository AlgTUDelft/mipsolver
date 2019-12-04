package nl.tudelft.alg.MipSolverCore;

import java.util.HashSet;
import java.util.Set;

public class VariablePair {
	final static VariablePair CONST = new VariablePair();
	Variable v1, v2;
	
	public VariablePair(Variable v1, Variable v2) {
		this.v1 = v1;
		this.v2 = v2;
	}
	
	public VariablePair(Variable v1) {
		this.v1 = v1;
		this.v2 = null;
	}
	
	private VariablePair() {
		this.v1 = null;
		this.v2 = null;
	}
	
	public Set<Variable> getVariables() {
		Set<Variable> result = new HashSet<Variable>();
		if(v1 != null) result.add(v1); 
		if(v2 != null) result.add(v2);
		return result;
	}
	
	public Variable[] getVariableArray() {
		return getVariables().toArray(new Variable[0]);
	}
		
	public boolean isConstant() {
		return v1 == null && v2 == null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((v1 == null) ? 0 : v1.hashCode());
		result = prime * result + ((v2 == null) ? 0 : v2.hashCode());
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
		VariablePair other = (VariablePair) obj;
		if (v1 == null) {
			if (other.v1 != null)
				return false;
		} else if (!v1.equals(other.v1))
			return false;
		if (v2 == null) {
			if (other.v2 != null)
				return false;
		} else if (!v2.equals(other.v2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "("+v1+"*"+v2+")";
	}
	
	
	
	
}
