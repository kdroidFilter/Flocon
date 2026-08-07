package io.github.openflocon.flocondesktop.features.network.mock.processor

import co.touchlab.kermit.Logger
import io.github.openflocon.domain.common.files.FilePicker
import io.github.openflocon.domain.network.models.MockNetworkDomainModel
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException

sealed interface ImportResult {
    data class Success(val mocks: List<MockNetworkDomainModel>) : ImportResult
    object Cancelled : ImportResult
    data class Failure(val error: Throwable) : ImportResult
}

class ImportMocksProcessor {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    suspend operator fun invoke(): ImportResult {
        val selectedFile = FilePicker.pickOpenFile(
            title = "Importer les Mocks JSON",
            extensions = listOf("json"),
        )

        if (selectedFile == null) {
            Logger.d("Importing cancelled")
            return ImportResult.Cancelled
        }

        val jsonString = try {
            selectedFile.readText()
        } catch (e: FileNotFoundException) {
            Logger.e("File not found during import", e)
            return ImportResult.Failure(e)
        } catch (e: Exception) {
            Logger.e("Error reading file during import", e)
            return ImportResult.Failure(e)
        }

        val domainMocks = try {
            val exportedMocks: List<MockNetworkExportedModel> = json.decodeFromString(jsonString)

            exportedMocks.map { it.toDomainModel() }
        } catch (e: Exception) {
            Logger.e("Error deserializing or mapping mocks", e)
            return ImportResult.Failure(e)
        }

        return ImportResult.Success(domainMocks)
    }
}
