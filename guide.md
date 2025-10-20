# 1) compile all sources
mkdir -p out/classes
find src/main/java -name '*.java' > sources.txt
javac -d out/classes @sources.txt

# 2) copy resources (icons, etc.)
cp -r src/main/resources/* out/classes/

# 3) download sqlite driver (one-time)
mkdir -p libs
wget -O libs/sqlite-jdbc.jar "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar"

# 4) run the app
java -cp "out/classes:libs/sqlite-jdbc.jar" main.Main