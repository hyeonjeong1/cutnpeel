echo compiling java sources...
rm -rf class
mkdir class

javac -cp ./fastutil-8.5.4.jar -d class $(find ./src -name *.java)

echo make jar archive...
cd class
jar cf CutNPeel.jar ./
rm ../CutNPeel.jar
mv CutNPeel.jar ../
cd ..
rm -rf class

echo done.
