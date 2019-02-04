FSAUtils
========

Models of finite automata (DFA, NFA) with support of common operations and easily readable creation of objects.

The main goals of this project are:

* Support of easily readable definitions of finite automata (FA) and regular expressions
* Support of important basic operations on FA
* Adherence to the following coding guidelines aiming to assure correctness:
    * Simple and easily understandable code
    * Mostly adherence to the functional programming paradigm
    * Functional parts of the code (the core) closely follows abstract mathematical definitions of the respective operations

Features supported so far
-------------------------

* Creation of Deterministic Finite Automata (DFA)
* Creation of Nondeterministic Finite Automata (NFA)
* Determinization of NFA
* Creation of Regular Expressions (RE)
* Checking for acceptance of a word by an automaton
* Concatenation, Star, Union, Intersection, Complement for DFA/NFA
* Checking DFA/NFA for equivalence
* Implicit conversion of DFA to NFA
* Pretty-printing toString methods for DFA/NFA
* Determination of the language (RE) of a DFA/NFA
* Conversion of RE to NFA (i.e. also checking of equivalence of DFA/NFA with RE)
* Minimization of DFA
* (De-)Serialization to and from XML

Get Started (1.1 Beta Version)
------------------------------

**Prerequisites:** You need to have Scala and the JVM installed. FSAUtils
has been tested with Scala 2.11.2 and Java 1.7. Furthermore, the environment
variable `$SCALA_HOME` has to be correctly set to the path where Scala resides.

The following steps should work for a Linux system.

1. Download the archive:
   
   ```bash
   wget https://github.com/rindPHI/FSAUtils/archive/v1.1-beta.tar.gz -O FSAUtils-1.1-beta.tar.gz
   ```
   
2. Extract it:
   
   ```bash
   tar xzf FSAUtils-1.1-beta.tar.gz
   ```
   
2. Build it:
   
   ```bash
   cd FSAUtils-1.1-beta/
   ant
   ```
   
   As the result, you find a file "FSAUtils.jar" in the directory `lib/`
   which you need to add to the classpath of scalac and scala in order
   to compile / run your objects that make use of FSAUtils.
   
3. In your Scala files, add the import

   ```scala
   import de.dominicscheurer.fsautils._
   ```
   
   and, if you want to use the FSA domain specific language
   for better readability, let your object extend `FSA_DSL`:
   
   ```scala
   object MyObject extends FSA_DSL {
   ```
   
4. Compile your scala object:
   
   ```bash
   scalac -classpath "/path/to/FSAUtils.jar" YourObject.scala
   ```
   
5. ...and run it:
   
   ```bash
   scala -classpath ".:/path/to/FSAUtils.jar" YourObject
   ```
   
An example file like mentioned in points 3. to 5. could have, for instance,
the following content:

```scala
import de.dominicscheurer.fsautils._

object FSAUtilsTest extends FSA_DSL {
  
    def main(args: Array[String]) {
      val myDFA =
            dfa ('Z, 'S, 'q0, 'd, 'A) where
                'Z  ==> Set('a, 'b)   and
                'S  ==> Set(0, 1)     and
                'q0 ==> 0             and
                'A  ==> Set(0)        and
                'd  ==> Delta(
                      (0, 'a) -> 0,
                      (0, 'b) -> 1,
                      (1, 'a) -> 0,
                      (1, 'b) -> 1
                )|
        
        print("DFA accepts aaab: ")
        println(myDFA accepts "aaab")
        print("DFA accepts aaaba: ")
        println(myDFA accepts "aaaba")
    }
    
}
```

If you wish to run the included unit tests, execute

```bash
ant runTests
```

in the `FSAUtils-1.1-beta` directory.

Examples
--------

Please consider the file Test.scala to see some working applied examples.

### Creation of a DFA

```scala
val myDFA =
    dfa ('Z, 'S, 'q0, 'd, 'A) where
	    'Z  ==> Set('a, 'b)   and
	    'S  ==> Set(0, 1)     and
	    'q0 ==> 0             and
	    'A  ==> Set(0)        and
	    'd  ==> Delta(
              (0, 'a) -> 0,
              (0, 'b) -> 1,
              (1, 'a) -> 0,
              (1, 'b) -> 1
        )|

print("DFA accepts aaab: ")
println(myDFA accepts "aaab")
```

### Creation of an NFA

```scala
val myNFA =
    nfa ('Z, 'S, 'q0, 'd, 'A) where
        'Z  ==> Set('a, 'b)   and
        'S  ==> Set(0, 1)     and
        'q0 ==> 0             and
        'A  ==> Set(1)        and
        'd  ==> Delta(
              (0, 'a) -> Set(0, 1),
              (0, 'b) -> Set(0)
        )||

print("NFA accepts aaab: ")
println(myNFA accepts "aaab")
```

### Star Operation for NFA

```scala
println((myNFA*) accepts "aaabaaab")
```

### Determinization for NFA

```scala
println((myNFA toDFA) accepts "aaab")
```

### Complement for DFA

```scala
println((!myDFA) accepts "aaab")
```

### Concatenation

```scala
println(myNFA ++ myNFA2);
```

### Pretty Printing

`println(myNFA toDFA)` yields:

```
DFA (Z,S,q0,d,A) with
|    Z = {a,b}
|    S = {{},{0},{1},{0,1}}
|    q0 = {0}
|    A = {{1},{0,1}}
|    d = {
|        ({},a) => {},
|        ({},b) => {},
|        ({0},a) => {0,1},
|        ({0},b) => {0},
|        ({1},a) => {},
|        ({1},b) => {},
|        ({0,1},a) => {0,1},
|        ({0,1},b) => {0}
|    }
```

### Creation of RE

```scala
def myRegExp = (('a*) + ('b & ('b*) & 'a))* : RE
```

License
-------

### MIT License

Copyright (c) 2014-2019 Dominic Steinhöfel

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
