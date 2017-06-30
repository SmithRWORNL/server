# DOECode Web Application

Consists of the "back-end" services and JAX/RS API calls for DOE Code, to be
accessed by the front-end or presentation layer.  This application is targeted
at a non-EE Java container such as Tomcat, using JPA and JAX/RS (Jersey implementation)
for persistence layer and web API implementation.

## Running the back-end

The application will run on most back-end Java EE platforms, tested
specifically on Jetty and Tomcat.  This assumes one already has a persistence
store up-and-running; define your access information via a local maven
profile, ensure you define:

Parameter | Definition
--- | ---
${database.driver} | the JDBC database driver to use
${database.url} | the JDBC URL to access
${database.username} | the database user (with create/alter schema permission)
${database.password} | the user's password
${serviceapi.host} | base URL for validation services
${publishing.host} | base URL for submitting final metadata to OSTI (via /submit API)
${datacite.username} | (optional) DataCite user account name for registering DOIs
${datacite.password} | (optional) DataCite account password for DOI registration

Execute the back-end via:

```bash
mvn jetty:run

# or
mvn tomcat:run
```

as you prefer.  Services by default will be available on localhost port 8080.

Note that log4j assumes tomcat as a basis for its files; simply include the
command line switch to override:

```bash
mvn -P *your-profile* -Dcatalina.base=$HOME jetty:run
```

to have logs in $HOME/logs/doecode.log via log4j default configuration.

The value of ${database.driver} is org.apache.derby.jdbc.EmbeddedDriver for Derby.

## API services

`GET /services/metadata/{ID}`

Retrieves a specified Metadata by its unique ID value, in JSON format.

`GET /services/metadata/yaml/{ID}`

Retrieve a specified Metadata by its unique ID value, in YAML format.

`GET /services/metadata/autopopulate?repo={URL}`

Calls the Connector services to attempt to scrape/auto-populate metadata information
if possible by deriving the appropriate repository from the URL.  Empty JSON is
returned if the determination cannot be made or the project does not exist or
is otherwise inaccessible.

`GET /services/metadata/autopopulate/yaml?repo={URL}`

As above, but returns YAML.

`POST /services/metadata`

Store a given metadata JSON to the DOECode persistence layer in an incomplete or
pending status.  The resulting JSON information is returned as the JSON object "metadata",
including the generated unique IDs as appropriate if the operation was successful. Record
is placed in the "Saved" work flow.

`POST /services/metadata/yaml`

Takes in JSON format metadata information, and returns that information in the YAML
format.  Does not persist any data.

`POST /services/metadata/publish`

Store the metadata information to the DOECode persistence layer with a "Published"
work flow.  JSON is returned as with the "Saved" service above, and this record
is marked as available to the DOECode search output services.  If DataCite information
has been configured, this step will attempt to register any DOI entered and update
metadata information with DataCite.

`POST /services/metadata/submit`

Post the metadata to OSTI, attempt to register a DOI if possible, and persist
the information on DOECode.  If workflow validations pass, the JSON will be returned
with appropriate unique identifier information and DOI values posted in the
JSON object "metadata".  Data is placed in "Published" state.

`POST /services/validation`

Send JSON detailing a set of award number values or DOIs to validate.

```json
{
    "values": ["10.5072/2134", "10.5072/238923", ...],
    "validations": ["DOI"]
}
```

Each value will be checked, and JSON "errors" array returned.  Each value of the
array should correspond with the passed-in "values" items.  If the position in the
"errors" array is blank, that value may be assumed valid; otherwise, an error
message will be returned.

```json
{
    "errors": ["10.5072/2134 is not a valid DOI.", "", ...]
}
```

## Configuring settings.xml

Database parameters are provided through the ~/.m2/settings.xml file. The following is a sample using the full (non-embedded) Derby Database:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <profiles>
        <profile>
            <id>doecode</id>
            <properties>
                <github.apikey>your-key-goes-here</github.apikey>
                <github.user>username</github.user>
                <!-- The following line configures the URL to the full Derby database that is running on the network. -->
                <database.url>jdbc:derby://localhost:1527/DOECode;create=true</database.url>
                <database.driver>org.apache.derby.jdbc.ClientDriver</database.driver>
                <database.user></database.user>
                <database.password></database.password>
                <database.dialect>org.hibernate.dialect.DerbyDialect</database.dialect>
                <database.schema>doecode</database.schema>
            </properties>
        </profile>
    </profiles>
</settings>
```

## Creating a Derby Database in Eclipse

It is often useful to have a simple database for testing that is not your institutions fully deployed database. The following steps outline how to create such a database in Eclipse.
1) Install Eclipse Data Platform from the Help->Install New Software Menu if you do not already have it. The full list of update sites is available at http://www.eclipse.org/datatools/downloads.php.
2) Install Apache Derby (either by downloading it manually or installing it via a package manager).
3) In Eclipse, open the "Database Development" perspective.
4) Follow the [Eclipse Documentation](http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.datatools.common.doc.user%2Fdoc%2Fhtml%2Fasc1229700387729.html) to create a Derby Connector, create a connection profile, and connect to Derby.

In step 4, be sure to select "Derby Client Driver" instead of "Derby Embedded Driver." DOE Code is not currently configured to work with the Embedded driver.

## Running on AWS

The DOE Code server works well on AWS. For the default RHEL 7 instance, the server can be executed with a Derby database for storing using the following rough steps:
1) Create the instance. Make sure your security group is configured to let the necessary ports through (normally 8080).
2) SSH into the instance using your key. Issue the following commands to download and install prerequisites including Java, Git, and Derby.
```bash
sudo yum install git java-1.8.0* wget
wget https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
wget ftp://mirror.reverse.net/pub/apache/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.tar.gz
wget http://mirror.stjschools.org/public/apache//db/derby/db-derby-10.13.1.1/db-derby-10.13.1.1-bin.tar.gz
tar -xzvf apache-maven-3.5.0-bin.tar.gz
sudo mkdir /opt/Apache
sudo cp db-derby-10.13.1.1-bin.zip /opt/Apache/
cd /opt/Apache/
sudo unzip db-derby-10.13.1.1-bin.zip
```
3) Checkout the server code
```bash
git clone https://github.com/doecode/server
```
4) Use an editor to add the following line to your .bashrc file:
```bash
export DERBY_INSTALL=/opt/Apache/db-derby-10.13.1.1-bin
```
5) Start Derby
sudo /opt/Apache/db-derby-10.13.1.1-bin/bin/startNetworkServer &
6) Edit your local Maven settings file to point it to Derby using the following content
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <profiles>
        <profile>
            <id>doecode</id>
            <properties>
                <github.apikey>your-key-goes-here</github.apikey>
                <github.user>username</github.user>
                <database.url>jdbc:derby://localhost:1527/DOECode;create=true</database.url>
                <database.driver>org.apache.derby.jdbc.ClientDriver</database.driver>
                <database.user>toby</database.user>
                <database.password>keith</database.password>
                <database.dialect>org.hibernate.dialect.DerbyDialect</database.dialect>
                <database.schema>doecode</database.schema>
            </properties>
        </profile>
    </profiles>
</settings>
```
7) Start the server in test mode
```
cd ~/server
~/apache-maven-3.5.0/bin/mvn -P doecode jetty:run
```
