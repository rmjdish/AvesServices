


CREATE DEFINER=`root`@`%` PROCEDURE `remove_non_ascii`()
BEGIN
UPDATE robin.valuelabels SET Label = CONVERT(Label USING ASCII)
WHERE Label <> CONVERT(Label USING ASCII);
UPDATE robin.variablelabels SET Label = CONVERT(Label USING ASCII)
WHERE Label <> CONVERT(Label USING ASCII);
UPDATE rook.valuelabels SET Label = CONVERT(Label USING ASCII)
WHERE Label <> CONVERT(Label USING ASCII);
UPDATE rook.variablelabels SET Label = CONVERT(Label USING ASCII)
WHERE Label <> CONVERT(Label USING ASCII);
END
