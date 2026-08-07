package io.github.openflocon.flocondesktop.features.database.processor

import io.github.openflocon.domain.common.Either
import io.github.openflocon.domain.common.Failure
import io.github.openflocon.domain.common.Success
import io.github.openflocon.domain.common.files.FilePicker
import io.github.openflocon.domain.database.models.DatabaseQueryLogDomainModel
import io.github.openflocon.domain.database.models.toFullSql
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportDatabaseQueryLogsToMarkdownProcessor {

    suspend operator fun invoke(
        logs: List<DatabaseQueryLogDomainModel>
    ): Either<Throwable, String> {
        val fileName = "database_logs_${System.currentTimeMillis()}.md"

        val file = FilePicker.pickSaveFile(
            title = "Export database logs as Markdown",
            defaultFileName = fileName,
        ) ?: return Failure(Throwable("no file selected"))

        exportToMarkdown(
            file = file,
            logs = logs,
        )
        return Success(file.absolutePath)
    }

    private fun exportToMarkdown(
        file: File,
        logs: List<DatabaseQueryLogDomainModel>,
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val markdown = buildString {
            logs.forEach { log ->
                val date = dateFormat.format(Date(log.timestamp))
                val sql = log.sqlQuery.replace("|", "\\|").replace("\n", " ")
                val args = (log.bindArgs?.toString() ?: "[]").replace("|", "\\|")
                val fullSql = log.toFullSql().replace("|", "\\|").replace("\n", " ")

                appendLine("# $date")
                appendLine()
                appendLine("### query")
                appendLine(sql)
                appendLine()
                appendLine("### args")
                appendLine(args)
                appendLine()
                appendLine("### SQL")
                appendLine(fullSql)
                appendLine()
                appendLine("--------------------------------------------------------------------------------")
                appendLine()
            }
        }

        file.writeText(markdown)
    }
}
