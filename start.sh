rm -rf karaf/deploy/*
cp karaf/custom/config/org.apache.karaf.features.cfg-dev karaf/etc/org.apache.karaf.features.cfg 
cp karaf/custom/config/org.ops4j.pax.logging.cfg-noshell karaf/etc/org.ops4j.pax.logging.cfg
cp org.lorainelab.igb.feature/target/*.kar karaf/deploy/
karaf/bin/igb server
