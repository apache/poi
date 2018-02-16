Sample: SampleTemplate
Category: template
Author: Steven Traut (straut@bea.com)
Last Updated: Sept. 6, 2004

Versions:
    xmlbeans-v1 1.0.3
    xmlbeans-v2

-----------------------------------------------------------------------------

This sample template should be used when authoring new samples for XMLBeans.

Each sample MUST consist of the following:

    - Apache License 2.0 must appear on all files.

    - README.txt: in the same format as this README.  Sample name, author,
      last upated, and versions of XMLBeans this sample works with are listed
      at the top.

    - build.xml: with 'clean', 'build', 'run', and 'test' targets.  Follow the
      conventions in the build.xml distributed with this template.

    - schemas directory: contains any XMLSchema files used in compilation.
      Typically, the target namespace of the schema will be
      "http://xmlbeans.apache.org/samples/category/samplename" where
      "category" and "samplename" are replaced appropriately.

    - xml directory: contains any xml files used for validation by the sample.

    - src directory: contains any java source files.  The package of the
      sample should be 'org.apache.xmlbeans.samples.category' and the main
      class should be given the sample name.

At the end of the README's description, the desired output of running the sample
should be given and the steps to build and run should be provided.

To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. To compile the schemas and sample source, run "ant build"
4. To execute the sample, run "ant run"
