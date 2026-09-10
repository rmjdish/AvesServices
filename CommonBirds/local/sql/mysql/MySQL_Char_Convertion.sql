-- Phil Curran March 2025
-- Uses MySQL built in to transcode non-ascii to ascii in Label fields
-- The way to find non-ascii chars is like this:
-- SELECT whatever
--   FROM tableName 
-- WHERE columnToCheck <> CONVERT(columnToCheck USING ASCII)

DELIMITER $$
DROP PROCEDURE IF EXISTS remove_non_ascii $$
CREATE PROCEDURE remove_non_ascii()
BEGIN
	DECLARE kiwi  VARCHAR(64) DEFAULT "mrc-kiwi01";
	DECLARE finch VARCHAR(64) DEFAULT "mrc-finch01";
	DECLARE whoiam VARCHAR(64) DEFAULT @@hostname;
	-- The Label fields have been where the funny chars have historically been
	-- but the technique can be used on any VARCHAR or TEXT column
	-- This version converts strings in two tables, variablelabels and valuelabels
	-- variablelabels:  Name, Label
	-- valuelabels Name, Value, Label, MissingValueCode
	-- robin is on both Finch and Kiwi
	-- Name in valuelabels
	UPDATE robin.valuelabels SET Name = CONVERT(Name USING ASCII)
	WHERE Name <> CONVERT(Name USING ASCII);
	UPDATE robin.valuelabels SET Name = TRIM(Name);
	-- Value in valuelabels
	UPDATE robin.valuelabels SET Value = CONVERT(Value USING ASCII)
	WHERE Value <> CONVERT(Value USING ASCII);
	UPDATE robin.valuelabels SET Value = TRIM(Value);
	-- Label in valuelabels
	UPDATE robin.valuelabels SET Label = CONVERT(Label USING ASCII)
	WHERE Label <> CONVERT(Label USING ASCII);
	UPDATE robin.valuelabels SET Label = TRIM(Label);
	-- MissingValueCode in valuelabels
	UPDATE robin.valuelabels SET MissingValueCode = CONVERT(MissingValueCode USING ASCII)
	WHERE MissingValueCode <> CONVERT(MissingValueCode USING ASCII);
	UPDATE robin.valuelabels SET MissingValueCode = TRIM(MissingValueCode);
	-- Name in variablelabels
	UPDATE robin.variablelabels SET Name = CONVERT(Name USING ASCII)
	WHERE Name <> CONVERT(Name USING ASCII);
	UPDATE robin.variablelabels SET Name = TRIM(Name);
	-- Label in variablelabels
	UPDATE robin.variablelabels SET Label = CONVERT(Label USING ASCII)
	WHERE Label <> CONVERT(Label USING ASCII);
	UPDATE robin.variablelabels SET Label = TRIM(Label);
        -- Name in shoppingbaskets
	UPDATE robin.shoppingbaskets SET Name = CONVERT(Name USING ASCII)
	WHERE Name <> CONVERT(Name USING ASCII);
	UPDATE robin.shoppingbaskets SET Name = TRIM(Name);
	-- rook is only on Kiwi
	IF whoiam = kiwi THEN
		-- Name in valuelabels
		UPDATE rook.valuelabels SET Name = CONVERT(Name USING ASCII)
		WHERE Name <> CONVERT(Name USING ASCII);
		UPDATE rook.valuelabels SET Name = TRIM(Name);
		-- Value in valuelabels
		UPDATE rook.valuelabels SET Value = CONVERT(Value USING ASCII)
		WHERE Value <> CONVERT(Value USING ASCII);
		UPDATE rook.valuelabels SET Value = TRIM(Value);
		-- Label in valuelabels
		UPDATE rook.valuelabels SET Label = CONVERT(Label USING ASCII)
		WHERE Label <> CONVERT(Label USING ASCII);
		UPDATE rook.valuelabels SET Label = TRIM(Label);
		-- MissingValueCode in valuelabels
		UPDATE robin.valuelabels SET MissingValueCode = CONVERT(MissingValueCode USING ASCII)
		WHERE MissingValueCode <> CONVERT(MissingValueCode USING ASCII);
		UPDATE robin.valuelabels SET MissingValueCode = TRIM(MissingValueCode);
		-- Name in variablelabels		
		UPDATE rook.variablelabels SET Name = CONVERT(Name USING ASCII)
		WHERE Name <> CONVERT(Name USING ASCII);
		UPDATE rook.variablelabels SET Name = TRIM(Name);
		-- Label in variablelabels
		UPDATE rook.variablelabels SET Label = CONVERT(Label USING ASCII)
		WHERE Label <> CONVERT(Label USING ASCII);
		UPDATE rook.variablelabels SET Label = TRIM(Label);
	        -- Name in shoppingbaskets
	        UPDATE rook.shoppingbaskets SET Name = CONVERT(Name USING ASCII)
		WHERE Name <> CONVERT(Name USING ASCII);
	        UPDATE rook.shoppingbaskets SET Name = TRIM(Name);
	END IF;
END $$
DELIMITER ;
