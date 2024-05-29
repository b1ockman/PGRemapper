grammar PGMap;

@header {
package org.bruh.PGRemapper.antlr4;
}

mappings: mapping* EOF;

mapping: class_mapping ':' (field_mapping | func_mapping | func_mapping_ln)*;//inclass_mapping*;
//inclass_mapping: field_mapping | func_mapping | func_mapping_ln;

class_mapping: pinfo_name '->' pinfo_name; // name '->' name;

field_mapping: type ID '->' ID;

func_mapping_ln: INT ':' INT ':' func_mapping;
func_mapping: func '->' func_name;

func: type func_name '(' func_args? ')';
func_name: ID | '<init>' | '<clinit>';
func_args: type (',' type)*;

type: name '[]'*;
name: ID ('.' ID)*;

// TODO: Fix somehow
// package-info class name workaround
pinfo_name: pinfo_fix ('.' pinfo_fix)*;
pinfo_fix: ID | 'package-info';

INT: [0-9]+;
ID: [_$a-zA-Z][_$a-zA-Z0-9]*;
WS: [ \t\n\r\f]+ -> skip;
COMMENT: '#' ~[\r\n]* -> skip;