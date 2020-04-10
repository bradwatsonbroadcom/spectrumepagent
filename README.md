# spectrumepagent

This is an EPAgent plugin for CA APM to pull CA Spectrum device health into CA APM. The plugin connects to the Spectrum REST API and searches for the devices that belong to a specified Global Collection. It will then loop through the devices listed in the Global Collection and against call the Spectrum REST API to get the health status of the device.

AFTER properly installing an EPAgent, follow these steps to run the plugin and pull the metrics over:

1. Grab the hex value of the Spectrum Global Collection you want to monitor.

2. Create a directory in the epaplugins directory of your EPAgent and in that directory create a properties file that will contain the necessary information for polling the Spectrum REST API. Below is an example spectrum.properties file. NOTE: {MODEL} and {ATTRIBUTES} will be replaced by the values you provide in the properties file so do not update the URL to contain them.
spectrum.api.url=http://<SPECTRUMHOST:PORT>/spectrum/restful/model/{MODEL}?{ATTRIBUTES}  #Only <SPECTRUMHOST:PORT> should change here.
spectrum.api.model.url=http://<SPECTRUMHOST:PORT>/spectrum/restful/associations/relation/0x0001003b/model/{MODEL}?side=left  #Only <SPECTRUMHOST:PORT> should change here.
spectrum.api.user=USER
spectrum.api.password=PASSWORD
spectrum.api.model=0x1000aa5  #The model from step 1.
spectrum.api.attributes=attr=0x1000b&attr=0x1006e  #This most likely will not need to change.
spectrum.api.debug=false  #This will show detailed information about the calls made to the REST API.
spectrum.metric.path=Spectrum|Devices  #The metric tree you want the metrics to show up under in CA APM.

3. Copy the jar files under the lib directory of this project into either the directory created in step 2 or into the lib directory of the EPAgent. Regardless of which directory you choose, you will need the path in the following step. There should be 5 total jars:
commons-codec-X.X.jar
commons-logging-X.X.jar
httpclient-X.X.X.jar
httpcore-X.X.X.jar
json-XXXXXXXX.jar
SpectrumEPAgent.jar

4. Update your EPAgent IntroscopeEPAgent.properties file to include the stateless plugin configuration needed to run the plugin. It's a simply Java process so it's just a Java CLI startup command. Below is an example congiruation.
introscope.epagent.plugins.stateless.names=SPECTRUM
introscope.epagent.stateless.SPECTRUM.command=java -cp "E:/CAWILY/epagent/epaplugins/spectrum/commons-codec-1.9.jar;E:/CAWILY/epagent/epaplugins/spectrum/commons-logging-1.2.jar;E:/CAWILY/epagent/epaplugins/spectrum/httpclient-4.5.2.jar;E:/CAWILY/epagent/epaplugins/spectrum/httpcore-4.4.4.jar;E:/CAWILY/epagent/epaplugins/spectrum/json-20180813.jar;E:/CAWILY/epagent/epaplugins/spectrum/utils.jar;E:/CAWILY/epagent/epaplugins/spectrum/SpectrumEPAgent.jar" -Dprops=E:/CAWILY/epagent/epaplugins/spectrum/spectrum.properties com.broadcom.apmsql.EPAgent
introscope.epagent.stateless.SPECTRUM.delayInSeconds=60  #This is how often you want to run the poller to get the device health.

5. Restart your EPAgent process and check the log files for any errors. If all steps were followed correctly, you should now see the device health metrics in the CA APM metric tree under your EPAgent.
