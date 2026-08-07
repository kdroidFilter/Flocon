package io.github.openflocon.domain.network.usecase

import io.github.openflocon.domain.common.Either
import io.github.openflocon.domain.common.Failure
import io.github.openflocon.domain.common.Success
import io.github.openflocon.domain.common.files.FilePicker
import io.github.openflocon.domain.common.then
import io.github.openflocon.domain.device.usecase.GetCurrentDeviceIdAndPackageNameUseCase
import io.github.openflocon.domain.network.repository.NetworkCsvRepository
import io.github.openflocon.domain.network.repository.NetworkRepository

class ImportNetworkCallsFromCsvUseCase(
    private val getCurrentDeviceIdAndPackageNameUseCase: GetCurrentDeviceIdAndPackageNameUseCase,
    private val networkRepository: NetworkRepository,
    private val networkCsvRepository: NetworkCsvRepository,
) {
    suspend operator fun invoke(): Either<Throwable, Unit> {
        val current = getCurrentDeviceIdAndPackageNameUseCase()
            ?: return Failure(Throwable("no current device"))
        return try {
            val file = FilePicker.pickOpenFile(
                title = "Import network calls from CSV",
                extensions = listOf("csv"),
            )
            if (file == null) {
                Failure(Throwable("no file selected"))
            } else {
                networkCsvRepository.importCallsFromCsv(file = file, current.appInstance)
                    .then {
                        networkRepository.addCalls(
                            deviceIdAndPackageName = current,
                            calls = it
                        )
                        Success(Unit)
                    }
            }
        } catch (t: Throwable) {
            Failure(t)
        }
    }
}
