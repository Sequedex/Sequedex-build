In this file, <git-top-level> refers to the top level of a Sequedex  
release or the directory where the Sequedex project has been cloned or 
checked out from the git repository (i.e. where .git directory resides).  
This file (README_BUILD.txt) should be found in <git-top-level>.

Steps for building Sequedex:

Step 1.

Either

a)  clone (or checkout) Sequedex project from github into <git-top-level> for latest version of source code; or
b)  download the source code for Sequedex release (either .zip or .tar.gz) from the release assets.


Step 2.

If it does not exist, create a subdirectory called data in <git-top-level>. The
data subdirectory should be populated with data modules to be included in the 
sequedex distribution you are building.  Note: additional data modules may be 
added later by the user but at least one data module is required to run Sequedex.  
The easiest way to populate the data directory is download sequedex.zip or sequedex.tgz 
from the release assets, then uncompress.  This will generate a directory called
sequedex, which contains all the files, including executables, needed to run Sequedex. 
Copy the files in sequedex/data to <git-top-level>/data.  For example, 
from the command line on Linux or Mac:

cp -rp <path-to-unzipped-sequedex-dir>/data/* <git-top-level>/data

Step 3.

If it does not exist, create a subdirectory called thirdPartyJarFiles in 
<git-top_level>.  This subdirectory must contain the required third party 
jar files (and nothing else). See LICENSE.txt for current list of
required jar files. The easiest way to populate this subdirectory is similar to 
the instructions in Step 2 for the data directory - i.e. find sequedex.zip or
sequedex.tgz in the assets for the release, uncompress it, and copy the files in 
sequedex/lib/lib to thirdPartyJarFiles.  For example, from the command line on Linux or Mac:

cp -rp <path-to-unzipped—sequedex-dir>/lib/lib/* <git-top-level>/thirdPartyJarFiles

When steps 2 and 3 are completed, <git-top-level> should contain:

.git
.gitignore
CHANGELOG.txt
LICENSE.txt
README.txt
README_BUILD.txt
build_standalone.xml
data
doc
etc
licenses
src
thirdPartyJarFiles

If files for the data and thirdPartyJarFiles directories were copied from the 
a Sequedex release, data subdirectory should contain:

Life2550-8GB.1.jar
virus10k.1.jar

and thirdPartyJarFiles should contain:

commons-lang3-3.7.jar
forester_1050.jar
java-getopt-1.0.14.jar
jgoodies-common-1.4.0.jar
jgoodies-forms-1.6.0.jar
jna-4.5.2.jar
jna-platform-4.5.2.jar
logback-classic-1.2.3.jar
logback-core-1.2.3.jar
oshi-core-3.4.4.jar
slf4j-api-1.7.25.jar
threetenbp-1.3.6.jar


Step 4.

To build the runtime directory for Sequedex,  the build script <git-top-level/build_standalone.xml must be executed.  If <git-top-level>/sequedex directory already exists, the build script will delete it before building a new version.  This script can be executed using Apache Ant.  If Apache Ant is not already installed, it can be found at 
http://ant.apache.org.  To check if Apache Ant is installed and in PATH, 
at the command prompt execute:

ant -version

Response should be something like:
Apache Ant(TM) version 1.10.1 compiled on February 2 2017

build_standalone.xml has been run successfully on both Mac and Linux using 
Apache Ant versions 1.10.1 and 1.10.5.

To run the build script from the command line if ant is in PATH:

ant -f <git-top-level>/build_standalone.xml 

otherwise: 

<path-to-ant-distrib>/bin/ant -f <git-top-level>/build_standalone.xml

Currently the build script does not create a javadoc.  But hopefully 
this will be added soon.  Also, for the moment, it is just copying 
SequedexQuickStart.docx and SequedexQuickStart.pdf from <git-top-level/doc.
Thus if any changes are made to the docx version, a pdf must be manually
generated and put in <git-top-level>/doc as needed. The goal is to replace 
these with .tex and associated files, which can be compiled into a .pdf file 
in the build script using TeX.

Step 5.

The sequedex directory created by the build script can be moved
wherever needed for running Sequedex or compressed with tar or zip 
for release.


Judith Cohn
10 December 2018
