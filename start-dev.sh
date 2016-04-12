rm -rf karaf/deploy/*
cp karaf/custom/config/org.ops4j.pax.logging.cfg-noshell karaf/etc/org.ops4j.pax.logging.cfg
cp org.lorainelab.igb.feature/target/features/features-hotdeploy.xml karaf/deploy/
karaf/bin/igb server clean debug
