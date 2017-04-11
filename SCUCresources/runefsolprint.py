import string
import math
from pyutilib.component.core import *
from coopr.pysp import solutionwriter
from coopr.pysp.scenariotree import *

#
# write the generator data for AMES (as found by runef)
#

def index_to_pair(index):
    # return the thermal, time pair
    indstr = str(index)
    indstr = indstr.lstrip('(').rstrip(')')
    indstr = indstr.replace(' ','')
    indstr = indstr.replace("'","")

    pieces = indstr.split(",")
    thermal = pieces[0]
    hour = int(pieces[1])
    return thermal, hour

class CSVSolutionWriter(SingletonPlugin):

    implements (solutionwriter.ISolutionWriterExtension)

    def write(self, scenario_tree, scenario_instances, output_file_prefix):

        if not isinstance(scenario_tree, ScenarioTree):
            raise RuntimeError("CSVSolutionWriter write method expects ScenarioTree object - type of supplied object="+str(type(scenario_tree)))

        output_filename = "xfertoames.dat"
        outfile = open(output_filename,"w")

        root_node = scenario_tree._stages[0]._tree_nodes[0]

        # get the indexes of UnitOn
        Thermals = []
        Hours = []  # assume that every thermal has all hours
#       print len(root_node._variable_indices['UnitOn'])
        dict = {};
#       print zip(root_node._variable_indices['UnitOn'],root_node._solution.items())
        for idx in root_node._variable_indices['UnitOn']:
            thermal, hour = index_to_pair(idx)
            if thermal not in Thermals:
                Thermals.append(thermal)
            if hour not in Hours:
                Hours.append(hour)
        # the following could be better... but this works (dlw Jan 2013)
        # instead of the if, I should index directly into the _solutions structure
#        print dir(root_node)
#        print root_node._variable_ids
        uc = root_node._solution.values()
        count = 0;
        for g in sorted(Thermals):
            print >>outfile,"%s" % str(g).ljust(8)
            for t in sorted(Hours):
                print >>outfile, "% 1d %6.2f %6.2f" % (int(uc[count]+.5), 0.0, 0.0)
                count = count+1;
        outfile.close()

        print("Generator on-off to file="+output_filename)
