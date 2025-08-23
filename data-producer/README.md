# Generate test data

Generate data for the kafka streams to work with.


download the duckdb jdbc jar:
```bash
mvn dependency:get \
  -DgroupId=org.duckdb \
  -DartifactId=duckdb_jdbc \
  -Dversion=1.3.2.0
```