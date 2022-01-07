# Indexing SQLite table data

Note: 
- multiple SQLite db files are loaded concurrently using Kotlin coroutines
- a single `org.apache.lucene.index.IndexWriter` is used to index data
