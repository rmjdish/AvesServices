"""
Class PlotColumn:
    Plots an individual column as a Pandas/Matplotlib plot and returns as
    a Matplotlib figure
"""
class PlotColumn():
    def __init__(self,dframe):
        self.dataframe = dframe
    
    #-------------------------------------------------------------------------#
    
    def doXTab(self,field):
        # Does this dataframe have a sex column?
        if 'sex' in self.dataframe.columns:
            xtab = pd.crosstab(index=self.dataframe[field],
                               columns=self.dataframe["sex"],
                               dropna=False,
                               margins=True)
        else:
            xtab = pd.crosstab(index=self.dataframe[field],
                               columns="count",
                               dropna=False,
                               margins=True)
        thisfig = xtab.to_html()    
        return thisfig
    
    #-------------------------------------------------------------------------#
    def doHist(self,field):
        fig, axs  = plt.subplots(figsize=(7,5), dpi=100)
        # Plot Histogram on x
        x = self.dataframe[field]
        plt.hist(x)
        plt.gca().set(title='Frequency Histogram of ' + field, ylabel='Frequency');
        #plt.show()
        return thisfig

