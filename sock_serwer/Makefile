SHELL := bash

JDIR := java
CDIR := classes

JFILES := $(wildcard $(JDIR)/*.java)
CFILES := $(patsubst $(JDIR)/%.java,$(CDIR)/%.class,$(JFILES))

JCP := javac 
JVM := java
JFLAGS := -cp $(CDIR) -d $(CDIR) -sourcepath $(JDIR) -g

MAIN := App

.PHONY: all clear run

all: $(CFILES)
	@echo '-------------------------------------------------------------'
	@echo 'COMPILATION SUCCEEDED'
	@echo '-------------------------------------------------------------'

$(CDIR)/%.class: $(JDIR)/%.java
	@$(JCP) $(JFLAGS) $<

run: all dirs
	$(JVM) -cp $(CDIR) $(MAIN)

dirs: 
	@[ -d $(JDIR) ] || mkdir $(JDIR)
	@[ -d $(CDIR) ] || mkdir $(CDIR)

	@echo 'DIRECTORIES PREPARED'

clear:
	@rm -v $(CDIR)/*
