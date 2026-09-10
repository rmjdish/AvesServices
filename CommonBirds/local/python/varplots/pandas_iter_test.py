import pandas as pd

df = pd.DataFrame({'num_legs': [4, 2], 'num_wings': [0, 2]},
                  index=['dog', 'hawk'])
print(df)
# Iterate over it
for row in df.itertuples(name='Animal'):
    print(f"Legs: {row.num_legs}, Wings: {row.num_wings}")
    print(f"Raw Row: {row}")
    



