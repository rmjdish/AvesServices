#!/bin/bash
#*
#*----------------------------------------------------------------------------------------
#*
#* All software provided below is unsupported and provided as-is, without warranty 
#* of any kind.
#*
#* To the extent possible under law, Red Hat, Inc. has dedicated all copyright
#* to this software to the public domain worldwide, pursuant to the CC0 Public
#* Domain Dedication. This software is distributed without any warranty.
#* See <http://creativecommons.org/publicdomain/zero/1.0/>.
#*
#*----------------------------------------------------------------------------------------
#*
#* Maintainer: bubrown@redhat.com
#* Version   : "01.06-0077-09052018"		/* Wed May  9 14:40:01 2018 bubrown */
#
# shalign
#
# Input arguments:
#   arg1 = disk name, sdX (e.g. sda, sdb)
#
# Outputs:
#   redirect all output to log file for capture
#
# perform a partition alignment calculation,  similar to alignment check in parted, and
# make a suggested alignment for partitions on the device.
#
# parted alignment check:
#     1. Always use the reported alignment offset as offset
#     2a.  If optimal io size is     present in the topology info use that as grain
#     2b.  If optimal io size is not present in the topology info and alignment
#          offset is 0 and minimum io size is a power of 2, use the
#          default optimal alignment (grain 1MiB).
#     2c.  If not 2a and 2b and minimum io size is defined (>0), use the minimum io size as grain
#     2d.  if not 2a,2b, 2c use the physical sector size as grain (iow the minimum alignment).
#
echo " "
echo "Disk name: $1"
echo "------------------------------------------------------------------------------------"
echo "See https://access.redhat.com/solutions/184143 for more info."
echo "------------------------------------------------------------------------------------"
if [ "$1" != "TEST" ]
then
    _optiosz=$( cat /sys/block/$1/queue/optimal_io_size  )
    _miniosz=$( cat /sys/block/$1/queue/minimum_io_size  ) 
    _phybksz=$( cat /sys/block/$1/queue/physical_block_size )
    _logbksz=$( cat /sys/block/$1/queue/logical_block_size  )
    _algnoff=0
    _algnflg="N"
    if [ -e /sys/block/$1/queue/alignment_offset ]
    then
        _algnoff=$( cat /sys/block/$1/queue/alignment_offset    )
	_algnflg="Y"
    fi
else
    # TEST case with manually provided data
    if [ "$6" == "" ]
    then
	echo "too few arguments:"
	echo "arg2 = optimal_io_size,     e.g. 16777216"
        echo "arg3 = minimum_io_size,     e.g.  4177920"
	echo "arg4 = physical_block_size, e.g.      512"
	echo "arg5 = logical_block_size,  e.g.      512"
	echo "arg6 = alignment_offset,    e.g.        0"
        exit
    fi 
    _optiosz=$2
    _miniosz=$3
    _phybksz=$4
    _logbksz=$5
    _algnoff=$6
    _algnflg="*"
fi
_ioratio=0
_padratio=0
_whole=0
_fract=0
_ratioflg=" "
if [ "$_miniosz" != "0" ]
then
    _ioratio=$(( ($_optiosz * 10000) / $_miniosz ))
    _whole=$(( $_ioratio / 10000 ))
    _fract=$(( $_ioratio - ($_whole * 10000) ))
    _ratioflg=" "
    _padratio=$_ioratio
    if [ "$_fract" != "0" ]
    then
	_ratioflg="!"
        _padratio=$(( ($_optiosz + $_miniosz) / $_miniosz ))
    fi
fi 
echo " "
echo "Device info:"
echo "optimal_io_size  minimum_io_size   opt:min ratio  physical_block_size  logical_block_size  alignment_offset"
echo "---------------  ---------------  --------------  -------------------  ------------------  -- -------------"
#                123456789.123456789.   123456789.1234
_padded=$( echo "                   $_optiosz" | grep -o '.\{15\}$' ) ; echo -n "$_padded  "
_padded=$( echo "                   $_miniosz" | grep -o '.\{15\}$' ) ; echo -n "$_padded  "
_padded=$( echo "                   $_whole"   | grep -o '.\{9\}$'  ) ; echo -n "$_padded."
_padded=$( echo "            0000000$_fract"   | grep -o '.\{4\}$'  ) ; echo -n "$_padded$_ratioflg "
_padded=$( echo "                   $_phybksz" | grep -o '.\{18\}$' ) ; echo -n "$_padded  " 
_padded=$( echo "                   $_logbksz" | grep -o '.\{19\}$' ) ; echo -n "$_padded  "
echo -n " $_algnflg "
_padded=$( echo "                   $_algnoff" | grep -o '.\{13\}$' ) ; echo    "$_padded  quantities in bytes"
echo "-----------------------------------------------------------------------------------------------------------"
_optiosc=$(( $_optiosz / _logbksz ))
_miniosc=$(( $_miniosz / _logbksz ))
_padded=$( echo "                   $_optiosc" | grep -o '.\{15\}$' ) ; echo -n "$_padded  "
_padded=$( echo "                   $_miniosc" | grep -o '.\{15\}$' ) ; echo -n "$_padded  "
           echo "                                                                           quantities in logical sectors"
echo "-----------------------------------------------------------------------------------------------------------"
echo " "
#
echo    " "
echo -n "Disk Type: "
if [ $_logbksz != $_phybksz ]
then
    if [ $_logbksz == 512 ]
    then
	if [ $_phybksz == 4096 ]
	then
	    echo " 512 bytes/sector logical, 4096 bytes physical (Advanced Format 512byte Emulation - 512e)"
	else
	    echo "huh?"
	fi
    else
	echo "huh?"
    fi
else
    if [ $_logbksz == 512 ]
    then
	echo "  512 bytes/sector logical&physical"
    else
	echo " 4096 bytes/sector logical&physical (Advanced Format 4k Native - 4kN)"
    fi
fi
#
_ioratio=1
if [ $_optiosz != 0 ]
then
    if [ $_miniosz != 0 ]
    then
	_ioratio=$(( $_optiosz / $_miniosz ))
    fi
fi
_even=" "
_iototal=$(( $_ioratio * $_miniosz ))
if [ $_iototal != $_optiosz ]
then
    if [ $_optiosz != 0 ]
    then
       _even="+"
    fi
fi
_bkratio=$(( $_phybksz / $_logbksz ))
#
echo " "
echo "phy:log block ratio: $_bkratio"
echo "opt:min       ratio: $_ioratio$_even"
echo " "
_rule_2a=0
_rule_2b=0
_rule_2c=0
if [ $_algnoff == 0 ] ; then echo "[ ] 1. Always use the reported alignment offset as offset" ;
                        else echo "[x] 1. Always use the reported alignment offset as offset" ; fi
if [ $_optiosz == 0 ] ; then echo "[ ] 2a. If optimal io size is     present in the topology info use that as grain" ;
			else echo "[x] 2a. If optimal io size is     present in the topology info use that as grain ($_optiosz)" ; _rule_2a=1 ; fi
if [ $_optiosz == 0 ] && [ $_algnoff == 0 ] && [ $_miniosz != 0 ] ;
		        then echo "[x] 2b. If optimal io size is not present in the topology info and alignment" ; _rule_2b=1 ; 
			else echo "[ ] 2b. If optimal io size is not present in the topology info and alignment" ; fi
			     echo "        offset is 0 and minimum io size is a power of 2, use the default"
			     echo "        optimal alignment (grain 1MiB)" ;
if [ $_rule_2a == 0 ] && [ $_rule_2b == 0 ] ;
			then echo "[x] 2c. If not 2a and 2b and minimum io size is defined (>0), use the minimum io size as grain ($_miniosz)" ; _rule_2c=1 ; 
			else echo "[ ] 2c. If not 2a and 2b and minimum io size is defined (>0), use the minimum io size as grain" ; fi
if [ $_rule_2a == 0 ] && [ $_rule_2b == 0 ] && [ _rule_2c == 0 ] ;
		        then echo "[x] 2d. If not 2a,2b, 2c use the physical sector size as grain (iow the minimum alignment). ($_phybksz)" ;
		        else echo "[ ] 2d. If not 2a,2b, 2c use the physical sector size as grain (iow the minimum alignment). " ; fi
#
#
if [ $_iototal != $_optiosz ]
then
    if [ $_optiosz != 0 ]
    then
	echo ">>WARNING: optimal_io_size not an even ratio of minimum_io_size?! Expectation is opt/min = whole number."
	echo ">>         Fdisk may complain about alignment because optimal io size is not an even ratio of minimum io size."
	echo ">>         The expectation is the alignment should be divisible by both.  In this case using 1MB offset,"
        echo ">>         $(( (1024*1024) / $_logbksz)) sectors, may be alternative/reasonable value to use versus the"
        echo ">>         calculation of 'partition alignment = (optimal_io_size + alignment_offset) / physical_block_size'"
        echo ">>         For example, see https://access.redhat.com/solutions/2888411 for a case where this can happen."
    fi
fi
#
echo " "
_rule="??"
_align="999999"
if [ $_optiosz != 0 ]
then
    _rule="2a."
    _align=$(( $_optiosz + $_algnoff  ))	# 2a. optio  present
else
    if [ $_algnoff == 0 ]
    then
	_rule="2b."
       _align=1048576				# 2b. optio !present && algnoff=0 (&&minio is power of 2)
    else
	if [ $_miniosz != 0 ]
	then
	    _rule="2c."
	    _align=$(( $_miniosz + $_algnoff )) # 2c. not above and min io size defined
	else
	    _rule="2d."
	    _align=$(( $_phybksz + $_algnoff )) # 2d. not above, use phy block size
        fi
    fi
fi
_incrb=$(( $_optiosz              ))
if [ $_incrb == 0 ]
then
    _incrb=$(( $_phybksz ))
    if [ $_incrb -lt $_align ]
    then
	_incrb=$(( $_align - $_algnoff ))
    fi
fi
_aligb=$_align
_incrs=$(( $_incrb   / $_logbksz ))
_align=$(( $_align   / $_logbksz ))
echo "Alignment: $_align sectors (suggested minimum:rule $_rule), "
echo "           - minimum quanta/increments of $_aligb bytes ($_align sectors) should be "
echo "             specficied for parted START partition value, that is each partition's  "
echo "             starting sector should be evenly divisible by $_align."
echo "------------------------------------------------------------------------------------"
echo " "
echo "example: mkpart primary   "$_align"s 100%   << $_align/$_incrs =  1 (START/quanta)"
_align=$(( $_align * 16 ))
echo "example: mkpart primary " $_align"s 100%  << $_align/$_incrs = 16"
echo " "
echo " "
exit
#
# using _align=_align/_phybksz seems wrong for 512e devices like the following:
#
# ./shalign sda
#   
#  Disk name: sda
#  --------------------------------------------------
#   
#  Disk Type:  512 bytes/sector logical, 4096 bytes physical (Advanced Format 512byte Emulation - 512e)
#   
#  physical_sector_size 4096   logical_sector_size  512    ratio: 8
#  optimal_io_size      0   minimum_io_size      4096    ratio: 1    alignment_offset 0
#   
#  Alignment: 256 sectors (suggested minimum:rule 2b.), minimum quanta/increments of 1048576 bytes (2048 sectors)
#  --------------------------------------------------
#  
#  so alignment end up at 256 sectors x 512b per LBA = 128Kib, not the 1Mib expected.
#  so changed the divisor to logbksz so alignment is on the 1 MiB grain. Sectors are accessible
#  by logical block size not physical block size.
#
# NOTES:
# cat /sys/block/sdb/queue/optimal_io_size = 1835008
# cat /sys/block/sdb/queue/minimum_io_size =  262144 (ratio of 1:7)
# suggests that the logical volume is 7 stripe widths wide, each stripe being 256k
# but that is only one interpretation of the data.

