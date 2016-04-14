rm -rf karaf/deploy/*
cp karaf/custom/config/org.apache.karaf.features.cfg-dev karaf/etc/org.apache.karaf.features.cfg 
cp karaf/custom/config/org.ops4j.pax.logging.cfg-noshell karaf/etc/org.ops4j.pax.logging.cfg
cp org.lorainelab.igb.feature/target/features/features-hotdeploy.xml karaf/deploy/
karaf/bin/igb server
