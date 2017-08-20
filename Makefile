LOG_FILE = /tmp/load-test.log
TARGET = target/load-test
CSV_HEADER = Id,Name,Username,ProjectId,AccessToken

PERSONAS = Margaret Armin Anne Martin Marie
THREADS = 15
LOOPS = 10
Armin_THREADS = 100
Armin_LOOPS = 150

log = $(shell echo "$@" | tee -a "$(LOG_FILE)")
log_start = $(call log,$(1))

$(TARGET)/load-test.properties:
	echo "[AppSpecific]" > $@

$(TARGET)/%.sql: $(TARGET)/load-test.properties
	mkdir -p $(TARGET)
	$(eval PERSONA := $(@:$(TARGET)/%.sql=%))
	$(eval T := $(or $($(PERSONA)_THREADS),$($(PERSONA)_THREADS),$(THREADS)))
	$(eval L := $(or $($(PERSONA)_LOOPS),$($(PERSONA)_LOOPS),$(LOOPS)))
	for i in `seq 1 $(T)`; do \
		load-test/user-template.sh $(@:$(TARGET)/%.sql=%) $$i ; \
	done >> $@

	echo >> $<
	echo "; Persona $(PERSONA)" >> $<
	echo "$(@:$(TARGET)/%.sql=%.threads) = $(T)" >> $<
	echo "$(@:$(TARGET)/%.sql=%.loops) = $(L)" >> $<
	echo "$(@:$(TARGET)/%.sql=%.csv) = $(PWD)/$(@:$(TARGET)/%.sql=$(TARGET)/%.csv)" >> $<

$(TARGET)/%.csv: $(TARGET)/%.sql
	mkdir -p $(TARGET)
	echo $(CSV_HEADER) > $@
	grep -E ^-- "$^" | sed 's/^-- //' | grep "$(@:$(TARGET)/%.csv=%)" >> $@

$(TARGET)/init.sql: $(PERSONAS:%=$(TARGET)/%.csv)
	mkdir -p $(TARGET)
	cat $(^:$(TARGET)/%.csv=$(TARGET)/%.sql) > $@

init.sql: $(TARGET)/init.sql

clean:
	rm -f $(PERSONAS:%=$(TARGET)/%.csv)
	rm -f $(PERSONAS:%=$(TARGET)/%.sql)
	rm -f $(TARGET)/init.sql
	rm -f $(TARGET)/load-test.properties
