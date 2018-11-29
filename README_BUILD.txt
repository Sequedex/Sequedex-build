The path to the sequedex build release or the directory where the Sequedex 
project has been cloned or checked out from git repository (i.e. where 
.git directory resides) is referred to below as <git-top-level>. This file 
(README_BUILD.txt) should be found in <git-top-level>.

The path to the directory where the sequedex 
binary release is located is referred to as <sequedex-dir>.

Steps for building Sequedex:

If you downloaded and uncompressed a build release of Sequedex, then go directly 
to step 4.

Step 1.

Clone (or checkout) Sequedex project from github into <git-top-level>.  

You may also need to download the Sequedex binary release (see steps
2 and 3). 

Step 2.

If it does not exist, create a subdirectory called data in <git-top-level>. The
data subdirectory should be populated with data modules to be included in the 
sequedex distribution you are building.  Note: additional data modules may be 
added later by the user but at least one data module is required to run Sequedex.  
The easiest way to create and populate the data directory in one step
is to copy the data directory from the binary release of Sequedex.  For example, 
from the command line on Linux or Mac:

cp -rp <path-to-sequedex-dir>/data <git-top-level>

Step 3.

If it does not exist, create a subdirectory called lib in 
<git-top_level>.  This subdirectory must contain the required third party 
jar files (and nothing else). See LICENSE.txt for current list of
required jar files. The easiest way to create and populate this subdirectory
is to copy the lib/lib directory from a compatible binary release of Sequedex.
For example, from the command line on Linux or Mac:

cp -rp <path-to-sequedex-dir>/lib/lib <git-top-level>

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
lib
licenses
src

If data and lib directories were copied from current binary release, 
data subdirectory should contain:

Life2550-8GB.1.jar
virus10k.1.jar

and lib should contain:

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

Build the Sequedex binary release by executing the build script <git-top-level/build_standalone.xml.  This script must be executed using Apache Ant.  If Apache Ant 
is not already installed on your computer, it can be found at http://ant.apache.org.  
To check if Apache Ant is installed and in PATH, at the command prompt execute:

ant -version

Response should be something like:
Apache Ant(TM) version 1.10.1 compiled on February 2 2017

build_standalone.xml has been run successfully on both Mac and Linux using Apache Ant 
versions 1.10.1 and 1.10.5.

To run the build script from the command line if ant is in PATH:

ant -f <git-top-level>/build_standalone.xml 

otherwise: 

<path-to-ant-distrib>/bin/ant -f <git-top-level>/build_standalone.xml

This will produce the directory <git-top-level>/sequedex, which has everything 
needed to run Sequedex.  Currently it is not creating javadoc.  But hopefully 
this will be added soon.  Also, for the moment, it is just copying 
SequedexQuickStart.docx and SequedexQuickStart.pdf from <git-top-level/doc,
which needed to be manually generated and put in <git-top-level>/doc 
as needed. The goal is to replace these with .tex and associated files 
which can be compiled into a .pdf file in the build script using TeX.

Step 5.

The sequedex directory created by the build script can then be moved
wherever needed for running Sequedex or compressed with tar or zip 
for release.


Judith Cohn
28 November 2018
