# Integrated Genome Browser - JavaFX edition

The Integrated Genome Browser (IGB) is a fast, free, and flexible desktop genome browser
implemented in Java.

Here, we are re-writing IGB using JavaFX and 100% modular, services based programming
using OSGi.

# Quick Start 

Install maven build tool and Apache karaf OSGi container.

To build and run IGB-FX:

1. Clone this repository
2. Build IGB maven 
3. Launch IGB using start-shell.sh


`mvn clean install -DskipTests=true
start-shell`


# About IGB-FX

Like IGB classic, IGB-FX IGB runs in an OSGi container, which supports adding and removing pluggable Apps while IGB is running. 
For a tutorial on OSGi written by IGB Developers, see: 

* Stackleader.com [blog posts on OSGI](https://blog.stackleader.com/tags/osgi/)

# To contribute

IGB development uses the Fork-and-Branch workflow. See:

* Forking Workflow [tutorial](https://www.atlassian.com/git/tutorials/comparing-workflows/forking-workflow) by Atlassian
* Blog post titled [Using the Fork-and-Branch Git Workflow](http://blog.scottlowe.org/2015/01/27/using-fork-branch-git-workflow/)