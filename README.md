# ItemMatch
Custom search component that discovers associated items for Solr documents given a specified field.

## Installation Instructions
Install Maven on your system.

Clone this repository.

Make sure your JAVA_HOME is set. NB: for instances of fedora-stack, that is usually JAVA_HOME=/usr/lib/jvm/java-8-oracle/

Go to root of the ItemMatch directory.

Run `mvn install`

ItemMatch jar will be found in `target` directory

Rename to ItemMatch.jar and copy resulting jar file to your Solr jar files' directory.

Edit the solrconfig.xml file for the Solr core you wish to use. Add in the following sections.

For ease of use, place this section above the /select request handler section.

Edit the `field` and `path` fields below to match your wanted settings.
  ```xml
  <searchComponent name="ItemMatch" class="com.bfm.apps.solr.plugin.components.ItemMatch">
    <str name="field">SOLR_FIELD_FOUND_IN_YOUR_CORE</str>
    <str name="path">FULL_PATH_TO_SOLR_CORE_INDEX_DIRECTORY</str>
  </searchComponent>
  ```

Add following section inside the /select requestHandler section.
  ```xml
    <arr name="last-components">
      <str>ItemMatch</str>
    </arr>
  ```

### Notes
Tested on Solr 4.1.0
