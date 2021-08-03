all: compile demo
compile:
	-chmod u+x ./*.sh
	./compile.sh
demo:
	-chmod u+x ./*.sh
	rm -rf outputInfo
	mkdir outputInfo
	@echo [DEMO] running CutNPeel...
	./run_cutnpeel.sh ./data/test.txt 0.9 1 test

