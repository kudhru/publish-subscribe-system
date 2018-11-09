all : clean build

build :
	javac *.java
	rmic PubSubImpl
	cp PubSubImpl_Stub.class ../Client
clean :
	rm -f *.class