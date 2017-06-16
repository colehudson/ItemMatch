# ItemMatch
Custom search component that discovers associated items for Solr documents given a specified field.

## Installation Instructions
Install Maven on your system.
Clone repository.
Make sure your JAVA_HOME is set. NB: for instances of fedora-stack, that is usually--JAVA_HOME=/usr/lib/jvm/java-8-oracle/
Go ItemMatch directory root.
Run `mvn install`
Copy resulting jar file to your Solr jar files' location (ItemMatch jar will be found in `target` directory)
Set up solrconfig.
  `<searchComponent name="ItemMatch" class="com.bfm.apps.solr.plugin.components.ItemMatch">
    <str name="field">SOLR_FIELD</str>
    <str name="path">FULL_PATH_TO_SOLR_CORE_INDEX_DIRECTORY</str>
  </searchComponent>`
  
Add following section under the /select requestHandler.
    `<arr name="last-components">
      <str>ItemMatch</str>
    </arr>`


