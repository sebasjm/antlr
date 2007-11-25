import sys
import os
import glob

targetDir = os.path.abspath(os.path.dirname(__file__))
rootDir = os.path.abspath(os.path.join(targetDir, '..', '..'))

jars = glob.glob(os.path.join(rootDir, 'lib', '*.jar'))
jars += glob.glob(os.path.join(rootDir, 'build', '*.jar'))
CLASSPATH = ':'.join(jars)
PYTHONPATH = targetDir

if len(sys.argv) != 2:
    sys.stderr.write("USAGE: %s cmd\n" % sys.argv[0])
    sys.exit(1)
    
if sys.argv[1] == 'build':
    pass

elif sys.argv[1] == 'unittest':
    os.chdir(targetDir)
    os.execvpe(
        'python2.4',
        ['python2.4', 'setup.py', 'unittest'],
        { 'CLASSPATH': CLASSPATH,
          'PYTHONPATH': PYTHONPATH
          }
        )

elif sys.argv[1] == 'functest':
    os.chdir(targetDir)
    os.execvpe(
        'python2.4',
        ['python2.4', 'setup.py', 'functest'],
        { 'CLASSPATH': CLASSPATH,
          'PYTHONPATH': PYTHONPATH
          }
        )

else:
    sys.stderr.write("Invalid command %r\n" % sys.argv[1])
    sys.exit(1)
