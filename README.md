# Indexing SQLite table data

This is a proof of concept project.

Multiple SQLite db files are loaded concurrently using Kotlin coroutines. Each db file is mapped to an asynchronous `kotlinx.coroutines.flow.Flow`, which runs in a shared background pool of threads using the `kotlinx.coroutines.flow.flowOn` operator. A single `org.apache.lucene.index.IndexWriter` is used to index data. It runs on the main thread using the `kotlinx.coroutines.runBlocking` coroutine builder.
