from setuptools import setup, find_packages

VERSION = '1.0' 
DESCRIPTION = 'Package for supporting long/wide transformations of CSV files'
LONG_DESCRIPTION = 'Two classes provide methods (go_long and go_wide) for supporting the transformation of CSV files from long to wide or vice versa'

# Setting up
setup(
       # the name must match the folder name 
        name="transform", 
        version=VERSION,
        author="Phil Curran",
        author_email="philipcurran@duck.com",
        url='http://mule.local/wiki',
        description=DESCRIPTION,
        long_description=LONG_DESCRIPTION,
        packages=find_packages(),
        install_requires=['csv', 'json'], # add any additional packages that 
        # needs to be installed along with your package. Eg: 'pandas'
        
        keywords=['python', 'transform'],
        classifiers= [
            "Development Status :: 4 - Beta",
            "Intended Audience :: Data Managers",
            "Programming Language :: Python :: 3",
            "Operating System :: MacOS :: MacOS X",
            "Operating System :: Microsoft :: Windows",
	    "Operating System :: Linux :: Debian",
        ]
)
