-- 'Loads CSV file produced by Descriptives.py into robin.descriptives'
LOAD DATA LOCAL INFILE "/home/Public/workspace/CommonBirds/local/python/varplots/outputs/plots_in_html.csv"
REPLACE INTO TABLE `descriptives`
CHARACTER SET 'utf8'
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '|'
LINES TERMINATED  BY '$$'
IGNORE 1 LINES
(name,descriptives)
