from coopr.pyomo import *

from pyutilib.component.core import *

import math

class UCPrettyPrint(SingletonPlugin):

   implements(IPyomoScriptPostprocess)

   def apply(self, **kwds):

        options=kwds.pop('options')
        instance=kwds.pop('instance')
        results=kwds.pop('results')
        
#       instance.pprint()
        outfile = open ("xfertoames.dat", "w")
        dict = {};
        for g in instance.ThermalGenerators.value:
            for t in instance.TimePeriods:
                dict[(g,t)] = instance.UnitOn[g,t];
       
        for g in sorted(instance.ThermalGenerators.value):
            print >>outfile,"%s" % str(g).ljust(8)
            for t in sorted(instance.TimePeriods):
                print >>outfile,"% 1d %6.2f %6.2f" % (int(dict[(g,t)]+0.5), 0.0, 0.0)
        outfile.close()