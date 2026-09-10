{
# As a general guide run these kind of script files with sed -E -i.bak -f <filename.sed> <filespec>
#
# 5.6 regular expression extensions
#
#The following sequences have special meaning inside regular expressions (used in addresses and the s command).
#These can be used in both basic and extended regular expressions (that is, with or without the -E/-r options).
#Remember that if you used the  -E options then subexpressions do not need escaped brackets!!!
#
# \w
#
#    Matches any  wordµ character. A  wordµ character is any letter or digit or the underscore character.
#
#    $ echo "abc %-= def." | sed 's/\w/X/g'
#    XXX %-= XXX.
#
# \W
#
#    Matches any  non-wordµ character.
#
#    $ echo "abc %-= def." | sed 's/\W/X/g'
#    abcXXXXXdefX
#
# \b
#
#    Matches a word boundary; that is it matches if the character to the left is a  wordµ character and the character to the right is a  non-wordµ character, or vice-versa.
#
#    $ echo "abc %-= def." | sed 's/\b/X/g'
#    XabcX %-= XdefX.
#
# \B
#
#    Matches everywhere but on a word boundary; that is it matches if the character to the left and the character to the right are either both  wordµ characters or both  non-wordµ characters.
#
#    $ echo "abc %-= def." | sed 's/\B/X/g'
#    aXbXc X%X-X=X dXeXf.X
#
# \s
#
#    Matches whitespace characters (spaces and tabs). Newlines embedded in the pattern/hold spaces will also match:
#
#    $ echo "abc %-= def." | sed 's/\s/X/g'
#    abcX%-=Xdef.
#
# \S
#
#    Matches non-whitespace characters.
#
#    $ echo "abc %-= def." | sed 's/\S/X/g'
#    XXX XXX XXXX
#
# \<
#
#    Matches the beginning of a word.
#
#    $ echo "abc %-= def." | sed 's/\</X/g'
#    Xabc %-= Xdef.
#
# \>
#
#    Matches the end of a word.
#
#    $ echo "abc %-= def." | sed 's/\>/X/g'
#    abcX %-= defX.
#
# \`
#
#    Matches only at the start of pattern space. This is different from ^ in multi-line mode.
#
#    Compare the following two examples:
#
#    $ printf "a\nb\nc\n" | sed 'N;N;s/^/X/gm'
#    Xa
#    Xb
#    Xc
#
#    $ printf "a\nb\nc\n" | sed 'N;N;s/\`/X/gm'
#    Xa
#    b
#    c
#
# \'
#
#    Matches only at the end of pattern space. This is different from $ in multi-line mode.
#

s/extends\s+"SWIFT"/extends swiftVersion|slice(0,7) == "Skylark" \? "SKYLARK" : "SWIFT"/
## above too adventurous for the moment.  Lets try and find an extend
##/extends\s+".+"/p
}