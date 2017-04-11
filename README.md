
# AMES (V4.0)


[Agent based Modeling of Electricity Systems](http://www2.econ.iastate.edu/tesfatsi/AMESMarketHome.htm) (AMES V4.0) is a free open-source computational platform (Java/Python) permitting the small-scale study of U.S. ISO-managed wholesale power markets operating over AC transmission grids with congestion handled by locational marginal pricing.

AMES (V4.0) extends [AMES (V2.06)](http://www2.econ.iastate.edu/tesfatsi/AMESVersionReleaseHistory.htm) in three key ways:

* AMES (V4.0) models a fully operational two-settlement system consisting of an ISO-managed day-ahead market (DAM) settled at DAM LMPs and an ISO-managed real-time market (RTM) settled at RTM LMPs.
* AMES (V4.0) includes an enhanced modeling of ISO-managed DAM Security-Constrained Unit Commitment (SCUC) that takes into account UC costs such as start-up costs, up-time/down-time constraints, and ramping constraints.
* AMES (V4.0) includes reserve requirements in its DAM SCUC/SCED and RTM SCED optimal power flow formulations.

### Install instructions

The [Install](https://github.com/kdheepak/AMES-v4.0/blob/master/INSTALL.md) instructions describes install instructions for different operating systems

### DATA

The DATA folder contains an example of an [8 Bus 8 Generator test case](https://github.com/kdheepak/AMES-v4.0/blob/master/DATA/8BusTestCase_8gen.dat) as an illustrative example of the AMES data format

### Usage

The [Usage](https://github.com/kdheepak/AMES-v4.0/blob/master/USAGE.md) describes a list of commonly used settings.

### Source

AMES source code can be downloaded from [here](https://github.com/kdheepak/AMES-v4.0/repository/archive.zip?ref=master).
The source can also be viewed online [here](https://github.com/kdheepak/AMES-v4.0/tree/master)

### Contributions

AMES (V4.0) was developed by Dheepak Krishnamurthy, Sean Mooney, Auswin George, Wanning Li and Leigh Tesfatsion.

### References

D. Krishnamurthy, W. Li and L. Tesfatsion, "An 8-Zone Test System Based on ISO New England Data: Development and Application," in IEEE Transactions on Power Systems, vol. 31, no. 1, pp. 234-246, Jan. 2016.

H. Li and L. Tesfatsion, "The AMES wholesale power market test bed: A computational laboratory for research, teaching, and training," 2009 IEEE Power & Energy Society General Meeting, Calgary, AB, 2009, pp. 1-8.
