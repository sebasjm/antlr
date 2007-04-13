import antlr3
from t014parserLexer import t014parserLexer as Lexer
from t014parserParser import t014parserParser as Parser

cStream = antlr3.StringStream('var foobar; gnarz(); var blupp; flupp ( ) ;')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
parser.document()

assert len(parser.reportedErrors) == 0, parser.reportedErrors
assert parser.events == [
    ('decl', 'foobar'),
    ('call', 'gnarz'),
    ('decl', 'blupp'),
    ('call', 'flupp')
    ], parser.events


# malformed input
cStream = antlr3.StringStream('var; foo();')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)

parser.document()

# FIXME: currently strings with formatted errors are collected
# can't check error locations yet
assert len(parser.reportedErrors) == 1, parser.reportedErrors
# FIXME: shouldn't this be ('call', 'foo')???
assert parser.events == [], parser.events


# malformed input
cStream = antlr3.StringStream('var foobar(); gnarz();')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)

parser.document()

# FIXME: currently strings with formatted errors are collected
# can't check error locations yet
assert len(parser.reportedErrors) == 1, parser.reportedErrors
assert parser.events == [
    ('call', 'gnarz'),
    ], parser.events


# malformed input
cStream = antlr3.StringStream('gnarz(; flupp();')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)

parser.document()

# FIXME: currently strings with formatted errors are collected
# can't check error locations yet
assert len(parser.reportedErrors) == 1, parser.reportedErrors
assert parser.events == [
    ('call', 'gnarz'),
    ('call', 'flupp'),
    ], parser.events
