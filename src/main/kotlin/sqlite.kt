import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

typealias Record = Map<String, String>

fun recordFlow(dbFile: String): Flow<Record> = flow {
    val sqliteUrl = "jdbc:sqlite:$dbFile"

    val datasetName = dbFile.subSequence(
        dbFile.lastIndexOf("/") + 1,
        dbFile.lastIndexOf(".")
    ).toString()

    DriverManager.getConnection(sqliteUrl).use { conn ->
        val stmt = conn.createStatement()

        log("${datasetName}: emitting columns...")
        val (colNames, colLabels) = getColumns(stmt, datasetName)
        emit(colLabels)

        val rs = stmt.executeQuery("SELECT rowid as id, * FROM data")
        while (rs.next()) {
            log("${datasetName}: emitting row...")
            val row = getRow(rs, colNames, datasetName)
            emit(row)
        }
    }
}.flowOn(Dispatchers.Default)

private fun getColumns(stmt: Statement, datasetName: String): Pair<List<String>, Record> {
    val columns = mutableListOf<String>()
    val row = mutableMapOf<String, String>()

    val rs: ResultSet = stmt.executeQuery("SELECT attr, label FROM label")
    while (rs.next()) {
        columns.add(rs.getString("attr"))
        row[rs.getString("attr")] = rs.getString("label")
    }

    columns.add("id")
    row["datasetname"] = datasetName

    return columns.toList() to row.toMap()
}

private fun getRow(rs: ResultSet, colNames: List<String>, datasetName: String): Record {
    val row = mutableMapOf<String, String>()
    colNames.forEach {
        if (rs.getString(it) != null) {
            row[it] = rs.getString(it)
        }
    }
    row["datasetname"] = datasetName

    return row.toMap()
}