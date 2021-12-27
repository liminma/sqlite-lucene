data_path = ./data
index_path = ./lucene-index

build:
	./gradlew build && \
	./gradlew --quiet installDist

index: clean
	./build/install/sqlite-lucene/bin/sqlite-lucene build-index $(data_path) $(index_path)
#	./build/install/sqlite-lucene/bin/sqlite-lucene build-index $(data_path)/03_icced2020rcced_p_sas_sas7bdat.db $(index_path)

search:
	./build/install/sqlite-lucene/bin/sqlite-lucene search $(index_path)

clean:
	rm -rf $(index_path)

.PHONY: build