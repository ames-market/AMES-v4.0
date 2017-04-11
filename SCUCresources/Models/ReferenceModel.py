#######################################################################################################
# a basic (thermal) unit commitment model, drawn from:                                                 #
# A Computationally Efficient Mixed-Integer Linear Formulation for the Thermal Unit Commitment Problem #
# Miguel Carrion and Jose M. Arroyo                                                                    #
# IEEE Transactions on Power Systems, Volume 21, Number 3, August 2006.                                #
########################################################################################################

# JPW's model modified by DLW to have transmission constraints
# comments for these items have an (S) for security
# constraints now have penalties such as load loss, fast ramping, etc.

# WIND:
# wind is basically a generator, set the upper and lower bounds to be the same to make it non-dispatchable
# not doing it as negative load (but this has the same effect if it is non-dispatch....)
# as of 9 Feb 2012, wind does not appear in the objective function on the assumption that it is a sunk cost

# STORAGE:
# Storage has properties of load and generator. It functions as generator (discharge) when its power OUTPUT
# is > 0, and it acts as a load (charging) when its power INPUT is > 0.
# As of 21 Feb 2012, storage does not appear in the objective function on the assumption that it is a sunk
# cost and that it is operated by the ISO/RTO, so the energy employed to charge the storage is paid for by
# the Load Serving Entities (LSEs)
# As of 21 Feb 2012, energy storage is not included as a source of reserves
# As of 21 Feb 2012, energy storage assummes hourly time periods - energy conservation constraint
# As of 21 Feb 2012, energy storage assummes 20 time periods (NoNondispatchableStorage.dat) - end-point constraint
# Minimum input/output power must be set 0.0, otherwise there could be errors
# As of 22 Feb 2012, energy storage has linear losses (round trip efficiency) - energy conservation


from coopr.pyomo import *

model = AbstractModel()

#
# Parameters
#

##############################################
# string indentifiers for the set of busses. (S)
##############################################

model.Buses = Set(ordered=True)

################################

model.StageSet = Set() 

# IMPORTANT: The stage set must be non-empty - otherwise, zero costs result.
def check_stage_set(m):
   return (len(m.StageSet) != 0)
model.CheckStageSet = BuildCheck(rule=check_stage_set)

model.CommitmentStageCost = Var(model.StageSet, within=NonNegativeReals)
model.GenerationStageCost = Var(model.StageSet, within=NonNegativeReals)

#\tau
model.TimePeriodLength = Param(default=1.0)
#\tau_n
model.NumTimePeriods = Param(within=PositiveIntegers)

model.TimePeriods = RangeSet(1, model.NumTimePeriods)
model.CommitmentTimeInStage = Set(model.StageSet, within=model.TimePeriods) # must come from the data files or from an initialization function that uses a parameter that tells you when the stages end (and that thing needs to come from the data files)
model.GenerationTimeInStage = Set(model.StageSet, within=model.TimePeriods)
model.StageCost = Var(model.StageSet, within=NonNegativeReals)


################################ NEW NEW NEW NEW NEW NEW NEW NEW NEW



##############################################
# Network definition (S)
##############################################

model.NumTransmissionLines = Param(default=0)
model.TransmissionLines = RangeSet(model.NumTransmissionLines)

model.BusFrom = Param(model.TransmissionLines)
model.BusTo   = Param(model.TransmissionLines)

def derive_connections_to(m, b):
   return (l for l in m.TransmissionLines if m.BusTo[l]==b)
model.LinesTo = Set(model.Buses, initialize=derive_connections_to)  # derived from TransmissionLines

def derive_connections_from(m, b):
   return (l for l in m.TransmissionLines if m.BusFrom[l]==b)
model.LinesFrom = Set(model.Buses, initialize=derive_connections_from)  # derived from TransmissionLines

#RE(l)
model.Reactance = Param(model.TransmissionLines)
# a negative reactance is treated as infinite (i.e., B=0)
def get_b_from_Reactance(m, l):
    if m.Reactance[l] <= 0:
        return 0.0
    else:
        return 1/float(m.Reactance[l])
model.B = Param(model.TransmissionLines, initialize=get_b_from_Reactance) # Susceptance (1/Reactance; usually 1/x)
#TL(\ell)
model.ThermalLimit = Param(model.TransmissionLines) # max flow across the line

##########################################################
# string indentifiers for the set of thermal generators. #
# and their locations. (S)                               #
##########################################################

model.ThermalGenerators = Set()
model.ThermalGeneratorsAtBus = Set(model.Buses)

model.QuickStartGenerators = Set(within=model.ThermalGenerators)

model.NondispatchableGenerators = Set()
def nd_gen_init(m,b):
    return []
model.NondispatchableGeneratorsAtBus = Set(model.Buses, initialize=nd_gen_init)


#################################################################
# the global system demand, for each time period. units are MW. #
# demand as at busses (S) so total demand is derived
#################################################################

#D(b,k)
model.Demand = Param(model.Buses, model.TimePeriods, default=0.0, mutable=True)

##################################################################
# the global system reserve, for each time period. units are MW. #
##################################################################
#R(k)  could have been changed zone
model.ReserveRequirement = Param(model.TimePeriods, within=NonNegativeReals, default=0.0, mutable=True)

####################################################################################
# minimum and maximum generation levels, for each thermal generator. units are MW. #
# could easily be specified on a per-time period basis, but are not currently.     #
####################################################################################

# you can enter generator limits either once for the generator or for each period (or just take 0)

#\underbar(P)_{j}
model.MinimumPowerOutput = Param(model.ThermalGenerators, within=NonNegativeReals, default=0.0)

def maximum_power_output_validator(m, v, g):
   return v >= value(m.MinimumPowerOutput[g])

#\overbar(P)_j
model.MaximumPowerOutput = Param(model.ThermalGenerators, within=NonNegativeReals, validate=maximum_power_output_validator, default=0.0)

# wind is similar, but max and min will be equal for non-dispatchable wind

#\underbar{NP}_{j}
model.MinNondispatchablePower = Param(model.NondispatchableGenerators, model.TimePeriods, within=NonNegativeReals, default=0.0, mutable=True)

def maximum_nd_output_validator(m, v, g, t):
   return v >= value(m.MinNondispatchablePower[g,t])

#\overbar{NP}_j
model.MaxNondispatchablePower = Param(model.NondispatchableGenerators, model.TimePeriods, within=NonNegativeReals, validate=maximum_nd_output_validator, default=0.0, mutable=True)

#################################################
# generator ramp up/down rates. units are MW/h. #
#################################################

# limits for normal time periods
#NRU_j
model.NominalRampUpLimit = Param(model.ThermalGenerators, within=NonNegativeReals)
#NRD_j
model.NominalRampDownLimit = Param(model.ThermalGenerators, within=NonNegativeReals)

# limits for time periods in which generators are brought on or off-line.
# must be no less than the generator minimum output.
# We're ignoring this validator for right now and enforcing meaning when scaling
def ramp_limit_validator(m, v, g):
   return True
   #return v >= m.MinimumPowerOutput[g] and v <= m.MaximumPowerOutput[g]

#NSU_j
model.StartupRampLimit = Param(model.ThermalGenerators, within=NonNegativeReals, validate=ramp_limit_validator)
#NSD_j
model.ShutdownRampLimit = Param(model.ThermalGenerators, within=NonNegativeReals, validate=ramp_limit_validator)

def scale_ramp_up(m, g):
    temp = m.NominalRampUpLimit[g] * m.TimePeriodLength
    if temp > m.MaximumPowerOutput[g]:
        return m.MaximumPowerOutput[g]
    else:
        return temp
#Calculated internally.
model.ScaledNominalRampUpLimit = Param(model.ThermalGenerators, within=NonNegativeReals, initialize=scale_ramp_up)

def scale_ramp_down(m, g):
    temp = m.NominalRampDownLimit[g] * m.TimePeriodLength
    if temp > m.MaximumPowerOutput[g]:
        return m.MaximumPowerOutput[g]
    else:
        return temp
model.ScaledNominalRampDownLimit = Param(model.ThermalGenerators, within=NonNegativeReals, initialize=scale_ramp_down)

def scale_startup_limit(m, g):
    temp = m.StartupRampLimit[g] #* m.TimePeriodLength
    if temp > m.MaximumPowerOutput[g]:
        return m.MaximumPowerOutput[g]
    else:
        return temp
model.ScaledStartupRampLimit = Param(model.ThermalGenerators, within=NonNegativeReals, validate=ramp_limit_validator, initialize=scale_startup_limit)

def scale_shutdown_limit(m, g):
    temp = m.ShutdownRampLimit[g] #* m.TimePeriodLength
    if temp > m.MaximumPowerOutput[g]:
        return m.MaximumPowerOutput[g]
    else:
        return temp
model.ScaledShutdownRampLimit = Param(model.ThermalGenerators, within=NonNegativeReals, validate=ramp_limit_validator, initialize=scale_shutdown_limit)


##########################################################################################################
# the minimum number of time periods that a generator must be on-line (off-line) once brought up (down). #
##########################################################################################################

#UT_j
model.MinimumUpTime = Param(model.ThermalGenerators, within=NonNegativeIntegers, default=0)
#DT_j
model.MinimumDownTime = Param(model.ThermalGenerators, within=NonNegativeIntegers, default=0)

def scale_min_uptime(m, g):
    return int(round(m.MinimumUpTime[g] / m.TimePeriodLength))
model.ScaledMinimumUpTime = Param(model.ThermalGenerators, within=NonNegativeIntegers, initialize=scale_min_uptime)

def scale_min_downtime(m, g):
    return int(round(m.MinimumDownTime[g] / m.TimePeriodLength))
model.ScaledMinimumDownTime = Param(model.ThermalGenerators, within=NonNegativeIntegers, initialize=scale_min_downtime)

#############################################
# unit on state at t=0 (initial condition). #
#############################################

# if positive, the number of hours prior to (and including) t=0 that the unit has been on.
# if negative, the number of hours prior to (and including) t=0 that the unit has been off.
# the value cannot be 0, by definition.

def t0_state_nonzero_validator(m, v, g):
    return v != 0
# \hat{v}_j(0)
# 
model.UnitOnT0State = Param(model.ThermalGenerators, within=Reals, validate=t0_state_nonzero_validator, mutable=True)

def t0_unit_on_rule(m, g):
    return int(value(m.UnitOnT0State[g]) >= 1)

#v_j(0) --> Value follows immediated from \hat{v}_j value. DON'T SET
model.UnitOnT0 = Param(model.ThermalGenerators, within=Binary, initialize=t0_unit_on_rule, mutable=True)

#######################################################################################
# the number of time periods that a generator must initally on-line (off-line) due to #
# its minimum up time (down time) constraint.                                         #
#######################################################################################

def initial_time_periods_online_rule(m, g):
   if not value(m.UnitOnT0[g]):
      return 0
   else:
      return int(min(value(m.NumTimePeriods),
             round(max(0, value(m.MinimumUpTime[g]) - value(m.UnitOnT0State[g])) / value(m.TimePeriodLength))))

#Calculated
model.InitialTimePeriodsOnLine = Param(model.ThermalGenerators, within=NonNegativeIntegers, initialize=initial_time_periods_online_rule, mutable=True)

def initial_time_periods_offline_rule(m, g):
   if value(m.UnitOnT0[g]):
      return 0
   else:
      return int(min(value(m.NumTimePeriods),
             round(max(0, value(m.MinimumDownTime[g]) + value(m.UnitOnT0State[g])) / value(m.TimePeriodLength)))) # m.UnitOnT0State is negative if unit is off

#Calcualted
model.InitialTimePeriodsOffLine = Param(model.ThermalGenerators, within=NonNegativeIntegers, initialize=initial_time_periods_offline_rule, mutable=True)

####################################################################
# generator power output at t=0 (initial condition). units are MW. #
####################################################################

def between_limits_validator(m, v, g):
    return v <= value(m.MaximumPowerOutput[g]) and v >= value(m.MinimumPowerOutput[g])*value(m.UnitOnT0[g])
#p_j
model.PowerGeneratedT0 = Param(model.ThermalGenerators, within=NonNegativeReals, validate=between_limits_validator, mutable=True)

##################################################################################################################
# production cost coefficients (for the quadratic) a0=constant, a1=linear coefficient, a2=quadratic coefficient. #
##################################################################################################################
#\a_j
model.ProductionCostA0 = Param(model.ThermalGenerators, default=10.0) # units are $/hr (or whatever the time unit is).
#\b_j
model.ProductionCostA1 = Param(model.ThermalGenerators, default=0.0) # units are $/MWhr.
#\c_j
model.ProductionCostA2 = Param(model.ThermalGenerators, default=0.0) # units are $/(MWhr^2).

def piecewise_init(m, g):
    return []

model.CostPiecewisePoints = Set(model.ThermalGenerators, initialize=piecewise_init, ordered=True)
model.CostPiecewiseValues = Set(model.ThermalGenerators, initialize=piecewise_init, ordered=True)

#FC_j
model.FuelCost = Param(model.ThermalGenerators, default=1.0) # Sets the cost of fuel to the generator.  Defaults to 1 so that we could just input cost as heat rates.

# Minimum production cost (needed because Piecewise constraint on ProductionCost 
# has to have lower bound of 0, so the unit can cost 0 when off -- this is added
# back in to the objective if a unit is on
def minimum_production_cost(m, g):
    if len(m.CostPiecewisePoints[g]) > 1:
        return m.CostPiecewiseValues[g].first() * m.FuelCost[g]
    elif len(m.CostPiecewisePoints[g]) == 1:
        # If there's only one piecewise point given, that point should be (MaxPower, MaxCost) -- i.e. the cost function is linear through (0,0),
        # so we can find the slope of the line and use that to compute the cost of running at minimum generation
        return m.MinimumPowerOutput[g] * (m.CostPiecewiseValues[g].first() / m.MaximumPowerOutput[g]) * m.FuelCost[g]
    else:
        return  m.FuelCost[g] * \
               (m.ProductionCostA0[g] + \
                m.ProductionCostA1[g] * m.MinimumPowerOutput[g] + \
                m.ProductionCostA2[g] * m.MinimumPowerOutput[g]**2)
model.MinimumProductionCost = Param(model.ThermalGenerators, within=NonNegativeReals, initialize=minimum_production_cost, mutable=True)

##############################################################################################
# number of pieces in the linearization of each generator's quadratic cost production curve. #
##############################################################################################

#NL_j Higher the num pieces, better approx -> but harder to solve. Same for all genco offer curves
model.NumGeneratorCostCurvePieces = Param(within=PositiveIntegers, default=2, mutable=True)

#######################################################################
# points for piecewise linearization of power generation cost curves. #
#######################################################################

# maps a (generator, time-index) pair to a list of points defining the piecewise cost linearization breakpoints.
# the time index is redundant, but required. in the Piecewise construct, the breakpoints must be indexed the
# same as the Piecewise construct itself.

model.PowerGenerationPiecewisePoints = {}
model.PowerGenerationPiecewiseValues = {}

def power_generation_piecewise_points_rule(m, g, t):
    if len(m.CostPiecewisePoints[g]) > 0:
        m.PowerGenerationPiecewisePoints[g,t] = list(m.CostPiecewisePoints[g])
        temp = list(m.CostPiecewiseValues[g])
        m.PowerGenerationPiecewiseValues[g,t] = {}
        for i in range(len(m.CostPiecewisePoints[g])):
            m.PowerGenerationPiecewiseValues[g,t][m.PowerGenerationPiecewisePoints[g,t][i]] = temp[i] - m.MinimumProductionCost[g]
        # MinimumPowerOutput will be one of our piecewise points, so it is safe to add (0,0)
        if m.PowerGenerationPiecewisePoints[g,t][0] != 0:
            m.PowerGenerationPiecewisePoints[g,t].insert(0,0)
        m.PowerGenerationPiecewiseValues[g,t][0] = 0
    elif value(m.ProductionCostA2[g]) == 0:
        # If cost is linear, we only need two points -- (0,CostA0-MinCost) and (MaxOutput, MaxCost)
        m.PowerGenerationPiecewisePoints[g, t] = [0, value(m.MaximumPowerOutput[g])]
        m.PowerGenerationPiecewiseValues[g,t] = {}
        m.PowerGenerationPiecewiseValues[g,t][0] = value(m.ProductionCostA0[g] - m.MinimumProductionCost[g])
        m.PowerGenerationPiecewiseValues[g,t][m.PowerGenerationPiecewisePoints[g,t][1]] = \
                value(m.ProductionCostA0[g]) + \
                value(m.ProductionCostA1[g]) * m.PowerGenerationPiecewisePoints[g, t][1] \
                - value(m.MinimumProductionCost[g])
    else:
        min_power = value(m.MinimumPowerOutput[g])
        max_power = value(m.MaximumPowerOutput[g])
        n = value(m.NumGeneratorCostCurvePieces)
        width = (max_power - min_power) / float(n)
        if width == 0:
            m.PowerGenerationPiecewisePoints[g, t] = [min_power]
        else:
            m.PowerGenerationPiecewisePoints[g, t] = [min_power + i*width for i in range(0,n+1)]
        m.PowerGenerationPiecewiseValues[g,t] = {}
        for i in range(n+1):
            m.PowerGenerationPiecewiseValues[g,t][m.PowerGenerationPiecewisePoints[g,t][i]] = \
                       value(m.ProductionCostA0[g]) + \
                       value(m.ProductionCostA1[g]) * m.PowerGenerationPiecewisePoints[g, t][i] + \
                       value(m.ProductionCostA2[g]) * m.PowerGenerationPiecewisePoints[g, t][i]**2 \
                       - value(m.MinimumProductionCost[g])
        if m.PowerGenerationPiecewisePoints[g, t][0] != 0:
            m.PowerGenerationPiecewisePoints[g, t].insert(0,0)
            m.PowerGenerationPiecewiseValues[g, t][0] = 0

model.CreatePowerGenerationPiecewisePoints = BuildAction(model.ThermalGenerators * model.TimePeriods, rule=power_generation_piecewise_points_rule)

# a function for use in piecewise linearization of the cost function.
def production_cost_function(m, g, t, x):
    return m.TimePeriodLength * m.PowerGenerationPiecewiseValues[g,t][x] * m.FuelCost[g]

###############################################
# startup cost parameters for each generator. #
###############################################

#CSH_j
model.ColdStartHours = Param(model.ThermalGenerators, within=NonNegativeIntegers, default=0) # units are hours.

#HSC_j
model.HotStartCost = Param(model.ThermalGenerators, within=NonNegativeReals, default=0.0) # units are $.
#CSC_j
model.ColdStartCost = Param(model.ThermalGenerators, within=NonNegativeReals, default=0.0) # units are $.

##################################################################################
# shutdown cost for each generator. in the literature, these are often set to 0. #
##################################################################################

model.ShutdownCostCoefficient = Param(model.ThermalGenerators, within=NonNegativeReals, default=0.0) # units are $.

#
# STORAGE parameters
#

# \cal{S}
model.Storage = Set()
# \cal{S}(b} \subseteq \cal{S}
model.StorageAtBus = Set(model.Buses, initialize=Set())

####################################################################################
# minimum and maximum power ratings, for each storage unit. units are MW.          #
# could easily be specified on a per-time period basis, but are not currently.     #
####################################################################################

# Storage power output >0 when discharging

#\underbar{POS}_s
model.MinimumPowerOutputStorage = Param(model.Storage, within=NonNegativeReals, default=0.0)

def maximum_power_output_validator_storage(m, v, s):
   return v >= value(m.MinimumPowerOutputStorage[s])

#\overbar{POS}_s
model.MaximumPowerOutputStorage = Param(model.Storage, within=NonNegativeReals, validate=maximum_power_output_validator_storage, default=0.0)

#Storage power input >0 when charging

#\underbar{PIS}_s
model.MinimumPowerInputStorage = Param(model.Storage, within=NonNegativeReals, default=0.0)

def maximum_power_input_validator_storage(m, v, s):
   return v >= value(m.MinimumPowerInputStorage[s])

#\overbar{PIS}_s
model.MaximumPowerInputStorage = Param(model.Storage, within=NonNegativeReals, validate=maximum_power_input_validator_storage, default=0.0)

###############################################
# storage ramp up/down rates. units are MW/h. #
###############################################

# ramp rate limits when discharging
#NRUOS_s
model.NominalRampUpLimitStorageOutput    = Param(model.Storage, within=NonNegativeReals)
#NRDOS_s
model.NominalRampDownLimitStorageOutput  = Param(model.Storage, within=NonNegativeReals)

# ramp rate limits when charging
#NRUIS_s
model.NominalRampUpLimitStorageInput     = Param(model.Storage, within=NonNegativeReals)
#NRDIS_s
model.NominalRampDownLimitStorageInput   = Param(model.Storage, within=NonNegativeReals)

def scale_storage_ramp_up_out(m, s):
    return m.NominalRampUpLimitStorageOutput[s] * m.TimePeriodLength
model.ScaledNominalRampUpLimitStorageOutput = Param(model.Storage, within=NonNegativeReals, initialize=scale_storage_ramp_up_out)

def scale_storage_ramp_down_out(m, s):
    return m.NominalRampDownLimitStorageOutput[s] * m.TimePeriodLength
model.ScaledNominalRampDownLimitStorageOutput = Param(model.Storage, within=NonNegativeReals, initialize=scale_storage_ramp_down_out)

def scale_storage_ramp_up_in(m, s):
    return m.NominalRampUpLimitStorageInput[s] * m.TimePeriodLength
model.ScaledNominalRampUpLimitStorageInput = Param(model.Storage, within=NonNegativeReals, initialize=scale_storage_ramp_up_in)

def scale_storage_ramp_down_in(m, s):
    return m.NominalRampDownLimitStorageInput[s] * m.TimePeriodLength
model.ScaledNominalRampDownLimitStorageInput = Param(model.Storage, within=NonNegativeReals, initialize=scale_storage_ramp_down_in)

####################################################################################
# minimum state of charge (SOC) and maximum energy ratings, for each storage unit. #
# units are MWh for energy rating and p.u. (i.e. [0,1]) for SOC     #
####################################################################################

# you enter storage energy ratings once for each storage unit

#\overbar{ES}_s
model.MaximumEnergyStorage = Param(model.Storage, within=NonNegativeReals, default=0.0)
#\underbar{SOC}_s
model.MinimumSocStorage = Param(model.Storage, within=PercentFraction, default=0.0)

################################################################################
# round trip efficiency for each storage unit given as a fraction (i.e. [0,1]) #
################################################################################

#\eta_s
model.EfficiencyEnergyStorage = Param(model.Storage, within=PercentFraction, default=1.0)

########################################################################
# end-point SOC for each storage unit. units are in p.u. (i.e. [0,1])  #
########################################################################

# end-point values are the SOC targets at the final time period. With no end-point constraints
# storage units will always be empty at the final time period.

#EPSOC_s
model.EndPointSocStorage = Param(model.Storage, within=PercentFraction, default=0.5)

############################################################
# storage initial conditions: SOC, power output and input  #
############################################################

def t0_storage_power_input_validator(m, v, s):
    return (v >= value(m.MinimumPowerInputStorage[s])) and (v <= value(m.MaximumPowerInputStorage[s]))

def t0_storage_power_output_validator(m, v, s):
    return (v >= value(m.MinimumPowerInputStorage[s])) and (v <= value(m.MaximumPowerInputStorage[s]))

#\overbar{x}_s(0)
model.StoragePowerOutputOnT0 = Param(model.Storage, within=NonNegativeIntegers, validate=t0_storage_power_output_validator, default=0)
#\underbar{x}_s(0)
model.StoragePowerInputOnT0  = Param(model.Storage, within=NonNegativeIntegers, validate=t0_storage_power_input_validator, default=0)
#SOC_S(0)
model.StorageSocOnT0         = Param(model.Storage, within=PercentFraction, default=0.5)


#########################################
# penalty costs for constraint violation #
#########################################

BigPenalty = 1e6
#\Lambda
model.LoadMismatchPenalty = Param(within=NonNegativeReals, default=BigPenalty)

#
# Variables
#

# Total demand for reserve requirement
model.TotalDemand = Var(model.TimePeriods, within=NonNegativeReals)

def calculate_total_demand(m, t):
    return m.TotalDemand[t] == sum(m.Demand[b,t] for b in m.Buses)

model.CalculateTotalDemand = Constraint(model.TimePeriods, rule=calculate_total_demand)

######################
# decision variables #
######################

# indicator variables for each generator, at each time period.
model.UnitOn = Var(model.ThermalGenerators, model.TimePeriods, within=Binary, initialize=1) 

# amount of power produced by each generator, at each time period.
def power_bounds_rule(m, g, t):
    return (0, m.MaximumPowerOutput[g])
model.PowerGenerated = Var(model.ThermalGenerators, model.TimePeriods, within=NonNegativeReals, bounds=power_bounds_rule) 

# amount of power flowing along each line, at each time period
def line_power_bounds_rule(m, l, t):
   return (-m.ThermalLimit[l], m.ThermalLimit[l])
model.LinePower = Var(model.TransmissionLines, model.TimePeriods, bounds=line_power_bounds_rule)

# assume wind can be curtailed, then wind power is a decision variable
def nd_bounds_rule(m,n,t):
    return (m.MinNondispatchablePower[n,t], m.MaxNondispatchablePower[n,t])
model.NondispatchablePowerUsed = Var(model.NondispatchableGenerators, model.TimePeriods, within=NonNegativeReals, bounds=nd_bounds_rule)

# maximum power output for each generator, at each time period.
model.MaximumPowerAvailable = Var(model.ThermalGenerators, model.TimePeriods, within=NonNegativeReals)

# voltage angles at the buses (S) (lock the first bus at 0) in radians
model.Angle = Var(model.Buses, model.TimePeriods, within=Reals, bounds=(-3.14159265,3.14159265))

def fix_first_angle_rule(m,t):
    return m.Angle[m.Buses[1],t] == 0.0
model.FixFirstAngle = Constraint(model.TimePeriods, rule=fix_first_angle_rule)

##############################
# Storage decision variables #
##############################

# binary variables for storage (input/output are semicontinuous)
model.OutputStorage = Var(model.Storage, model.TimePeriods, within=Binary)
model.InputStorage = Var(model.Storage, model.TimePeriods, within=Binary)

# amount of output power of each storage unit, at each time period
def power_output_storage_bounds_rule(m, s, t):
    return (0, m.MaximumPowerOutputStorage[s])
model.PowerOutputStorage = Var(model.Storage, model.TimePeriods, within=NonNegativeReals, bounds=power_output_storage_bounds_rule)

# amount of input power of each storage unit, at each time period
def power_input_storage_bounds_rule(m, s, t):
    return (0, m.MaximumPowerInputStorage[s])
model.PowerInputStorage = Var(model.Storage, model.TimePeriods, within=NonNegativeReals, bounds=power_input_storage_bounds_rule)

# state of charge of each storage unit, at each time period
model.SocStorage = Var(model.Storage, model.TimePeriods, within=PercentFraction)

###################
# cost components #
###################

# production cost associated with each generator, for each time period.
model.ProductionCost = Var(model.ThermalGenerators, model.TimePeriods, within=NonNegativeReals)

# startup and shutdown costs for each generator, each time period.
model.StartupCost = Var(model.ThermalGenerators, model.TimePeriods, within=NonNegativeReals)
model.ShutdownCost = Var(model.ThermalGenerators, model.TimePeriods, within=NonNegativeReals)

# (implicit) binary denoting whether starting up a generator will cost HotStartCost or ColdStartCost
model.HotStart = Var(model.ThermalGenerators, model.TimePeriods, bounds=(0,1))

# cost over all generators, for all time periods.
"""model.TotalProductionCost = Var(within=NonNegativeReals)"""

# all other overhead / fixed costs, e.g., associated with startup and shutdown.
"""model.TotalFixedCost = Var(within=NonNegativeReals)"""
############################################### KICK THAT OUT ?

#####################################################
# load "shedding" can be both positive and negative #
#####################################################
model.LoadGenerateMismatch = Var(model.Buses, model.TimePeriods, within = Reals, initialize=0)
model.posLoadGenerateMismatch = Var(model.Buses, model.TimePeriods, within = NonNegativeReals, initialize=0)
model.negLoadGenerateMismatch = Var(model.Buses, model.TimePeriods, within = NonNegativeReals, initialize=0)

model.GlobalLoadGenerateMismatch = Var(model.TimePeriods, within = Reals, initialize=0)
model.posGlobalLoadGenerateMismatch = Var(model.TimePeriods, within = NonNegativeReals, initialize=0)
model.negGlobalLoadGenerateMismatch = Var(model.TimePeriods, within = NonNegativeReals, initialize=0)

# the following constraints are necessarily, at least in the case of CPLEX 12.4, to prevent
# the appearance of load generation mismatch component values in the range of *negative* e-5.
# what these small negative values do is to cause the optimal objective to be a very large negative,
# due to obviously large penalty values for under or over-generation. JPW would call this a heuristic
# at this point, but it does seem to work broadly. we tried a single global constraint, across all
# buses, but that failed to correct the problem, and caused the solve times to explode.

def pos_load_generate_mismatch_tolerance_rule(m, b):
   return sum((m.posLoadGenerateMismatch[b,t] for t in m.TimePeriods)) >= 0.0
model.PosLoadGenerateMismatchTolerance = Constraint(model.Buses, rule=pos_load_generate_mismatch_tolerance_rule)

def neg_load_generate_mismatch_tolerance_rule(m, b):
   return sum((m.negLoadGenerateMismatch[b,t] for t in m.TimePeriods)) >= 0.0
model.NegLoadGenerateMismatchTolerance = Constraint(model.Buses, rule=neg_load_generate_mismatch_tolerance_rule)

#################################################
# per-stage cost variables - necessary for PySP #
#################################################

"""model.FirstStageCost = Var(within=NonNegativeReals)
model.SecondStageCost = Var(within=NonNegativeReals)"""
######################################################### KICK THAT OUT ?

#
# Constraints
#

def line_power_rule(m, l, t):
   return m.LinePower[l,t] == m.B[l] * (m.Angle[m.BusFrom[l], t] - m.Angle[m.BusTo[l], t])
model.CalculateLinePower = Constraint(model.TransmissionLines, model.TimePeriods, rule=line_power_rule)

# Power balance at each node (S)
def power_balance(m, b, t):
    # bus b, time t (S)
    return sum(m.PowerGenerated[g, t] for g in m.ThermalGeneratorsAtBus[b]) \
           + sum(m.PowerOutputStorage[s, t] for s in m.StorageAtBus[b])\
           - sum(m.PowerInputStorage[s, t] for s in m.StorageAtBus[b])\
           + sum(m.NondispatchablePowerUsed[g, t] for g in m.NondispatchableGeneratorsAtBus[b]) \
           + sum(m.LinePower[l,t] for l in m.LinesTo[b]) \
           - sum(m.LinePower[l,t] for l in m.LinesFrom[b]) \
           + m.LoadGenerateMismatch[b,t] \
           == m.Demand[b, t] 
model.PowerBalance = Constraint(model.Buses, model.TimePeriods, rule=power_balance)

# give meaning to the positive and negative parts of the mismatch
def posneg_rule(m, b, t):
    return m.posLoadGenerateMismatch[b, t] - m.negLoadGenerateMismatch[b, t] == m.LoadGenerateMismatch[b, t]
model.Defineposneg_Mismatch = Constraint(model.Buses, model.TimePeriods, rule = posneg_rule)

def global_posneg_rule(m, t):
    return m.posGlobalLoadGenerateMismatch[t] - m.negGlobalLoadGenerateMismatch[t] == m.GlobalLoadGenerateMismatch[t]
model.Global_Defineposneg_Mismatch = Constraint(model.TimePeriods, rule = global_posneg_rule)

# ensure there is sufficient maximal power output available to meet both the
# demand and the spinning reserve requirements in each time period.
# encodes Constraint 3 in Carrion and Arroyo.

def enforce_reserve_requirements_rule(m, t):
    return sum(m.MaximumPowerAvailable[g, t] for g in m.ThermalGenerators) \
           + sum(m.NondispatchablePowerUsed[n,t] for n in m.NondispatchableGenerators) \
           + sum(m.PowerOutputStorage[s,t] for s in m.Storage) \
              == \
           m.TotalDemand[t] + m.ReserveRequirement[t] + m.GlobalLoadGenerateMismatch[t]

model.EnforceReserveRequirements = Constraint(model.TimePeriods, rule=enforce_reserve_requirements_rule)

############################################
# generation limit and ramping constraints #
############################################

# enforce the generator power output limits on a per-period basis.
# the maximum power available at any given time period is dynamic,
# bounded from above by the maximum generator output.

# the following three constraints encode Constraints 16 and 17 defined in Carrion and Arroyo.

# NOTE: The expression below is what we really want - however, due to a pyomo design feature, we have to split it into two constraints:
# m.MinimumPowerOutput[g] * m.UnitOn[g, t] <= m.PowerGenerated[g,t] <= m.MaximumPowerAvailable[g, t] <= m.MaximumPowerOutput[g] * m.UnitOn[g, t]

def enforce_generator_output_limits_rule_part_a(m, g, t):
   return m.MinimumPowerOutput[g] * m.UnitOn[g, t] <= m.PowerGenerated[g,t]

model.EnforceGeneratorOutputLimitsPartA = Constraint(model.ThermalGenerators, model.TimePeriods, rule=enforce_generator_output_limits_rule_part_a)

def enforce_generator_output_limits_rule_part_b(m, g, t):
   return m.PowerGenerated[g,t] <= m.MaximumPowerAvailable[g, t]

model.EnforceGeneratorOutputLimitsPartB = Constraint(model.ThermalGenerators, model.TimePeriods, rule=enforce_generator_output_limits_rule_part_b)

def enforce_generator_output_limits_rule_part_c(m, g, t):
   return m.MaximumPowerAvailable[g,t] <= m.MaximumPowerOutput[g] * m.UnitOn[g, t]

model.EnforceGeneratorOutputLimitsPartC = Constraint(model.ThermalGenerators, model.TimePeriods, rule=enforce_generator_output_limits_rule_part_c)

# note: as of 9 Feb 2012 wind is done using Var bounds

# impose upper bounds on the maximum power available for each generator in each time period,
# based on standard and start-up ramp limits.

# the following constraint encodes Constraint 18 defined in Carrion and Arroyo.

def enforce_max_available_ramp_up_rates_rule(m, g, t):
   # 4 cases, split by (t-1, t) unit status (RHS is defined as the delta from m.PowerGenerated[g, t-1])
   # (0, 0) - unit staying off:   RHS = maximum generator output (degenerate upper bound due to unit being off)
   # (0, 1) - unit switching on:  RHS = startup ramp limit
   # (1, 0) - unit switching off: RHS = standard ramp limit minus startup ramp limit plus maximum power output (degenerate upper bound due to unit off)
   # (1, 1) - unit staying on:    RHS = standard ramp limit plus power generated in previous time period
   if t == 1:
      return m.MaximumPowerAvailable[g, t] <= m.PowerGeneratedT0[g] + \
                                              m.ScaledNominalRampUpLimit[g] * m.UnitOnT0[g] + \
                                              m.ScaledStartupRampLimit[g] * (m.UnitOn[g, t] - m.UnitOnT0[g]) + \
                                              m.MaximumPowerOutput[g] * (1 - m.UnitOn[g, t])
   else:
      return m.MaximumPowerAvailable[g, t] <= m.PowerGenerated[g, t-1] + \
                                              m.ScaledNominalRampUpLimit[g] * m.UnitOn[g, t-1] + \
                                              m.ScaledStartupRampLimit[g] * (m.UnitOn[g, t] - m.UnitOn[g, t-1]) + \
                                              m.MaximumPowerOutput[g] * (1 - m.UnitOn[g, t])

model.EnforceMaxAvailableRampUpRates = Constraint(model.ThermalGenerators, model.TimePeriods, rule=enforce_max_available_ramp_up_rates_rule)

# the following constraint encodes Constraint 19 defined in Carrion and Arroyo.

def enforce_max_available_ramp_down_rates_rule(m, g, t):
   # 4 cases, split by (t, t+1) unit status
   # (0, 0) - unit staying off:   RHS = 0 (degenerate upper bound)
   # (0, 1) - unit switching on:  RHS = maximum generator output minus shutdown ramp limit (degenerate upper bound) - this is the strangest case.
   # (1, 0) - unit switching off: RHS = shutdown ramp limit
   # (1, 1) - unit staying on:    RHS = maximum generator output (degenerate upper bound)
#NOTE: As expressed in Carrion-Arroyo and subsequently here, this constraint does NOT consider ramp down from initial conditions to t=1!
   #if t == value(m.NumTimePeriods):
   #   return Constraint.Skip
   #else:
   #   return m.MaximumPowerAvailable[g, t] <= \
   #          m.MaximumPowerOutput[g] * m.UnitOn[g, t+1] + \
   #          m.ScaledShutdownRampLimit[g] * (m.UnitOn[g, t] - m.UnitOn[g, t+1])

   #This version fixes the problem with ignoring initial conditions mentioned in the above note
   if t == 1:
       # Not 100% sure of this one since there is no MaximumPowerAvailableT0
       return m.PowerGeneratedT0[g] <= \
                 m.MaximumPowerOutput[g] * m.UnitOn[g,t] + \
                 m.ScaledShutdownRampLimit[g] * (m.UnitOnT0[g] - m.UnitOn[g,t])
   else:
      return m.MaximumPowerAvailable[g, t-1] <= \
                 m.MaximumPowerOutput[g] * m.UnitOn[g, t] + \
                 m.ScaledShutdownRampLimit[g] * (m.UnitOn[g, t-1] - m.UnitOn[g, t])

model.EnforceMaxAvailableRampDownRates = Constraint(model.ThermalGenerators, model.TimePeriods, rule=enforce_max_available_ramp_down_rates_rule)

# the following constraint encodes Constraint 20 defined in Carrion and Arroyo.

def enforce_ramp_down_limits_rule(m, g, t):
   # 4 cases, split by (t-1, t) unit status:
   # (0, 0) - unit staying off:   RHS = maximum generator output (degenerate upper bound)
   # (0, 1) - unit switching on:  RHS = standard ramp-down limit minus shutdown ramp limit plus maximum generator output - this is the strangest case.
#NOTE: This may never be physically true, but if a generator has ScaledShutdownRampLimit >> MaximumPowerOutput, this constraint causes problems
   # (1, 0) - unit switching off: RHS = shutdown ramp limit
   # (1, 1) - unit staying on:    RHS = standard ramp-down limit
   if t == 1:
      return m.PowerGeneratedT0[g] - m.PowerGenerated[g, t] <= \
             m.ScaledNominalRampDownLimit[g] * m.UnitOn[g, t] + \
             m.ScaledShutdownRampLimit[g]  * (m.UnitOnT0[g] - m.UnitOn[g, t]) + \
             m.MaximumPowerOutput[g] * (1 - m.UnitOnT0[g])
   else:
      return m.PowerGenerated[g, t-1] - m.PowerGenerated[g, t] <= \
             m.ScaledNominalRampDownLimit[g]  * m.UnitOn[g, t] + \
             m.ScaledShutdownRampLimit[g]  * (m.UnitOn[g, t-1] - m.UnitOn[g, t]) + \
             m.MaximumPowerOutput[g] * (1 - m.UnitOn[g, t-1])

model.EnforceScaledNominalRampDownLimits = Constraint(model.ThermalGenerators, model.TimePeriods, rule=enforce_ramp_down_limits_rule)


#######################################
# energy storage bounding constraints #
#######################################
# NOTE: The expressions below are what we really want - however, due to a pyomo design feature, we have to split it into two constraints:
# m.MinimumPowerInputStorage[g] * m.InputStorage[g, t] <= m.StoragePowerInput[g,t] <= m.MaximumPowerInputStorage[g] * m.InputStorage[g, t]
# m.MinimumPowerOutputStorage[g] * m.OutputStorage[g, t] <= m.StoragePowerOutput[g,t] <= m.MaximumPowerOutputStorage[g] * m.OutputStorage[g, t]

def enforce_storage_input_limits_rule_part_a(m, s, t):
   return m.MinimumPowerInputStorage[s] * m.InputStorage[s, t] <= m.PowerInputStorage[s,t]

model.EnforceStorageInputLimitsPartA = Constraint(model.Storage, model.TimePeriods, rule=enforce_storage_input_limits_rule_part_a)

def enforce_storage_input_limits_rule_part_b(m, s, t):
   return m.PowerInputStorage[s,t] <= m.MaximumPowerInputStorage[s] * m.InputStorage[s, t]

model.EnforceStorageInputLimitsPartB = Constraint(model.Storage, model.TimePeriods, rule=enforce_storage_input_limits_rule_part_b)

def enforce_storage_output_limits_rule_part_a(m, s, t):
   return m.MinimumPowerOutputStorage[s] * m.OutputStorage[s, t] <= m.PowerOutputStorage[s,t]

model.EnforceStorageOutputLimitsPartA = Constraint(model.Storage, model.TimePeriods, rule=enforce_storage_output_limits_rule_part_a)

def enforce_storage_output_limits_rule_part_b(m, s, t):
   return m.PowerOutputStorage[s,t] <= m.MaximumPowerOutputStorage[s] * m.OutputStorage[s, t]

model.EnforceStorageOutputLimitsPartB = Constraint(model.Storage, model.TimePeriods, rule=enforce_storage_output_limits_rule_part_b)


def enforce_input_output_exclusivity_rule(m, s, t):
    return m.PowerOutputStorage[s,t] + m.PowerInputStorage[s,t] <= 1

#model.EnforceInputOutputExclusivity = Constraint(model.Storage, model.TimePeriods, rule=enforce_input_output_exclusivity_rule)

#####################################
# energy storage ramping contraints #
#####################################

def enforce_ramp_up_rates_power_output_storage_rule(m, s, t):
   if t == 1:
      return m.PowerOutputStorage[s, t] <= m.StoragePowerOutputOnT0[s] + m.ScaledNominalRampUpLimitStorageOutput[s]
   else:
      return m.PowerOutputStorage[s, t] <= m.PowerOutputStorage[s, t-1] + m.ScaledNominalRampUpLimitStorageOutput[s]

model.EnforceStorageOutputRampUpRates = Constraint(model.Storage, model.TimePeriods, rule=enforce_ramp_up_rates_power_output_storage_rule)

def enforce_ramp_down_rates_power_output_storage_rule(m, s, t):
   if t == 1:
      return m.PowerOutputStorage[s, t] >= m.StoragePowerOutputOnT0[s] - m.ScaledNominalRampDownLimitStorageOutput[s]
   else:
      return m.PowerOutputStorage[s, t] >= m.PowerOutputStorage[s, t-1] - m.ScaledNominalRampDownLimitStorageOutput[s]

model.EnforceStorageOutputRampDownRates = Constraint(model.Storage, model.TimePeriods, rule=enforce_ramp_down_rates_power_output_storage_rule)

def enforce_ramp_up_rates_power_input_storage_rule(m, s, t):
   if t == 1:
      return m.PowerInputStorage[s, t] <= m.StoragePowerInputOnT0[s] + m.ScaledNominalRampUpLimitStorageInput[s]
   else:
      return m.PowerInputStorage[s, t] <= m.PowerInputStorage[s, t-1] + m.ScaledNominalRampUpLimitStorageInput[s]

model.EnforceStorageInputRampUpRates = Constraint(model.Storage, model.TimePeriods, rule=enforce_ramp_up_rates_power_input_storage_rule)

def enforce_ramp_down_rates_power_input_storage_rule(m, s, t):
   if t == 1:
      return m.PowerInputStorage[s, t] >= m.StoragePowerInputOnT0[s] - m.ScaledNominalRampDownLimitStorageInput[s]
   else:
      return m.PowerInputStorage[s, t] >= m.PowerInputStorage[s, t-1] - m.ScaledNominalRampDownLimitStorageInput[s]

model.EnforceStorageInputRampDownRates = Constraint(model.Storage, model.TimePeriods, rule=enforce_ramp_down_rates_power_input_storage_rule)

##########################################
# storage energy conservation constraint #
##########################################

def energy_conservation_rule(m, s, t):
    # storage s, time t
    if t == 1:
        return m.SocStorage[s, t] == m.StorageSocOnT0[s]  + \
               (- m.PowerOutputStorage[s, t] + m.PowerInputStorage[s,t]*m.EfficiencyEnergyStorage[s])/m.MaximumEnergyStorage[s]
    else:
        return m.SocStorage[s, t] == m.SocStorage[s, t-1]  + \
               (- m.PowerOutputStorage[s, t] + m.PowerInputStorage[s,t]*m.EfficiencyEnergyStorage[s])/m.MaximumEnergyStorage[s]
model.EnergyConservation = Constraint(model.Storage, model.TimePeriods, rule=energy_conservation_rule)

##################################
# storage end-point constraints  #
##################################

def storage_end_point_soc_rule(m, s):
    # storage s, last time period
    return m.SocStorage[s, value(m.NumTimePeriods)] == m.EndPointSocStorage[s]
#model.EnforceEndPointSocStorage = Constraint(model.Storage, rule=storage_end_point_soc_rule)

#############################################
# constraints for computing cost components #
#############################################

# compute the per-generator, per-time period production costs. this is a "simple" piecewise linear construct.
# the first argument to piecewise is the index set. the second and third arguments are respectively the input and output variables.
model.ComputeProductionCosts = Piecewise(model.ThermalGenerators * model.TimePeriods, model.ProductionCost, model.PowerGenerated, pw_pts=model.PowerGenerationPiecewisePoints, f_rule=production_cost_function, pw_constr_type='LB')


# compute the total production costs, across all generators and time periods.
"""def compute_total_production_cost_rule(m):
   return m.TotalProductionCost == sum(m.ProductionCost[g, t] for g in m.ThermalGenerators for t in m.TimePeriods)

model.ComputeTotalProductionCost = Constraint(rule=compute_total_production_cost_rule)"""
####################################### KICK THAT ONE OUT


# compute startup costs for each generator, for each time period
def compute_hot_start_rule(m, g, t):
    if t <= value(m.ColdStartHours[g]):
        if t - value(m.ColdStartHours[g]) <= value(m.UnitOnT0State[g]):
            m.HotStart[g, t] = 1
            m.HotStart[g, t].fixed = True
            return Constraint.Skip
        else:
            return m.HotStart[g, t] <= sum( m.UnitOn[g, i] for i in range(1, t) )
    else:
        return m.HotStart[g, t] <= sum( m.UnitOn[g, i] for i in range(t - m.ColdStartHours[g], t) )
        
model.ComputeHotStart = Constraint(model.ThermalGenerators, model.TimePeriods, rule=compute_hot_start_rule)

def compute_startup_costs_rule_minusM(m, g, t):
    if t == 1:
        return m.StartupCost[g, t] >= m.ColdStartCost[g] - (m.ColdStartCost[g] - m.HotStartCost[g])*m.HotStart[g, t] \
                                      - m.ColdStartCost[g]*(1 - (m.UnitOn[g, t] - m.UnitOnT0[g]))
    else:
        return m.StartupCost[g, t] >= m.ColdStartCost[g] - (m.ColdStartCost[g] - m.HotStartCost[g])*m.HotStart[g, t] \
                                      - m.ColdStartCost[g]*(1 - (m.UnitOn[g, t] - m.UnitOn[g, t-1]))

model.ComputeStartupCostsMinusM = Constraint(model.ThermalGenerators, model.TimePeriods, rule=compute_startup_costs_rule_minusM)

# compute the per-generator, per-time period shutdown costs.
def compute_shutdown_costs_rule(m, g, t):
   if t is 1:
      return m.ShutdownCost[g, t] >= m.ShutdownCostCoefficient[g] * (m.UnitOnT0[g] - m.UnitOn[g, t])
   else:
      return m.ShutdownCost[g, t] >= m.ShutdownCostCoefficient[g] * (m.UnitOn[g, t-1] - m.UnitOn[g, t])

model.ComputeShutdownCosts = Constraint(model.ThermalGenerators, model.TimePeriods, rule=compute_shutdown_costs_rule)

"""# compute the total startup and shutdown costs, across all generators and time periods.
def compute_total_fixed_cost_rule(m):
   return m.TotalFixedCost == sum(m.StartupCost[g, t] + m.ShutdownCost[g, t] for g in m.ThermalGenerators for t in m.TimePeriods)

model.ComputeTotalFixedCost = Constraint(rule=compute_total_fixed_cost_rule)"""
########################################## KICK THAT OUT ?


#######################
# up-time constraints #
#######################

# constraint due to initial conditions.
def enforce_up_time_constraints_initial(m, g):
   if value(m.InitialTimePeriodsOnLine[g]) == 0:
      return Constraint.Skip
   return sum((1 - m.UnitOn[g, t]) for t in m.TimePeriods if t <= value(m.InitialTimePeriodsOnLine[g])) == 0.0

model.EnforceUpTimeConstraintsInitial = Constraint(model.ThermalGenerators, rule=enforce_up_time_constraints_initial)

# constraint for each time period after that not involving the initial condition.
@simple_constraint_rule
def enforce_up_time_constraints_subsequent(m, g, t):
   if t <= value(m.InitialTimePeriodsOnLine[g]):
      # handled by the EnforceUpTimeConstraintInitial constraint.
      return Constraint.Skip
   elif t <= (value(m.NumTimePeriods - m.ScaledMinimumUpTime[g]) + 1):
      # the right-hand side terms below are only positive if the unit was off in the previous time period but on in this one =>
      # the value is the minimum number of subsequent consecutive time periods that the unit is required to be on.
      if t == 1:
         return sum(m.UnitOn[g, n] for n in m.TimePeriods if n >= t and n <= (t + value(m.ScaledMinimumUpTime[g]) - 1)) >= \
                m.ScaledMinimumUpTime[g] * (m.UnitOn[g, t] - m.UnitOnT0[g])
      else:
         return sum(m.UnitOn[g, n] for n in m.TimePeriods if n >= t and n <= (t + value(m.ScaledMinimumUpTime[g]) - 1)) >= \
                m.ScaledMinimumUpTime[g] * (m.UnitOn[g, t] - m.UnitOn[g, t-1])
   else:
      # handle the final (ScaledMinimumUpTime[g] - 1) time periods - if a unit is started up in
      # this interval, it must remain on-line until the end of the time span.
      if t == 1: # can happen when small time horizons are specified
         return sum((m.UnitOn[g, n] - (m.UnitOn[g, t] - m.UnitOnT0[g])) for n in m.TimePeriods if n >= t) >= 0.0
      else:
         return sum((m.UnitOn[g, n] - (m.UnitOn[g, t] - m.UnitOn[g, t-1])) for n in m.TimePeriods if n >= t) >= 0.0

model.EnforceUpTimeConstraintsSubsequent = Constraint(model.ThermalGenerators, model.TimePeriods, rule=enforce_up_time_constraints_subsequent)

#########################
# down-time constraints #
#########################

# constraint due to initial conditions.
def enforce_down_time_constraints_initial(m, g):
   if value(m.InitialTimePeriodsOffLine[g]) == 0:
      return Constraint.Skip
   return sum(m.UnitOn[g, t] for t in m.TimePeriods if t <= value(m.InitialTimePeriodsOffLine[g])) == 0.0

model.EnforceDownTimeConstraintsInitial = Constraint(model.ThermalGenerators, rule=enforce_down_time_constraints_initial)

# constraint for each time period after that not involving the initial condition.
@simple_constraint_rule
def enforce_down_time_constraints_subsequent(m, g, t):
   if t <= value(m.InitialTimePeriodsOffLine[g]):
      # handled by the EnforceDownTimeConstraintInitial constraint.
      return Constraint.Skip
   elif t <= (value(m.NumTimePeriods - m.ScaledMinimumDownTime[g]) + 1):
      # the right-hand side terms below are only positive if the unit was off in the previous time period but on in this one =>
      # the value is the minimum number of subsequent consecutive time periods that the unit is required to be on.
      if t == 1:
         return sum((1 - m.UnitOn[g, n]) for n in m.TimePeriods if n >= t and n <= (t + value(m.ScaledMinimumDownTime[g]) - 1)) >= \
                m.ScaledMinimumDownTime[g] * (m.UnitOnT0[g] - m.UnitOn[g, t])
      else:
         return sum((1 - m.UnitOn[g, n]) for n in m.TimePeriods if n >= t and n <= (t + value(m.ScaledMinimumDownTime[g]) - 1)) >= \
                m.ScaledMinimumDownTime[g] * (m.UnitOn[g, t-1] - m.UnitOn[g, t])
   else:
      # handle the final (ScaledMinimumDownTime[g] - 1) time periods - if a unit is shut down in
      # this interval, it must remain off-line until the end of the time span.
      if t == 1: # can happen when small time horizons are specified
         return sum(((1 - m.UnitOn[g, n]) - (m.UnitOnT0[g] - m.UnitOn[g, t])) for n in m.TimePeriods if n >= t) >= 0.0
      else:
         return sum(((1 - m.UnitOn[g, n]) - (m.UnitOn[g, t-1] - m.UnitOn[g, t])) for n in m.TimePeriods if n >= t) >= 0.0

model.EnforceDownTimeConstraintsSubsequent = Constraint(model.ThermalGenerators, model.TimePeriods, rule=enforce_down_time_constraints_subsequent)

# 
# Cost computations
#

# TBD: Add commitment costs for ancillary services - we aren't cost accounting quite correctly.
"""def first_stage_cost_rule(m):
    return m.FirstStageCost == m.TotalFixedCost + sum(sum(m.UnitOn[g, t] for t in m.TimePeriods) * m.MinimumProductionCost[g] * m.TimePeriodLength for g in m.ThermalGenerators)

model.ComputeFirstStageCost = Constraint(rule=first_stage_cost_rule) 

def second_stage_cost_rule(m):
    return m.SecondStageCost == m.TotalProductionCost + m.LoadMismatchPenalty * \
                (sum(m.posLoadGenerateMismatch[b, t] + m.negLoadGenerateMismatch[b, t] for b in m.Buses for t in m.TimePeriods) + \
                sum(m.posGlobalLoadGenerateMismatch[t] + m.negGlobalLoadGenerateMismatch[t] for t in m.TimePeriods))

model.ComputeSecondStageCost = Constraint(rule=second_stage_cost_rule)"""

def commitment_in_stage_st_cost_rule(m, st):
	return m.CommitmentStageCost[st] == (sum(m.StartupCost[g,t] + m.ShutdownCost[g,t] for g in m.ThermalGenerators for t in m.CommitmentTimeInStage[st]) + sum(sum(m.UnitOn[g,t] for t in m.CommitmentTimeInStage[st]) * m.MinimumProductionCost[g] * m.TimePeriodLength for g in m.ThermalGenerators))

model.Compute_commitment_in_stage_st_cost = Constraint(model.StageSet, rule = commitment_in_stage_st_cost_rule)
### NEW COMMITMENT COST RULE
#

def generation_in_stage_st_cost_rule(m, st):
	return m.GenerationStageCost[st] == sum(m.ProductionCost[g, t] for g in m.ThermalGenerators for t in m.GenerationTimeInStage[st]) + m.LoadMismatchPenalty * \
	(sum(m.posLoadGenerateMismatch[b, t] + m.negLoadGenerateMismatch[b, t] for b in m.Buses for t in m.GenerationTimeInStage[st]) + \
                sum(m.posGlobalLoadGenerateMismatch[t] + m.negGlobalLoadGenerateMismatch[t] for t in m.GenerationTimeInStage[st]))

	
model.Compute_generation_in_stage_st_cost = Constraint(model.StageSet, rule = generation_in_stage_st_cost_rule)
### NEW GENERATION COST RULE

def StageCost_rule(m, st):
	return m.StageCost[st] == m.GenerationStageCost[st] + m.CommitmentStageCost[st]
model.Compute_Stage_Cost = Constraint(model.StageSet, rule = StageCost_rule)

#
# Objectives
#

def total_cost_objective_rule(m):
   return sum(m.StageCost[st] for st in m.StageSet)	

model.TotalCostObjective = Objective(rule=total_cost_objective_rule, sense=minimize)

