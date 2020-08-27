
# AMES (V4.0)

[Agent based Modeling of Electricity Systems](http://www2.econ.iastate.edu/tesfatsi/AMESMarketHome.htm) V4.0 was developed by Dheepak Krishnamurthy, Sean L. Mooney, Auswin George Thomas, Wanning Li, and Leigh Tesfatsion at Iowa State University.

AMES V4.0 substantively modifies AMES V2.06 as follows:

* AMES V4.0 models a daily ISO-managed day-ahead market (DAM) formulated either as a deterministic SCUC optimization or as a two-stage stochastic SCUC optimization conditional on a set of user-specified net-load scenarios. These optimizations are based on Load Serving Entity (LSE) demand bids and Generation Company (GenCo) supply offers. DAM settlements are determined on the basis of locational marginal price (LMP) outcomes.

* AMES V4.0 includes an interface to a fast and efficient Python-based solver (Pyomo/PSST) for implementing the daily DAM SCUC optimization in either deterministic or stochastic form.

* AMES V4.0 permits spinning reserve requirements to be included among the system constraints for each optimization. For the stochastic DAM SCUC optimization, this inclusion provides a hedge against net-load scenario specification errors. 

### Install instructions

The [Install](https://github.com/ames-market/AMES-v4.0/blob/master/INSTALL.md) instructions describes install instructions for different operating systems.

### DATA

The DATA folder contains an example of an [8 Bus 8 Generator test case](https://github.com/ames-market/AMES-v4.0/blob/master/DATA/8BusTestCase_8gen.dat) as an illustrative example of the AMES data format.

### Usage

The [Usage](https://github.com/ames-market/AMES-v4.0/blob/master/USAGE.md) describes a list of commonly used settings.

### Source

AMES source code can be downloaded from [here](https://github.com/ames-market/AMES-v4.0/repository/archive.zip?ref=master).
The source can also be viewed online [here](https://github.com/ames-market/AMES-v4.0/tree/master).

### References

D. Krishnamurthy, W. Li and L. Tesfatsion, "An 8-Zone Test System Based on ISO New England Data: Development and Application," in IEEE Transactions on Power Systems, vol. 31, no. 1, pp. 234-246, Jan. 2016.

H. Li and L. Tesfatsion, "The AMES wholesale power market test bed: A computational laboratory for research, teaching, and training," 2009 IEEE Power & Energy Society General Meeting, Calgary, AB, 2009, pp. 1-8.
