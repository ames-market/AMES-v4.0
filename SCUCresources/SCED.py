'''
This file is part of the
AMES Wholesale Power Market Test Bed (Java): A Free Open-Source Test-Bed
         for the Agent-based Modeling of Electricity Systems
'''
from __future__ import division
from datetime import datetime
import os.path
import sys
import getopt

from pyomo import *
from pyomo.opt import SolutionStatus

from coopr.pyomo import *
from coopr.opt import SolutionStatus

import Models.ReferenceModel


log_file = os.path.join("log", (datetime.now().strftime("%Y-%m-%d_%H%M")))
print "LogFile", log_file

def compute_sced(solver_options, commitments, reference_model, tee = False):
    '''
    Use the Reference model to compute the SCED for the input by fixing
    the UnitOn values.
    @param solver_options dictionary of options to pass to the solver
    @param commitments 0/1 values, indexed by GenCo and then by hour.
    @param reference_model name of the file to load the reference model from
    @param tee whether or not to see the output of the solver. Default False
    '''
    instance = Models.ReferenceModel.model.create(reference_model)

    opt = pyomo.opt.SolverFactory("cplex")
    for k in solver_options:
        opt.options[k] = solver_options[k]

    #opt["tee"] = True
    #opt = coopr.opt.SolverFactory("glpk")

    #fix the GenCo commitments
    for g in commitments:
        #print "Fixing commitments for", g
        for t in sorted(commitments[g]):
            c = commitments[g][t]
            #print "H:", str(t), "BIN:", c
            instance.UnitOn[g, t] = c
            instance.UnitOn[g, t].fixed = True

    instance.preprocess()
    results = opt.solve(instance, suffixes=['dual'], tee=tee)
    return (instance, results)

def write_sced_results(instance, results, outfile, commitments=None):
    '''
    Write the required parts of the results t the outfile
    @param instance -- The instance of the model used for results
    @param results  -- Basic results. These will be transformed to get duals.
    @param outfile  -- Name of the output file.
    '''

    ensure_dir(log_file)
    with open(log_file, "a") as l:
        print >>l, "Initial Results"
        print >>l, results

    solution_status = results.Solution.Status
    print ("The solver returned a result of: " + str(solution_status))

    if solution_status == SolutionStatus.error or \
       solution_status == SolutionStatus.unknown:
        with open(outfile, "w") as f:
            f.write("Solution Status: " + str(solution_status))
        return 1

    #load the instance with results to make accessing the results easier
    if not instance.load(results):
        raise Exception("Unable to load model instance with results")

    log_model(instance, "With results loaded")

    transformed_results = instance.update_results(results)

    #transformed constraints
    t_constraints = transformed_results.solution(0)
    #print "Transformed solution"
    #print t_constraints

#
# Print the results
#
# An alternate way to print just the constraint duals
#    print "Duals"
#    from coopr.pyomo import Constraint
#    for c in instance.active_components(Constraint):
#        try:
#            print "Constraint",c
#            cobject = getattr(instance, c)
#            for index in cobject:
#                if index is None:
#                    print "Index is None"
#                    continue
#                print "     ", index, cobject[index].get_suffix_value("dual")
#        except:
#            print "Problem with", c

    with open(outfile, "w") as f:
        print "Writing SCED to", os.path.abspath(f.name)
        #Time stamp the file.
        f.write("//TIMESTAMP:" + str(datetime.now()) +"\n")
    #Write out the LMPS
        f.write("LMP\n")
        #FIXME: There has to be a better way to do this!
        for con in sorted(t_constraints.constraint.keys()):
            if con.startswith("PowerBalance"):
                lmp = t_constraints.constraint[con]["Dual"]
                lbl = con[13:-1].split(",")
                bn = lbl[0]
                h = lbl[1]
                f.write(str(bn) + ' : ' + str(h) +' : ' + str(lmp) +"\n")
        f.write("END_LMP\n")
    #Write out the dispatch for each genco
        f.write("GenCoResults\n")
        for g in sorted(instance.ThermalGenerators.value):
            print >> f, "%s" % str(g).ljust(8)
            for t in instance.TimePeriods:
                print >> f, "Hour: ", str(t)

                #Check to see if should have generated power!
                if (not commitments is None) and (instance.PowerGenerated[g, t]() > 0) :
                    if commitments[g][t] != 1 :
                        print "Warning ", str(g), str(t), "generated", str(instance.PowerGenerated[g,t]()), "but was not committed."

                print >> f, "\tPowerGenerated: %6.2f" % instance.PowerGenerated[g, t]()
                print >> f, "\tProductionCost: %6.2f" % instance.ProductionCost[g, t]()
                print >> f, "\tStartupCost: %6.2f" % instance.StartupCost[g, t]()
                print >> f, "\tShutdownCost: %6.2f" % instance.ShutdownCost[g, t]()

        f.write("END_GenCoResults\n")
    #Write out the Voltage angles at each bus/zone
        f.write("VOLTAGE_ANGLES\n")
        for bus in sorted(instance.Buses):
            for t in instance.TimePeriods:
                print >> f, str(bus), str(t), ":", str(instance.Angle[bus, t]())
                #print >>f, "\t %s,  : %6.2f"
        f.write("END_VOLTAGE_ANGLES\n")
    #Write out the Daily LMP
        f.write("DAILY_BRANCH_LMP\n")
        f.write("END_DAILY_BRANCH_LMP\n")
    #Write out the Daily Price Sensitive Demand
        f.write("DAILY_PRICE_SENSITIVE_DEMAND\n")
        f.write("END_DAILY_PRICE_SENSITIVE_DEMAND\n")
    #Write out which hour has a solution
        f.write("HAS_SOLUTION\n")
        h = 0
        max_hour = 24 #FIXME: Hard-coded number of hours.
        while h < max_hour:
            f.write("1\t") #FIXME: Hard-coded every hour has a solution.
            h += 1
        f.write("\nEND_HAS_SOLUTION\n")

    return 0

def log_model(instance, msg=None) :
    with open(log_file, "a") as l:
        print >>l, "#################################################################"
        if not msg is None:
            print >>l, msg

        print >>l, "Instance"
        for v in instance.active_components(Var):
            print >>l, "Variable", v
            varobject = getattr(instance, v)
            for index in sorted(varobject):
                print >>l, "    ",index, varobject[index].value
        for p in instance.active_components(Param):
            print >>l, "Parameter", p
            paramobject = getattr(instance, p)
            for index in sorted(paramobject):
                try :
                    v = paramobject[index].value
                except:
                    v = paramobject[index]
                print >>l, "    ",index, v

def ensure_dir(f) :
    d = os.path.dirname(f)
    if not os.path.exists(d):
        os.makedirs(d)


def read_commitment(commitment_file):
    '''Read the data file.
    Assume the format is
        GenCo id key
            24 commitment vectors: 0/1 Data, Data
    '''
    commitments_by_gen = dict()
    with open(commitment_file, 'r') as f:
        for line in f:
            #GenCO label
            g = line.strip()
            if len(g) == 0: #blank line in the file.
                continue

            commitments_by_gen[g] = dict()
            #24 hours or commitments.
            i = 1
            while i <= 24:
                #TODO: Paranoia about data being what I think it it.
                l = f.next().strip()
                if l is None: #blank line
                    continue
                l = l.split()
                commitments_by_gen[g][i] = int(l[0])
                i += 1
    return commitments_by_gen

def debug_results(commitment_file, model_file):
    '''
    Helper function to get a results object. Sequences the read csv and run pyomo
    actions of main.
    '''
     #TODO: Trap file not found
    cv = read_commitment(commitment_file)

    solver_options = {"threads" : 2}
    #TODO: Trap exceptions
    return compute_sced(solver_options, cv, model_file, False)

def main():
    '''
    Main function. Called at the end of the script is this
    is the main program.
    '''
    def show_cv():
        '''
        Helper to debug the commit vector
        '''
        for k in sorted(cv.keys()):
            print k
            g = cv[k]
            print g

    def show_usage():
        print "Usage: -f <pat/to/unit/commitment> -o <path/to/out/file> -r <path/to/reference/model>"

    
    #Hack to support spaces in paths.
    # if a string is surrounded by single quotes, get rid of them.
    # but, the args comming in may need those quotes to have getopt parse a space in a path name incorrectly.
    def trimQuote(s):
        if len(s) < 2:
            return s

        if s[0] == "'":
            s = s[1:]
        if s[-1] == "'":
            s = s[:-1]
        return s
    
    opt_markers = "f:o:r:h"

    if len(sys.argv[1:]) == 0:
        show_usage()
        sys.exit(1)

    opts, _ = getopt.getopt(sys.argv[1:], opt_markers)
    uc_file = None #File with the unit commitments
    outfile = None
    reference_model = None
    for o, a in opts:
        a = trimQuote(a)
        if o == "-f" :
            uc_file = a
        elif o == "-o":
            outfile = a
        elif o == "-r":
            reference_model = a
        elif o == "-h":
            show_usage()
            sys.exit(0)
        else:
            sys.stderr.write("Unknown option " + o + " EXITING!")
            sys.exit(1)

    if uc_file is None:
        sys.stderr.write("Missing unit commitment file.\n")
        sys.exit(1)

    if outfile is None:
        sys.stderr.write("Missing output file.\n")
        sys.exit(1)

    if reference_model is None:
        sys.stderr.write("Missing reference model.\n")
        sys.exit(1)

    #TODO: Trap file not found
    cv = read_commitment(uc_file)
    #show_cv()

#FIXME: Different solver algorims. Barrier, Primal Simplex, Dual Simplex. May need
# something other than default to get good prices.
    solver_options = {"threads" : 2}
    #TODO: Trap exceptions
    (instance, results) = compute_sced(solver_options, cv, reference_model, False)

    rc = write_sced_results(instance, results, outfile, cv)

    sys.exit(rc)

if __name__ == "__main__":
    main()
