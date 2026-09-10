# The nshd Python Package

## Included Programs

  + **buildbasket** - create a NSHD dataset as a set of CSV files from a Condor basket
  + **scramblebasket** - scramble an NSHD dataset that has been built by *buildbasket*

## SYNOPSIS

buildbasket *BASKETID* *USERNAME*
scramblebasket *BASKETID* *USERNAME*

# Description

The above related programs can build NSHD datasets from saved baskets
created by Condor.  The datasets are delivered as a set of CSV files
which include data and metadata about the variables in the specified
basket.

Alternatively, the user can pseudonomise an already built CSV dataset
that exists as a set of files.  Values for new identifiers are chosen
from the //scramble// database.

In both cases the user must specify the BASKETID and USERNAME
associated with the saved basket.

Resulting datasets are created or read from 

    /xnat/san/SST/baskets.

# Author

Written by Phil Curran 2025.

# Reporting Bugs

Contact SwiftInfo@ucl.ac.uk

# Copyright

Copyright Phil Curran 2025. This software is released under the
following License: GPLv3+: GNU GPL version 3 or later
<https://gnu.org/licenses/gpl.html>.

This is free software: you are free to change and redistribute it. There
is NO WARRANTY, to the extent permitted by law.
