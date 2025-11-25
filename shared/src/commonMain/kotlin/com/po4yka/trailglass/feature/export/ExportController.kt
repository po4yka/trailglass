package com.po4yka.trailglass.feature.export

import com.po4yka.trailglass.domain.error.Result
import com.po4yka.trailglass.feature.common.Lifecycle
import com.po4yka.trailglass.logging.logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

/**
 * Controller for managing export operations. Handles exporting data to various formats with progress tracking.
 *
 * IMPORTANT: Call [cleanup] when this controller is no longer needed to prevent memory leaks.
 */
@Inject
class ExportController(
    private val exportDataUseCase: ExportDataUseCase,
    coroutineScope: CoroutineScope
) : Lifecycle {
    private val logger = logger()

    // Create a child scope that can be cancelled independently
    private val controllerScope =
        CoroutineScope(
            coroutineScope.coroutineContext + SupervisorJob()
        )

    /** Export state. */
    data class ExportState(
        val isExporting: Boolean = false,
        val progress: Float = 0f,
        val currentOperation: String? = null,
        val lastExportPath: String? = null,
        val error: String? = null,
        val showDialog: Boolean = false,
        val selectedFormat: ExportDataUseCase.Format = ExportDataUseCase.Format.CSV,
        val selectedDataType: DataTypeSelection = DataTypeSelection.AllTrips
    )

    /** Data type selection for UI. */
    sealed class DataTypeSelection {
        object AllTrips : DataTypeSelection()

        data class SingleTrip(
            val tripId: String,
            val tripName: String
        ) : DataTypeSelection()

        data class TripVisits(
            val tripId: String,
            val tripName: String
        ) : DataTypeSelection()

        data class DateRange(
            val startTime: Instant,
            val endTime: Instant
        ) : DataTypeSelection()
    }

    private val _state = MutableStateFlow(ExportState())
    val state: StateFlow<ExportState> = _state.asStateFlow()

    /** Show export dialog. */
    fun showDialog() {
        logger.debug { "Showing export dialog" }
        _state.update { it.copy(showDialog = true) }
    }

    /** Hide export dialog. */
    fun dismissDialog() {
        logger.debug { "Dismissing export dialog" }
        _state.update {
            it.copy(
                showDialog = false,
                error = null
            )
        }
    }

    /** Set selected export format. */
    fun setFormat(format: ExportDataUseCase.Format) {
        logger.debug { "Setting export format to $format" }
        _state.update { it.copy(selectedFormat = format) }
    }

    /** Set selected data type. */
    fun setDataType(dataType: DataTypeSelection) {
        logger.debug { "Setting data type to $dataType" }
        _state.update { it.copy(selectedDataType = dataType) }
    }

    /**
     * Export data with the current settings.
     *
     * @param userId Current user ID (required for some data types)
     * @param outputPath Absolute path where the file should be saved
     */
    fun exportData(
        userId: String,
        outputPath: String
    ) {
        logger.info { "Starting export: format=${_state.value.selectedFormat}, path=$outputPath" }

        _state.update {
            it.copy(
                isExporting = true,
                progress = 0f,
                currentOperation = "Preparing export...",
                error = null
            )
        }

        controllerScope.launch {
            try {
                // Convert UI data type to use case data type
                val dataType =
                    when (val selection = _state.value.selectedDataType) {
                        is DataTypeSelection.AllTrips -> ExportDataUseCase.DataType.AllTrips
                        is DataTypeSelection.SingleTrip -> ExportDataUseCase.DataType.SingleTrip(selection.tripId)
                        is DataTypeSelection.TripVisits -> ExportDataUseCase.DataType.TripVisits(selection.tripId)
                        is DataTypeSelection.DateRange ->
                            ExportDataUseCase.DataType.DateRangeVisits(
                                userId,
                                selection.startTime,
                                selection.endTime
                            )
                    }

                _state.update { it.copy(progress = 0.3f, currentOperation = "Gathering data...") }

                // Execute export
                val result =
                    exportDataUseCase.execute(
                        dataType = dataType,
                        format = _state.value.selectedFormat,
                        outputPath = outputPath,
                        userId = userId
                    )

                _state.update { it.copy(progress = 0.9f, currentOperation = "Finalizing...") }

                when (result) {
                    is Result.Success -> {
                        logger.info { "Export completed successfully: $outputPath" }
                        _state.update {
                            it.copy(
                                isExporting = false,
                                progress = 1f,
                                currentOperation = "Export complete",
                                lastExportPath = outputPath,
                                showDialog = false
                            )
                        }
                    }
                    is Result.Error -> {
                        val error = result.error
                        logger.error { "Export failed: ${error.getTechnicalDetails()}" }
                        _state.update {
                            it.copy(
                                isExporting = false,
                                progress = 0f,
                                currentOperation = null,
                                error = error.getUserFriendlyMessage()
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error(e) { "Unexpected error during export" }
                _state.update {
                    it.copy(
                        isExporting = false,
                        progress = 0f,
                        currentOperation = null,
                        error = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }

    /** Clear error state. */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /** Reset export state. */
    fun reset() {
        logger.debug { "Resetting export state" }
        _state.update {
            ExportState(
                selectedFormat = it.selectedFormat,
                selectedDataType = it.selectedDataType
            )
        }
    }

    /**
     * Cleanup method to release resources and prevent memory leaks. MUST be called when this controller is no longer
     * needed.
     */
    override fun cleanup() {
        logger.info { "Cleaning up ExportController" }
        controllerScope.cancel()
        logger.debug { "ExportController cleanup complete" }
    }
}
