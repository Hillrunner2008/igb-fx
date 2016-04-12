del karaf\deploy\*.*
xcopy /f /y karaf\custom\config\org.ops4j.pax.logging.cfg-noshell karaf\etc\org.ops4j.pax.logging.cfg
xcopy /f /y org.lorainelab.igb.feature\target\features\features-hotdeploy.xml karaf\deploy\
call karaf\bin\start.bat server
