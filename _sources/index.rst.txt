AMES (V4.0)
===========

`Agent based Modeling of Electricity
Systems <http://www2.econ.iastate.edu/tesfatsi/AMESMarketHome.htm>`__
(AMES V4.0) is a free open-source computational platform (Java/Python)
permitting the small-scale study of U.S. ISO-managed wholesale power
markets operating over AC transmission grids with congestion handled by
locational marginal pricing.

AMES (V4.0) extends `AMES
(V2.06) <http://www2.econ.iastate.edu/tesfatsi/AMESVersionReleaseHistory.htm>`__
in three key ways:

-  AMES (V4.0) models a fully operational two-settlement system
   consisting of an ISO-managed day-ahead market (DAM) settled at DAM
   LMPs and an ISO-managed real-time market (RTM) settled at RTM LMPs.
-  AMES (V4.0) includes an enhanced modeling of ISO-managed DAM
   Security-Constrained Unit Commitment (SCUC) that takes into account
   UC costs (start-up costs, no-load costs, shut-down costs) 
   and additional system constraints (ramping and minimum up-time/down-time constraints).
-  AMES (V4.0) includes reserve requirements in its DAM SCUC/SCED and
   RTM SCED optimal power flow formulations.

Install instructions
~~~~~~~~~~~~~~~~~~~~

The
`Install <https://github.com/kdheepak/AMES-v4.0/blob/master/INSTALL.md>`__
instructions describes install instructions for different operating
systems

DATA
~~~~

The DATA folder contains an example of an `8 Bus 8 Generator test
case <https://github.com/kdheepak/AMES-v4.0/blob/master/DATA/8BusTestCase_8gen.dat>`__
as an illustrative example of the AMES data format

Usage
~~~~~

The
`Usage <https://github.com/kdheepak/AMES-v4.0/blob/master/USAGE.md>`__
describes a list of commonly used settings.

Source
~~~~~~

AMES source code can be downloaded as a zip file from
`here <https://github.com/kdheepak/AMES-v4.0/archive/master.zip>`__.
The source can also be viewed online
`here <https://github.com/kdheepak/AMES-v4.0/tree/master>`__

Contributions
~~~~~~~~~~~~~

AMES (V4.0) was developed by Dheepak Krishnamurthy, Sean Mooney, Auswin
George, Wanning Li and Leigh Tesfatsion.

References
~~~~~~~~~~

D. Krishnamurthy, W. Li and L. Tesfatsion, "An 8-Zone Test System Based
on ISO New England Data: Development and Application," in IEEE
Transactions on Power Systems, vol. 31, no. 1, pp. 234-246, Jan. 2016. - `Link <http://www2.econ.iastate.edu/tesfatsi/8ZoneISONETestSystem.RevisedAppendix.pdf>`__

Hongyan Li and Leigh Tesfatsion, "Development of Open Source Software for Power Market Research: The AMES Test Bed" Journal of Energy Markets, Vol. 2, No. 2, Summer 2009, 111-128. - `Link <http://www2.econ.iastate.edu/tesfatsi/OSS_AMES.2009.pdf>`__
