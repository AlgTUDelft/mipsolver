package nl.tudelft.alg.MipSolverCore;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class QuadExp extends Exp {
	Map<VariablePair, Double> expr;
	
	public QuadExp() {
		super();
		expr = new HashMap<VariablePair, Double>();
	}
	
	@Override
	public QuadExp addTerm(Variable x, double value){
		return addTerm(x, null, value);
	}
	
	public QuadExp addTerm(Variable x, Variable y, double value){
		if(value == 0.0)
			return this;
		double current = 0;
		VariablePair p = new VariablePair(x,y);
		if(expr.containsKey(p))
			current = get(p);
		expr.put(p, current + value);
		return this;
	}
	
	@Override
	public QuadExp addTerm(Variable x){
		expr.put(new VariablePair(x), 1.0);
		return this;
	}
	
	@Override
	public QuadExp addTerm(double value) {
		expr.put(VariablePair.CONST, get(VariablePair.CONST) + value);
		return this;
	}
	
	@Override
	public Double get(Variable x) {
		return get(new VariablePair(x));
	}
	
	public Double get(Variable x, Variable y) {
		return get(new VariablePair(x, y));	
	}
	
	public Double get(VariablePair p) {
		if(!expr.containsKey(p)) return 0.0;
		return expr.get(p);
	}
	
	@Override
	public Set<Variable> getVariables() {
		return expr.keySet().stream().flatMap(k -> k.getVariables().stream()).collect(Collectors.toSet());
	}
	
	public Set<VariablePair> getVariablePairs() {
		return expr.keySet();
	}
}

