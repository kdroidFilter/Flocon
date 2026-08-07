package io.github.openflocon.flocondesktop.features.database.processor

import io.github.openflocon.domain.common.files.FilePicker

class ImportSqlQueryProcessor {

    suspend operator fun invoke(): String? = FilePicker.pickOpenFile(
        title = "Import Sql Query",
        extensions = listOf("sql", "txt"),
    )?.readText()
}
