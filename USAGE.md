# Usage

### Running AMES test bed:

Windows:
    Double click the AMESMarket.jar file.

Linux:
    java -jar AMESMarket.jar

###  Configuration options:

```
    System Properties, set with -D<PROPERTY_NAME>=<value>
        //Chose how the sced is computed.
        //Defaults to pyomo
        SCED = pyomo | dcopfj

        //Whether or not to delete intermidate/external files.
        //Defaults to true. false is useful for debugging.
        DEL_INTER_FILES=true|false
```

Notes on sections, of the test case file format.

Specify whether or not the LSE demand bids will come from the TestCase or
the LoadCase. The ames-2.05 method is that the demand bids come from the TestCase.
Using load data in the LoadCase was added to support the ARPAe stochastic SCUC work.


### Key/Value pairs

The first section of the TestCase config consists of key/value pairs, where
whitespace seperates the key from the value.

```
Capacity_Margin <MARGIN_VALUE>
```

The capcity margin can be optionally set. It should be as a percentage, not as a decimal. e.g. 15, not .15 for a 15% capacity margin. The margin defaults to 10% if not set.


```
LSEDemandSource [TestCase | LoadCase]
```

> LSEDemandSource can be test to come from the TestCase or the LoadCase

```
SCUC_Type [Deterministic | Stochastic]
```

### DATA SECTIONS

**ZoneNames**

Zone names must be specified before any zones/bus designatations are used in remainder of the data file.

```
#ZoneNamesStart
    Name1
    Name2
    Name3
    ...
    NameN
#ZoneNamesEnd
```

**AlertGen**

Generators which are installed at zones to ensure the ability to meet load are AlertGen's. The intent is that these generators are priced such that they only are committed/dispatched as a last resort. The AlertGen section lists the name of each alert gen, one per line. The names in this section should match the names of the generation units in the GenData section. Like all the other sections of file, the section starts and ended with a line marker like below.

```
#AlertGenCoStart
    GenCo
#AlertGenCoEnd
```

**ScucInputData**

Lists the extra parameters needed for the external SCUC/SCED. Data format one GenCo per row, with
each column listing a specific parameter. Colum order is fixed.
The UnitOnT0, Schedule and Schedule2 items model booleans and must be either 0 or 1.

The system will not run if this data is not present. e.g. :

```
#ScucInputDataStart
//  Name    PowerT0 UnitOnT0    MinUpTime   MinDownTime NominalRampUp   NominalRampDown StartupRampLim ShutdownRampLim  Schedule    Schedule2
    GenCo1  1282    1           0           0           9999            9999            9999           9999             1           1
    GenCo2  17272   1           0           0           9999            9999            9999           9999             1           1
    GenCo3  483     1           0           0           9999            9999            9999           9999             1           1
    GenCo4  2538    1           0           0           9999            9999            9999           9999             1           1
    GenCo5  3568    1           0           0           9999            9999            9999           9999             1           1
#ScucInputDataEnd
```

