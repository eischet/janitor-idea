/* Janitor: a simple procedural language, inspired by JavaScript. */

grammar Janitor;


@parser::members {
    private boolean isLineTerminatorAhead() {
        Token next = _input.LT(1);
        int t = next.getType();
        if (t == JanitorLexer.RBRACE) return false;
        if (t == Token.EOF) return true;
        CommonTokenStream stream = (CommonTokenStream) getInputStream();
        java.util.List<Token> hidden = stream.getHiddenTokensToLeft(next.getTokenIndex());
        if (hidden == null) return false;
        for (Token h : hidden) {
            if (h.getType() == JanitorLexer.NEWLINE) return true;
        }
        return false;
    }
}

script: topLevelStatement* EOF; // A script consists of a number of top-level statements

stmtTerminator
    : SEMICOLON
    | { _input.LT(1).getType() != JanitorLexer.SEMICOLON && isLineTerminatorAhead() }?
    ;

topLevelStatement
    : importStatement                                         # topLevelImportStatement
    | blockStatement                                          # topLevelBlockStatement
    ;

importStatement
    : IMPORT importClause (COMMA importClause)* stmtTerminator
    ;

importClause
    : qualifiedName ('as' importAlias )?    # importPlain
    | STRING_LITERAL_SINGLE 'as' importAlias  # importStringSingle
    | STRING_LITERAL_DOUBLE 'as' importAlias  # importStringDouble
    ;

blockStatement
    : blockLabel=block                                                                      # nestedBlock
    | ifStatementDef                                                                        # ifStatement
    | FOR LPAREN validIdentifier IN expression RPAREN block                                 # forStatement
    | FOR LPAREN validIdentifier FROM expression TO expression RPAREN block                 # forRangeStatement
    | WHILE LPAREN expression RPAREN block                                                  # whileStatement
    | DO block WHILE LPAREN expression RPAREN stmtTerminator                                # doWhileStatement
    | TRY block (catchClause? finallyBlock? | finallyBlock)                                 # tryCatchStatement
    | RETURN expression? stmtTerminator                                                     # returnStatement
    | THROW expression stmtTerminator                                                       # throwStatement
    | BREAK stmtTerminator                                                                  # breakStatement
    | CONTINUE stmtTerminator                                                               # continueStatement
    | <assoc=right> expression bop=(ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN | MUL_ASSIGN | DIV_ASSIGN | MOD_ASSIGN )  expression        # assignmentStatement
    | statementExpression=expression stmtTerminator                                         # expressionStatement
    | functionDeclaration                                                                   # functionDeclarationStatement
    | SEMICOLON                                                                             # emptyStatement
    ;

ifStatementDef: IF LPAREN expression RPAREN block (ELSE block | ELSE ifStatementDef)?;

block: LBRACE blockStatement* RBRACE;

expression
    : expression DOT validIdentifier LPAREN argumentList? RPAREN                                                                  # memberCall
    | expression QDOT validIdentifier LPAREN argumentList? RPAREN                                                                 # optionalMemberCall
    | expression DOT validIdentifier                                                                                                # memberAccess
    | expression QDOT validIdentifier                                                                                               # optionalMemberAccess
    | functionCall                                                                                                                  # callExpression
    | expression bop=(DOT|QDOT) ( validIdentifier | functionCall | explicitGenericInvocation )                                      # callExpression
    | LPAREN expression RPAREN                                                                                                      # parensExpression
    | (DECIMAL_LITERAL | HEX_LITERAL | BINARY_LITERAL)                                                                              # integerLiteral
    | FLOAT_LITERAL                                                                                                                 # floatLiteral
    | (YEARS_LITERAL | MONTHS_LITERAL | DAYS_LITERAL | WEEKS_LITERAL | HOURS_LITERAL | MINUTES_LITERAL | SECONDS_LITERAL )          # durationLiteral
    | (DATE_TIME_LITERAL | NOW_LITERAL)                                                                                             # dateTimeLiteral
    | (DATE_LITERAL | TODAY_LITERAL)                                                                                                # dateLiteral
    | STRING_LITERAL_TRIPLE_SINGLE                                                                                                  # stringLiteralTripleSingle
    | STRING_LITERAL_TRIPLE_DOUBLE                                                                                                  # stringLiteralTripleDouble
    | STRING_LITERAL_SINGLE                                                                                                         # stringLiteralSingle
    | STRING_LITERAL_DOUBLE                                                                                                         # stringLiteralDouble
    | REGEX_LITERAL                                                                                                                 # regexLiteral
    | (TRUE|FALSE)                                                                                                                  # boolLiteral
    | NULL                                                                                                                          # nullLiteral
    | validIdentifier                                                                                                               # identifier
    | explicitGenericInvocationSuffix                                                                                               # genericInvocationExpression
    | expression LBRACK expression? COLON expression? COLON expression RBRACK                                                       # indexExpressionSteppedRange
    | expression LBRACK expression COLON RBRACK                                                                                     # indexExpressionRangeHead
    | expression LBRACK COLON expression RBRACK                                                                                     # indexExpressionRangeTail
    | expression LBRACK expression COLON expression RBRACK                                                                          # indexExpressionRange
    | expression LBRACK COLON RBRACK                                                                                                # indexExpressionFullRange
    | expression LBRACK expression RBRACK                                                                                           # indexExpression
    | expression postfix=( INC | DEC )                                                                                              # postfixExpression
    | prefix=( ADD | SUB | INC | DEC ) expression                                                                                   # prefixExpression
    | prefix=( NOT | ALT_NOT ) expression                                                                                           # notExpression
    | expression bop=(MUL | DIV | MOD) expression                                                                                   # binaryExpression
    | expression bop=(ADD | SUB) expression                                                                                         # binaryExpression
    | expression bop=(LE | GE | GT | LT) expression                                                                                 # binaryExpression
    | expression bop=(EQUAL | NOTEQUAL | ALT_NOTEQUAL | MATCH | MATCH_NOT ) expression                                              # binaryExpression
    | expression bop=(AND | CAND) expression                                                                                        # binaryExpression
    | expression bop=(OR | COR) expression                                                                                          # binaryExpression
    | <assoc=right> expression bop=QUESTION expression COLON expression                                                             # ternaryExpression
    | lambdaParameters ARROW lambdaBody                                                                                             # lambdaExpression
    | IF expression THEN expression (ELSE expression)?                                                                              # ifThenElseExpression
    | LBRACE (propertyAssignment (',' propertyAssignment)* ','?)? RBRACE                                                            # mapExpression
    | LBRACK (expression (',' expression)* ','?)? RBRACK                                                                            # listExpression
    ;


validIdentifier: IDENTIFIER | FROM | TO | IN;

propertyAssignment : (STRING_LITERAL_TRIPLE_DOUBLE | STRING_LITERAL_TRIPLE_SINGLE | STRING_LITERAL_DOUBLE | STRING_LITERAL_SINGLE | validIdentifier ) COLON expression;

functionDeclaration: FUNCTION validIdentifier formalParameters block;

formalParameters: LPAREN formalParameterList? RPAREN;

/*

formalParameterList: formalParameter (COMMA formalParameter)*;
formalParameter: validIdentifier;
*/

// foo

formalParameterList
    // 1) nonDefault (optional) + default (mind. einer) [+ *args] [+ **kwargs]
    : (nonDefaultParamList COMMA)? defaultParamList (COMMA varArgList)? (COMMA kwArgList)?   # formalParameterList4

    // 2) nur nonDefault [+ *args] [+ **kwargs]
    | nonDefaultParamList (COMMA varArgList)? (COMMA kwArgList)?                             # formalParameterList3

    // 3) nur *args [+ **kwargs]
    | varArgList (COMMA kwArgList)?                                                          # formalParameterList2

    // 4) nur **kwargs
    | kwArgList                                                                              # formalParameterList1
    ;
/*
formalParameterList
    : (nonDefaultParamList COMMA)? defaultParamList (COMMA varArgList)? (COMMA kwArgList)?   # formalParameterList5
    | (nonDefaultParamList COMMA)? defaultParamList? (COMMA varArgList)? (COMMA kwArgList)?  # formalParameterList4
    | (nonDefaultParamList COMMA)? varArgList (COMMA kwArgList)?                             # formalParameterList3
    | (nonDefaultParamList COMMA)? kwArgList                                                 # formalParameterList2
    | nonDefaultParamList                                                                    # formalParameterList1
    ;
*/

nonDefaultParamList
    : formalParameter (COMMA formalParameter)*
    ;

defaultParamList
    : formalParameterWithDefault (COMMA formalParameterWithDefault)*
    ;

varArgList
    : MUL validIdentifier
    ;

kwArgList
    : DOUBLE_STAR validIdentifier
    ;

formalParameter
    : validIdentifier
    ;

formalParameterWithDefault
    : validIdentifier '=' expression
    ;

// foo

importAlias: validIdentifier;

qualifiedName: validIdentifier (DOT validIdentifier)*;

catchClause: CATCH LPAREN validIdentifier RPAREN block;

finallyBlock: FINALLY block;


argumentList
    : positionalArgs (COMMA keywordArgs)?
    | keywordArgs
    ;

positionalArgs
    : argument (COMMA argument)*
    ;

keywordArgs
    : keywordArg (COMMA keywordArg)*
    ;

argument
    : expression
    ;

keywordArg
    : validIdentifier '=' expression
    ;

/* outdated variant of argumentList
expressionList: expression (COMMA expression)*;
*/

functionCall: validIdentifier LPAREN argumentList? RPAREN;

lambdaParameters: validIdentifier | LPAREN formalParameterList? RPAREN | LPAREN validIdentifier (COMMA validIdentifier)* RPAREN;

lambdaBody: expression | block;

// I think this can be simplified, but I'm not sure how

explicitGenericInvocation: explicitGenericInvocationSuffix;

explicitGenericInvocationSuffix: validIdentifier arguments;


arguments: LPAREN argumentList? RPAREN;

REGEX_LITERAL: 're/' ('\\/' | ~[\r\n] | .  )*? '/';


FUNCTION:           'function';
BREAK:              'break';
CATCH:              'catch';
CONTINUE:           'continue';
DO:                 'do';
ELSE:               'else';
FINALLY:            'finally';
FOR:                'for';
IF:                 'if';
THEN:               'then';
IMPORT:             'import';
RETURN:             'return';
THROW:              'throw';
TRY:                'try';
WHILE:              'while';
TRUE:               'true';
FALSE:              'false';
NULL:               'null';

// the following keywords are valid identifiers, too, by including them in the 'validIdentifier' rule
FROM:               'from';
TO:                 'to';
IN:                 'in';

DECIMAL_LITERAL:    ('0' | [1-9] (Digits? | '_'+ Digits)) [lL]?;
HEX_LITERAL:        '0' [xX] [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])? [lL]?;
OCT_LITERAL:        '0' '_'* [0-7] ([0-7_]* [0-7])? [lL]?;
BINARY_LITERAL:     '0' [bB] [01] ([01_]* [01])? [lL]?;
FLOAT_LITERAL:      (Digits '.' Digits? | '.' Digits) ExponentPart? [fFdD]? | Digits (ExponentPart [fFdD]? | [fFdD]);

DATE_LITERAL:       '@' '-'? [0-9] [0-9] [0-9] [0-9] '-' [0-9]? [0-9] '-' [0-9]? [0-9];
DATE_TIME_LITERAL:  '@' '-'? [0-9] [0-9] [0-9] [0-9] '-' [0-9]? [0-9] '-' [0-9]? [0-9] '-' [0-9]? [0-9] ':' [0-9] [0-9] (':' [0-9][0-9])?;

// maybe leading zeros should be suppressed by tweaking the expressions a bit
YEARS_LITERAL:      '@' [0-9]+ 'y';
MONTHS_LITERAL:     '@' [0-9]+ 'mo';
WEEKS_LITERAL:      '@' [0-9]+ 'w';
DAYS_LITERAL:       '@' [0-9]+ 'd';
HOURS_LITERAL:      '@' [0-9]+ 'h';
MINUTES_LITERAL:    '@' [0-9]+ 'mi';
SECONDS_LITERAL:    '@' [0-9]+ 's';
TODAY_LITERAL:      '@today';
NOW_LITERAL:        '@now';

STRING_LITERAL_SINGLE: '\'' (~['\\\r\n] | EscapeSequence)* '\'';
STRING_LITERAL_DOUBLE: '"'  (~["\\\r\n] | EscapeSequence)* '"';
STRING_LITERAL_TRIPLE_SINGLE: '\'\'\'' (~[\\] | EscapeSequence)*? '\'\'\'';
STRING_LITERAL_TRIPLE_DOUBLE: '"""' (~[\\] | EscapeSequence)*? '"""';


LPAREN: '('; RPAREN: ')';
LBRACE: '{'; RBRACE: '}';
LBRACK: '['; RBRACK: ']';

SEMICOLON:          ';';
COMMA:              ',';
DOUBLE_DOT:         '..';
DOT:                '.';
QDOT:               '?.';
ASSIGN:             '=';
PLUS_ASSIGN:        '+=';
MINUS_ASSIGN:       '-=';
MUL_ASSIGN:         '*=';
DIV_ASSIGN:         '/=';
MOD_ASSIGN:         '%=';
GT:                 '>';
LT:                 '<';
MATCH:              '~';
MATCH_NOT:          '!~';
QUESTION:           '?';
COLON:              ':';
EQUAL:              '==';
LE:                 '<=';
GE:                 '>=';
NOTEQUAL:           '!=';
ALT_NOTEQUAL:       '<>';
NOT:                'not';
ALT_NOT:            '!';
AND:                'and';
CAND:               '&&';
OR:                 'or';
COR:                '||';
INC:                '++';
DEC:                '--';
ADD:                '+';
SUB:                '-';
MUL:                '*';
DOUBLE_STAR:        '**';
DIV:                '/';
MOD:                '%';
ARROW:              '->';

WS:                 [ \t\u000C]+ -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);
NEWLINE:            [\r\n]+ -> channel(HIDDEN);

// https://groups.google.com/g/antlr-discussion/c/SzQJpVeSyHo about skipping white space

IDENTIFIER:         Letter LetterOrDigit*;

fragment ExponentPart: [eE] [+-]? Digits;
fragment EscapeSequence : '\\' [btnfr"'\\] | '\\' ([0-3]? [0-7])? [0-7] | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit;
fragment HexDigits: HexDigit ((HexDigit | '_')* HexDigit)?;
fragment HexDigit: [0-9a-fA-F];
fragment Digits: [0-9] ([0-9_]* [0-9])?;
fragment LetterOrDigit: Letter | [0-9];
fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;
