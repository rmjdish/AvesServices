from setuptools import setup, find_packages

VERSION = '1.0' 
DESCRIPTION = 'Basic package for accessing PG UK GE and EU referendum data'
LONG_DESCRIPTION = 'Utils to hide details of SQLAlchemy and Pandas for use with UK GE data'

# Setting up
setup(
       # the name must match the folder name 
        name="ukge", 
        version=VERSION,
        author="Phil Curran",
        author_email="philipcurran@duck.com",
        url='http://mule.local/wiki',
        description=DESCRIPTION,
        long_description=LONG_DESCRIPTION,
        packages=find_packages(),
        install_requires=['pandas', 'sqlalchemy', 'ruamel.yaml', 'psycopg2-binary'], # add any additional packages that 
        # needs to be installed along with your package. Eg: 'pandas'
        
        keywords=['python', 'UK GE'],
        classifiers= [
            "Development Status :: 4 - Beta",
            "Intended Audience :: Politicos",
            "Programming Language :: Python :: 3",
            "Operating System :: MacOS :: MacOS X",
            "Operating System :: Microsoft :: Windows",
	    "Operating System :: Linux :: Debian",
        ]
)
