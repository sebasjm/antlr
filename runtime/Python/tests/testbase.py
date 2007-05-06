import unittest
import imp
import os
import errno
import sys
from distutils.errors import *

def unlink(path):
    try:
        os.unlink(path)
    except OSError, exc:
        if exc.errno != errno.ENOENT:
            raise


class BrokenTest(unittest.TestCase.failureException):
    def __repr__(self):
        name, reason = self.args
        return '%s: %s: %s works now' % (
            (self.__class__.__name__, name, reason))


def broken(reason, *exceptions):
    '''Indicates a failing (or erroneous) test case fails that should succeed.
    If the test fails with an exception, list the exception type in args'''
    def wrapper(test_method):
        def replacement(*args, **kwargs):
            try:
                test_method(*args, **kwargs)
            except exceptions or unittest.TestCase.failureException:
                pass
            else:
                raise BrokenTest(test_method.__name__, reason)
        replacement.__doc__ = test_method.__doc__
        replacement.__name__ = 'XXX_' + test_method.__name__
        replacement.todo = reason
        return replacement
    return wrapper


dependencyCache = {}

# setup java CLASSPATH
cp = []

baseDir = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..'))
libDir = os.path.join(baseDir, 'lib')

jar = os.path.join(libDir, 'stringtemplate-3.0.jar')
if not os.path.isfile(jar):
    raise DistutilsFileError(
        "Missing file '%s'. Grap it from a distribution package."
        % jar,
        )
cp.append(jar)

jar = os.path.join(libDir, 'antlr-2.7.7.jar')
if not os.path.isfile(jar):
    raise DistutilsFileError(
        "Missing file '%s'. Grap it from a distribution package."
        % jar,
        )
cp.append(jar)

jar = os.path.join(libDir, 'junit-4.2.jar')
if not os.path.isfile(jar):
    raise DistutilsFileError(
        "Missing file '%s'. Grap it from a distribution package."
        % jar,
        )
cp.append(jar)

cp.append(os.path.join(baseDir, 'runtime', 'Python', 'build'))

classpath = ':'.join([os.path.abspath(p) for p in cp])


class ANTLRTest(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        unittest.TestCase.__init__(self, *args, **kwargs)

        self.baseName = os.path.splitext(os.path.basename(sys.modules[self.__module__].__file__))[0]
        self.lexerModule = None
        self.parserModule = None
        
        
    def compileGrammar(self, grammarName=None):
        if grammarName is None:
            grammarName = self.baseName + '.g'
            
        testDir = os.path.dirname(os.path.abspath(__file__))

        # get dependencies from antlr
        if grammarName in dependencyCache:
            dependencies = dependencyCache[grammarName]

        else:
            dependencies = []
            cmd = ('cd %s; java -cp %s org.antlr.Tool -depend %s 2>/dev/null'
                   % (testDir, classpath, grammarName)
                   )
            
            fp = os.popen(cmd)
            for line in fp:
                a, b = line.strip().split(':')
                dependencies.append(
                    (os.path.join(testDir, a.strip()),
                     os.path.join(testDir, b.strip()))
                    )
            rc = fp.close()
            if rc is not None:
                raise RuntimeError(
                    "antlr -depend failed with code %s on grammar '%s':\n\n"
                    % (rc, grammarName)
                    + cmd
                    )

            dependencyCache[grammarName] = dependencies
            

        rebuild = False
        
        for dst, src in dependencies:
            if (not os.path.isfile(dst)
                or os.path.getmtime(src) > os.path.getmtime(dst)
                ):
                rebuild = True

        if rebuild:
            fp = os.popen('cd %s; java -cp %s org.antlr.Tool %s 2>&1'
                          % (testDir, classpath, grammarName)
                          )
            output = ''
            failed = False
            for line in fp:
                output += line

                if line.startswith('error('):
                    failed = True

            rc = fp.close()
            if rc is not None:
                failed = True
                
            if failed:
                raise RuntimeError(
                    "Failed to compile grammar '%s':\n\n" % grammarName
                    + output
                    )


    def lexerClass(self, base):
        """Optionally build a subclass of generated lexer class"""
        
        return base


    def parserClass(self, base):
        """Optionally build a subclass of generated parser class"""
        
        return base


    def __load_module(self, name):
        modFile, modPathname, modDescription \
                 = imp.find_module(name, [os.path.dirname(__file__)])

        return imp.load_module(
            name, modFile, modPathname, modDescription
            )
    
        
    def getLexer(self, *args, **kwargs):
        """Build lexer instance. Arguments are passed to lexer __init__()."""


        self.lexerModule = self.__load_module(self.baseName + 'Lexer')
        cls = getattr(self.lexerModule, self.baseName + 'Lexer')
        cls = self.lexerClass(cls)

        lexer = cls(*args, **kwargs)

        return lexer
    

    def getParser(self, *args, **kwargs):
        """Build parser instance. Arguments are passed to lexer __init__()."""
        
        self.parserModule = self.__load_module(self.baseName + 'Parser')
        cls = getattr(self.parserModule, self.baseName + 'Parser')
        cls = self.parserClass(cls)

        parser = cls(*args, **kwargs)

        return parser

