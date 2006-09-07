#! /bin/bash

# Sill script for building libraries with GNU C
# compiler (gcc 4.0.1)
#
echo Pre-build clean...
rm -f *.o *.a *.so
echo "Attempt perforce checkout (ignore errors)..."
p4 edit ./binaries/macx86/* 2>/dev/null
rm -f ./binaries/macx86/*
echo "Compile debug versions of objects..."
gcc -g -c -I./include src/*.c 2>/dev/null
echo "Create libriares..."
ar -cr ./binaries/macx86/libantlr3cd.a *.o
gcc -dynamiclib -o ./binaries/macx86/libantlr3cd.so *.o 
rm -f *.o
echo "Compile -O2 optimized versions of objects..."
gcc -O2 -c -I./include src/*.c 2>/dev/null
echo "Create libriares..."
ar -cr ./binaries/macx86/libantlr3c.a *.o
gcc -dynamiclib -o ./binaries/macx86/libantlr3c.so *.o 
echo
chmod a+rx ./binaries/macx86/*.so
chmod a+r ./binaries/macx86/*.a
chmod a-w ./binaries/macx86/*.a ./binaries/macx86/*.so
cp ./binaries/macx86/* /usr/local/lib
rm -f *.o *.a *.so
#
echo "======================================================"
echo "Runtime libraries placed in ${PWD}/binaries/macx86"
echo
ls -l ./binaries/macx86/*
echo
echo "======================================================"
echo "Runtime libraries placed in /usr/local/lib"
echo
ls -l /usr/local/lib/libantlr*
echo
echo Complete
echo
