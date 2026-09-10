CREATE DEFINER=`root`@`%` PROCEDURE `getMetaForVar`(thisvar VARCHAR(64))
    READS SQL DATA
    COMMENT 'Returns metadata for a specified variable modulo low cell counts'
BEGIN
    DECLARE freqexists INT DEFAULT 0;
    -- Check table exists
    SELECT count(*) INTO freqexists FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = 'robin' AND TABLE_NAME = 'frequencies';
    IF freqexists = 0 THEN
        -- No frequencies table so just do the basic thing
        SELECT * FROM metadata WHERE Name = thisvar;
    ELSE
        -- Check values against entries in frequencies table
        IF thisvar IN (SELECT distinct fieldname from frequencies) THEN
            SELECT Tab,Name,VarLabel,Public,Value,ValueLabel,Missing
            FROM metadata INNER JOIN frequencies 
            ON (NAME = fieldname AND CONVERT(VALUE, DECIMAL(10,2)) = fieldvalue)
            WHERE Name=thisvar and fieldcount >=10 ;
        ELSE
            -- thisvar is not in frequencies table
            SELECT * FROM metadata WHERE Name = thisvar;
        END IF;
    END IF;
END
