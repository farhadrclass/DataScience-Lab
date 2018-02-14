# README : APTED 
This project has an implementation of Tree Edit Distance (TED).
 
APTED is originally developed by Mateusz Pawlik and Nikolaus Augsten.
The original implementation can be found at http://tree-edit-distance.dbresearch.uni-salzburg.at/#download

The source code from original implementation has been reorganized to make it as a reusable library package for maven
 based builds.

### Requirements
+ Newer version of Maven (Tested on 3.0.5)
+ Newer version of JDK (Tested on 1.7.0_95)

## Build instructions 
+ `mvn clean test package` to test and and package. Jar will be at `target/apted-*.jar`
+ `mvn install` to use it as a maven library for other projects. Then add the following as dependency to your project   

```xml
   <dependency>
   <groupId>edu.usc.irds.ted</groupId>
   <artifactId>apted</artifactId>
   <version>0.1.1</version>
   </dependency>
```

## LICENCE
The original project is distributed under MIT licence,
 so this project is available under MIT licence. Find the licence header in the files. 


---

### (original) README 
This is an implementation of the APTED algorithm from [2]. It builds up on the
works in [1] and [3].

The source code is published under the MIT licence found in the header of each
source file.

To build, do the following steps from within the root directory:
  mkdir build
  cd build
  cmake ..
  make

[1] M. Pawlik and N. Augsten. Efficient Computation of the Tree Edit 
    Distance. ACM Transactions on Database Systems (TODS) 40(1). 2015.  
[2] M. Pawlik and N. Augsten. Tree edit distance: Robust and memory-
    efficient. Information Systems 56. 2016.  
[3] M. Pawlik and N. Augsten. RTED: A Robust Algorithm for the Tree Edit 
    Distance. PVLDB 5(4). 2011.