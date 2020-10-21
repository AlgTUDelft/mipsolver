package nl.tudelft.alg.MipSolverGLPK;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.GlpkTerminal;
import org.gnu.glpk.GlpkTerminalListener;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;

import nl.tudelft.alg.MipSolverCore.CMP;
import nl.tudelft.alg.MipSolverCore.Constraint;
import nl.tudelft.alg.MipSolverCore.IMIPSolver;
import nl.tudelft.alg.MipSolverCore.IModel;
import nl.tudelft.alg.MipSolverCore.InfeasibleException;
import nl.tudelft.alg.MipSolverCore.LinExp;
import nl.tudelft.alg.MipSolverCore.MIP;
import nl.tudelft.alg.MipSolverCore.SolverException;
import nl.tudelft.alg.MipSolverCore.VarType;
import nl.tudelft.alg.MipSolverCore.Variable;

public class GLPKSolver implements IMIPSolver {
	private static boolean DEBUG = false;
	private static String logFile = "mip1.log";
	glp_prob prob;
	glp_iocp iocp;
	SWIGTYPE_p_int ind; 
	SWIGTYPE_p_double val;
	MIP mip;
	Map<Variable, Integer> varMap;
	boolean minimize;
	int nVariables;
	boolean fileOutput;
	boolean relaxed;
	FileOutputStream outputStream;
	
	public GLPKSolver() {
		GLPK.glp_term_out(0);
		iocp = new glp_iocp();
		GLPK.glp_init_iocp(iocp);
		minimize = true;
		fileOutput = true;
		relaxed = false;
	}
	
	private void debug(String s) {
		if(DEBUG) System.out.println(s);
	}

	private glp_iocp new_iocp(glp_iocp old) {
		glp_iocp iocp = new glp_iocp();
		GLPK.glp_init_iocp(iocp);
		iocp.setMip_gap(old.getMip_gap());
		return iocp;
	}

	@Override
	public void build(IModel mipInstance) {
		assert(mipInstance instanceof MIP);
		mip = (MIP) mipInstance;
		dispose();
		debug("build model");
		prob = GLPK.glp_create_prob();
		iocp = new_iocp(iocp);
		iocp.setTm_lim((int) (mip.getTimeLimit() * 1000)); //milliseconds
		varMap = new HashMap<Variable, Integer>();
		// Create variables
		nVariables = mip.getVars().size();
		GLPK.glp_add_cols(prob, nVariables);
		int i = 1;
		for(Variable v: mip.getVars()) {
			varMap.put(v, i);
			GLPK.glp_set_col_name(prob, i, v.getName());
			if (v.getType() == VarType.PositiveContinuous) {
				GLPK.glp_set_col_kind(prob, i, GLPKConstants.GLP_CV);
				GLPK.glp_set_col_bnds(prob, i, GLPKConstants.GLP_LO, 0, 0);
			} else if (v.getType() == VarType.NegativeContinuous) {
				GLPK.glp_set_col_kind(prob, i, GLPKConstants.GLP_CV);
				GLPK.glp_set_col_bnds(prob, i, GLPKConstants.GLP_UP, 0, 0);
			} else if (v.getType() == VarType.Binary && !relaxed) {
				GLPK.glp_set_col_kind(prob, i, GLPKConstants.GLP_BV);
			} else if (v.getType() == VarType.BinaryContinuous || (v.getType() == VarType.Binary && relaxed)) {
				GLPK.glp_set_col_kind(prob, i, GLPKConstants.GLP_CV);
				GLPK.glp_set_col_bnds(prob, i, GLPKConstants.GLP_DB, 0, 1);
			} else {
				GLPK.glp_set_col_kind(prob, i, GLPKConstants.GLP_CV);
				GLPK.glp_set_col_bnds(prob, i, GLPKConstants.GLP_FR, 0, 0);
			}
			i++;
		}
		debug("variables: done");
		
		ind = GLPK.new_intArray(nVariables+1);
		val = GLPK.new_doubleArray(nVariables+1);
		GLPK.glp_add_rows(prob, mip.getConstraints().size());
		i = 1;
		for(Constraint c: mip.getConstraints()) {
			addConstraint(i,c);
			i++;
		}
		debug("constraints: done");
		setObjectiveFunction();
		debug("objective: done");
		
		debug("Done building");
	}
	
	private void addConstraint(int i, Constraint c) {
		debug("add constraint "+c.getName());
		GLPK.glp_set_row_name(prob, i, c.getName());
		double b = - c.getLeft().get(Variable.CONST) + c.getRight().get(Variable.CONST);
		if(c.getComparator() == CMP.EQ)
			GLPK.glp_set_row_bnds(prob, i, GLPKConstants.GLP_FX, b, b);
		else if(c.getComparator() == CMP.LARGEREQ)
			GLPK.glp_set_row_bnds(prob, i, GLPKConstants.GLP_LO, b, b);
		else 
			GLPK.glp_set_row_bnds(prob, i, GLPKConstants.GLP_UP, b, b);
		debug("bound set");
		Set<Variable> vars = new HashSet<Variable>(c.getLeft().getVariables());
		vars.addAll(c.getRight().getVariables());
		debug("union of variables");
		
		
		debug("init arrays");

		int j = 1;
		for(Variable v: vars) {
			if(v.equals(Variable.CONST)) continue;
			GLPK.intArray_setitem(ind, j, varMap.get(v));
			GLPK.doubleArray_setitem(val, j, c.getLeft().get(v) - c.getRight().get(v));
			j++;
		}
        GLPK.glp_set_mat_row(prob, i, j-1, ind, val);
        debug("matrix build");
	}

	@Override
	public void setObjectiveFunction() {
		// Define objective
        GLPK.glp_set_obj_name(prob, "obj");
        if(minimize)
        	GLPK.glp_set_obj_dir(prob, GLPKConstants.GLP_MIN);
        else 
            GLPK.glp_set_obj_dir(prob, GLPKConstants.GLP_MAX);
        LinExp obj = (LinExp) mip.getObjectiveFunction();
        for(Variable v: obj.getVariables()) {
        	if(v.equals(Variable.CONST))
        		GLPK.glp_set_obj_coef(prob, 0, obj.get(v));
        	else if (Math.abs(obj.get(v)) > 1e-4){
        		GLPK.glp_set_obj_coef(prob, varMap.get(v), obj.get(v));
        	}
        }
	}

	@Override
	public void setMinimize(boolean value) {
		minimize = value;
	}

	@Override
	public double[] getUnboundedRay() {
		int varIndex = GLPK.glp_get_unbnd_ray(prob);
		double[] result = new double[nVariables];
		result[varIndex] = 1;
		return result;
	}

	@Override
	public double solve() throws SolverException {
		GLPK.glp_term_out(1);
		iocp.setMsg_lev(3);
		iocp.setPresolve(GLPKConstants.GLP_ON);
		redirectOutput();
		double ret;
		try {
			ret = GLPK.glp_intopt(prob, iocp);
		} catch (GlpkException e) {
			throw new InfeasibleException();
		} finally {
			GLPK.glp_term_out(0);
			closeOutput();
		}

		int mipstatus = GLPK.glp_mip_status(prob);
		// Retrieve solution
		if (ret == 0 || mipstatus == GLPK.GLP_OPT || mipstatus == GLPK.GLP_FEAS) {
			if (ret == GLPK.GLP_EMIPGAP) debug("GLP_EMIPGAP - solver stopped because minimum mip gap reached.");
			if (ret == GLPK.GLP_ETMLIM) debug("GLP_ETMLIM - Time limit exceeded.");
			if (mipstatus == GLPK.GLP_FEAS) debug("GLP_FEAS - (non-)optimality not yet proven.");
			if (mipstatus == GLPK.GLP_OPT) debug("GLP_OPT - Optimal solution.");
			for (Variable v : mip.getVars()) {
				double val = GLPK.glp_mip_col_val(prob, varMap.get(v));
				v.setSolution(val);
			}
			mip.writeSolution();
			return GLPK.glp_mip_obj_val(prob);
		} else {
			debug("The problem could not be solved.");
			if (ret == GLPK.GLP_EBOUND)
				throw new SolverException("GLP_EBOUND - could not start, variable has incorrect bounds.");
			else if (ret == GLPK.GLP_EROOT)
				throw new SolverException("GLP_EROOT - optimal LP inital base not provided.");
			else if (ret == GLPK.GLP_ENOPFS)
				throw new InfeasibleException("GLP_ENOPFS - LP has no primal feasible solution.");
			else if (ret == GLPK.GLP_ENODFS)
				throw new InfeasibleException("GLP_ENODFS - LP has no dual feasible solution.");
			else if (ret == GLPK.GLP_EFAIL)
				throw new SolverException("GLP_EFAIL - solver failure.");
			else if (ret == GLPK.GLP_ETMLIM)
				throw new SolverException("GLP_ETMLIM - Time limit exceeded.");
			else if (ret == GLPK.GLP_ESTOP)
				throw new SolverException("GLP_ESTOP - Terminated by application.");
			return Double.MAX_VALUE;
		}
	}

	private void closeOutput() throws SolverException {
		if (outputStream != null) {
			try {
				outputStream.close();
				outputStream = null;
			} catch (IOException e) {
				throw new SolverException(e);
			}
		}
	}

	private void redirectOutput() {
		GlpkTerminal.removeAllListeners();
		GlpkTerminal.addListener(new GlpkTerminalListener() {
			@Override
			public boolean output(String str) {
				try {
					if (outputStream == null) {
						File file = new File(logFile);
						if (!file.exists()) file.createNewFile();
						outputStream = new FileOutputStream(file, true);
					}
					if (outputStream != null) {
						outputStream.write(str.getBytes());
						outputStream.flush();
					}
				} catch (Exception e) {
					throw new RuntimeException("Error in writing to GLPK log file: " + e.getLocalizedMessage(), e);
				}
				return DEBUG;
			}
		});
	}

	@Override
	public void save(String file) {
		GLPK.glp_write_lp(prob, null, file);
	}

	@Override
	public void dispose() {
		// free memory
		if (prob != null) GLPK.glp_delete_prob(prob);
		if (ind != null) GLPK.delete_intArray(ind);
		if (val != null) GLPK.delete_doubleArray(val);
        debug("clear memory");
	}

	@Override
	public void setSolveAsLP(boolean value) {
		relaxed = value;
	}

	@Override
	public void setMipGap(double value) {
		iocp.setMip_gap(value);
	}

	@Override
	public void setDebug(boolean value) {
		fileOutput = value;
	}

	@Override
	public void setLogFile(String mipLogFile) throws SolverException {
		logFile = mipLogFile;
	}

}
