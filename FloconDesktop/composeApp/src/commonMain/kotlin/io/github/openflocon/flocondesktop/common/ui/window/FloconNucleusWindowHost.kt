package io.github.openflocon.flocondesktop.common.ui.window

import dev.nucleusframework.application.NucleusWindowHost

/**
 * Material-themed [NucleusWindowHost] for secondary windows (Deep Search, etc.).
 * Provided to [dev.nucleusframework.application.LocalNucleusWindowHost] from [io.github.openflocon.flocondesktop.app.AppScreen].
 */
expect fun floconNucleusWindowHost(): NucleusWindowHost
