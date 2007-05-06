import os
import sys
from distutils.errors import *
from distutils.cmd import Command
from distutils.core import setup
from distutils import log

class TestError(DistutilsError):
    pass

    
class CmdUnitTest(Command):
    """Run unit tests for package"""

    description = "run unit tests for package"

    user_options = []
    boolean_options = []

    def initialize_options(self):
        pass
    
    def finalize_options(self):
        pass
    
    def run(self):
        testDir = os.path.join(os.path.dirname(__file__), 'unittests')
        if not os.path.isdir(testDir):
            raise DistutilsFileError(
                "There is not 'unittests' directory. Did you fetch the development version?",
                )

        import glob
        import imp
        import unittest
        import traceback
        import StringIO
        
        suite = unittest.TestSuite()
        loadFailures = []
        
        # collect tests from all unittests/test*.py files
        testFiles = []
        for testPath in glob.glob(os.path.join(testDir, 'test*.py')):
            testFiles.append(testPath)

        testFiles.sort()
        for testPath in testFiles:
            testID = os.path.basename(testPath)[:-3]

            try:
                modFile, modPathname, modDescription \
                         = imp.find_module(testID, [testDir])

                testMod = imp.load_module(
                    testID, modFile, modPathname, modDescription
                    )
                
                suite.addTests(
                    unittest.defaultTestLoader.loadTestsFromModule(testMod)
                    )
                
            except Exception:
                buf = StringIO.StringIO()
                traceback.print_exc(file=buf)
                
                loadFailures.append(
                    (os.path.basename(testPath), buf.getvalue())
                    )              
                
            
        runner = unittest.TextTestRunner(verbosity=2)
        result = runner.run(suite)

        for testName, error in loadFailures:
            sys.stderr.write('\n' + '='*70 + '\n')
            sys.stderr.write(
                "Failed to load test module %s\n" % testName
                )
            sys.stderr.write(error)
            sys.stderr.write('\n')
            
        if not result.wasSuccessful() or loadFailures:
            raise TestError(
                "Unit test suite failed!",
                )
            

class CmdFuncTest(Command):
    """Run functional tests for package"""

    description = "run functional tests for package"

    user_options = []
    boolean_options = []

    def initialize_options(self):
        pass
    
    def finalize_options(self):
        pass
    
    def run(self):
        testDir = os.path.join(os.path.dirname(__file__), 'tests')
        if not os.path.isdir(testDir):
            raise DistutilsFileError(
                "There is not 'tests' directory. Did you fetch the development version?",
                )

        # make sure, relative imports from testcases work
        sys.path.insert(0, testDir)

        import glob
        import imp
        import unittest
        import traceback
        import StringIO
        
        suite = unittest.TestSuite()
        loadFailures = []
        
        # collect tests from all tests/t*.py files
        testFiles = []
        for testPath in glob.glob(os.path.join(testDir, 't*.py')):
            if (testPath.endswith('Lexer.py')
                or testPath.endswith('Parser.py')
                ):
                continue

            testFiles.append(testPath)

        testFiles.sort()
        for testPath in testFiles:
            testID = os.path.basename(testPath)[:-3]

            try:
                modFile, modPathname, modDescription \
                         = imp.find_module(testID, [testDir])

                testMod = imp.load_module(
                    testID, modFile, modPathname, modDescription
                    )
                
                suite.addTests(
                    unittest.defaultTestLoader.loadTestsFromModule(testMod)
                    )
                
            except Exception:
                buf = StringIO.StringIO()
                traceback.print_exc(file=buf)
                
                loadFailures.append(
                    (os.path.basename(testPath), buf.getvalue())
                    )              
                
            
        runner = unittest.TextTestRunner(verbosity=2)
        result = runner.run(suite)

        for testName, error in loadFailures:
            sys.stderr.write('\n' + '='*70 + '\n')
            sys.stderr.write(
                "Failed to load test module %s\n" % testName
                )
            sys.stderr.write(error)
            sys.stderr.write('\n')
            
        if not result.wasSuccessful() or loadFailures:
            raise TestError(
                "Functional test suite failed!",
                )
            

setup(name='antlr3',
      version='1.0b1',
      packages=['antlr3'],

      cmdclass={'unittest': CmdUnitTest,
                'functest': CmdFuncTest
                },
      )
