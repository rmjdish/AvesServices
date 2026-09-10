import pandas as pd
import numpy as np
df = pd.DataFrame(np.array((['red',   '2', 0],
                            ['black', '3', -1],
                            ['grey',  '5', 1])),
                  index=['mouse', 'rabbit', 'fox'],
                  columns=['Name', 'Value', 'Missing'])
print(df)
print(f"\nAnd as df[['Name','Value']]\n {df[['Name','Value']]}")
mark = 'BLACK'
mask = [x.casefold() == mark.casefold() for x in df['Name']]
print(f"\n\nTesting for Name={mark} Mask: {mask}")
print(f"Result \n{df[mask]}")
# Missing Values bit...
series = np.array([1,1,1,1,2,2,2,3,3,3,3,3,2,2,2,2,1,4,1,1,1,5,5,1,1])
print(f"Numpy series: {series}")
uniqs, freqs = np.unique(series, return_counts=True)
mask = uniqs[freqs < 3]
print(f"Mask for deletion(freqs < 3) {mask}")
final = series[~np.in1d(series, mask)]
print(f"Final series: {final}")
# Version 1
metadf = df[df['Missing'] != 0]
print(f"Version one: metadf:\n{metadf}")
# Now missing values
mask = [int(x) != 0 for x in df['Missing']]
print(f"Version two: Missing values mask: {mask}")
# How to apply mask to df
miss = df[mask]
print(f"Missing values only:\n {miss}")
# Use to cut series
mask = [int(x) for x in miss['Value']]
delmask = [x in mask for x in series]
print(f"Mask for array deletion: {delmask}")
fseries = np.delete(series, delmask)
print(f"Final cut series: {fseries}")
