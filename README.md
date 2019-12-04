# MIPSolver v1.0.0 #
Author: Koos van der Linden
Delft University of Technology   

MIPSolver is a wrapper for different mip solvers. Currently it supports Gurobi and GLPK


## Building from source ##
MIPSolver comes with a Maven project configuration file. In order to generate an executable jar file, you only need to execute the command `mvn package`. You can also import the project in your Eclipse workspace. Note that the Gurobi and the GLPK library file has not been included in the libs directory. This jar file can be found in your Gurobi installation directory.


### Installing Gurobi or GLPK ###
The following steps may help you in setting up the Gurobi or GLPK dependency. Note that Gurobi requires a license.
The following steps are for use on windows
#### Gurobi ####
1. Install Gurobi from http://www.gurobi.com
2. Ensure the license file is set up
3. Add the gurobi.jar as a dependency (you can find the file in the lib folder of gurobi's installation directory)
4. Set up the following environment variables (paths are example paths):
   ```
   GUROBI_HOME = C:\gurobi751\win64\
   GRB_LICENSE_FILE = C:\gurobi751\win64\gurobi.lic
   LD_LIBRARY_PATH = C:\gurobi751\win64\lib
   PATH = C:\gurobi751\win64\bin
   ```
5. Link the native library
   * When working in an IDE, set C:\gurobi751\win64\bin (change to your actual path) as the folder for the native library for gurobi.jar
   * Otherwise: add the following parameter to your execution command:
      ```
      -Djava.library.path="C:\gurobi751\win64\bin"
      ```
#### GLPK ####
1. Install GLPK from https://sourceforge.net/projects/winglpk/ and http://winglpk.sourceforge.net
2. Add the glpk-java.jar as a dependency (you can find the file in the w64 folder of glpk's installation directory)
3. Link the native library
   * When working in an IDE, set the "<your_path>\glpk-4.63\w64 as the folder for the native library for glpk-java.jar
   * Otherwise: add the following parameter to your execution command:
      ```
      -Djava.library.path="<your_path>\glpk-4.63\w64"
      ```
