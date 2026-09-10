from pathlib import PurePath, Path
from ruamel.yaml import YAML
# Read system properties from YAML properties file
properties = "metaconfig.yaml"
print("__file__ is --> ",__file__)
print("Current working directory is --> ",Path.cwd())
print("Parent Directory is --> ",PurePath(Path.cwd()))
yaml = YAML(typ="safe", pure=True)
print(f"Config file: {properties}")
try:
    with open(properties, "r", encoding="utf-8") as f:
        p = yaml.load(f)
except:
    sys.exit(f"readProps: Error reading properties file: {properties}")
# I'm just interested in the relevant section of p
sec = p["MakeDescriptives"]
print(sec)
