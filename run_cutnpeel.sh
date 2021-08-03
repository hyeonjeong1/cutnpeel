# Program : run_cutnpeel.sh
# Description : Run the CutNPeel algorithm

java -Xmx90g -cp ./CutNPeel.jar:./fastutil-8.5.4.jar cutnpeel.CutNPeel $@
