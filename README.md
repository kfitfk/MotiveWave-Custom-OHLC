编译

javac -cp "build/stubs_out:/Applications/MotiveWave.app/Contents/Java/mwave_sdk.jar" -d out CustomOHLC.java && \
cd out && jar cf ../CustomOHLC.jar com/ && cd ..