rm -rf karaf/deploy/*
rm -rf karaf/local/*
cp karaf/custom/config/org.apache.karaf.features.cfg-dev karaf/etc/org.apache.karaf.features.cfg
cp karaf/custom/config/org.ops4j.pax.logging.cfg-shell karaf/etc/org.ops4j.pax.logging.cfg
mkdir karaf/deploy -p
cp org.lorainelab.igb.feature/target/features/features-hotdeploy.xml karaf/deploy/
karaf/bin/igb clean debug
