rm -rf karaf/deploy/*
cp org.lorainelab.igb.feature/target/*.kar karaf/deploy/
karaf/bin/igb server
