package com.zippe.client.features.home

import android.content.Context
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.zippe.client.core.result.Result
import com.zippe.client.core.result.wrapAsResult
import com.zippe.client.core.util.isActive
import com.zippe.client.datastore.RouteManager
import com.zippe.client.datastore.UserManager
import com.zippe.client.domain.passengers.FetchPassengerUseCase
import com.zippe.client.entities.bookings.Booking
import com.zippe.client.entities.bookings.BookingInfo
import com.zippe.client.entities.bookings.BookingStatus
import com.zippe.client.entities.general.Coordinates
import com.zippe.client.entities.locations.GooglePlace
import com.zippe.client.repository.BookingsRepository
import com.zippe.client.repository.LocationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val locationsRepository: LocationsRepository,
    private val bookingsRepository: BookingsRepository,
    private val fetchPassengerUseCase: FetchPassengerUseCase,
    userManager: UserManager,
    routeManager: RouteManager
) : ViewModel() {

    val locationManager = LocationManager(context, viewModelScope)

    private var getUserAutomaticPlaceJob: Job? = null
    private var getUserMapSelectedPlaceJob: Job? = null
    private var locationJob: Job? = null
    private var updateBookingJob: Job? = null

    val currency = userManager.getUser().map { it.currency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    private val _dragPlaceResponse = MutableStateFlow<Result<GooglePlace>>(Result.Initial)
    var dragPlaceResponse = _dragPlaceResponse.asStateFlow()

    private val _pickUpPlaceResponse = MutableStateFlow<Result<GooglePlace>>(Result.Initial)
    var pickUpPlaceResponse = _pickUpPlaceResponse.asStateFlow()

    private val _dropOffPlaceResponse = MutableStateFlow<Result<GooglePlace>>(Result.Initial)
    var dropOffPlaceResponse = _dropOffPlaceResponse.asStateFlow()

    private var searchPlacesJob: Job? = null
    private val _searchPlacesResponse = MutableStateFlow<Result<List<GooglePlace>>>(Result.Initial)
    val searchPlacesResponse = _searchPlacesResponse.asStateFlow()

    private var currentBookingJob: Job? = null
    private val _currentBookingResponse = MutableStateFlow<Result<Booking?>>(Result.Initial)
    val currentBookingResponse = _currentBookingResponse.asStateFlow()

    private var quoteBookingJob: Job? = null
    private val _quoteBookingResponse = MutableStateFlow<Result<BookingInfo?>>(Result.Initial)
    val quoteBookingResponse = _quoteBookingResponse.asStateFlow()

    private var createBookingJob: Job? = null
    private val _createBookingResponse = MutableStateFlow<Result<Booking?>>(Result.Initial)
    val createBookingResponse = _createBookingResponse.asStateFlow()

    private var cancelBookingJob: Job? = null
    private val _cancelBookingResponse = MutableStateFlow<Result<Unit>>(Result.Initial)
    val cancelBookingResponse = _cancelBookingResponse.asStateFlow()

    private var getBookingJob: Job? = null
    private val getBookingResponse = MutableStateFlow<Result<Booking>>(Result.Initial)

    var searchPlaceTextField by mutableStateOf(TextFieldValue())
        private set

    private val searchPlaceRequestFilter = snapshotFlow { searchPlaceTextField.text }
        .map { it.takeIf { it.length >= 2 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val route = routeManager.getRoute()
        .map {
            it.map { point ->
                LatLng(point.latitude, point.longitude)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    var userlocation by mutableStateOf<Location?>(null)
        private set

    var bookingInfo by mutableStateOf<BookingInfo?>(null)
        private set

    var currentBooking by mutableStateOf<Booking?>(null)
        private set

    private val _rideStatusUI = MutableStateFlow(RideStatusUI.NOT_REQUESTED)
    val rideStatusUI = _rideStatusUI.asStateFlow()

    private fun bookingStatusToRideUIStatus(
        bookingStatus: BookingStatus
    ) = when (bookingStatus) {
        BookingStatus.LOOKING_FOR_DRIVER -> RideStatusUI.LOOKING_FOR_DRIVER
        BookingStatus.NO_AVAILABLE_DRIVER_IN_AREA -> {
            cancelBookingUpdates()
            RideStatusUI.NO_AVAILABLE_DRIVER_IN_AREA
        }
        BookingStatus.DRIVER_ON_THE_WAY -> RideStatusUI.DRIVER_ON_THE_WAY
        BookingStatus.DRIVER_ARRIVED -> RideStatusUI.DRIVER_ARRIVED
        BookingStatus.ON_JOURNEY -> RideStatusUI.ON_JOURNEY
        BookingStatus.UNAVAILABLE_IN_AREA -> {
            cancelBookingUpdates()
            RideStatusUI.UNAVAILABLE_IN_AREA
        }
        BookingStatus.RIDE_COMPLETED -> {
            cancelBookingUpdates()
            RideStatusUI.RIDE_COMPLETED
        }
        BookingStatus.CANCELLED -> {
            onCancelBooking()
            RideStatusUI.NOT_REQUESTED
        }
    }

    init {
        viewModelScope.launch {
            fetchPassengerUseCase().collect()
        }
        locationJob = viewModelScope.launch {
            locationManager.location.filterNotNull().collect {
                if (_pickUpPlaceResponse.value is Result.Initial || _pickUpPlaceResponse.value is Result.Error) {
                    userlocation = it
                    getUserAutomaticPlaceJob?.cancel()
                    getUserAutomaticPlaceJob = getPlaceJob(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        onResult = { place ->
                            stopAutomaticStartDestination()
                            _pickUpPlaceResponse.value = place
                        }
                    )
                } else {
                    stopAutomaticStartDestination()
                }
            }
        }
        viewModelScope.launch {
            locationManager.lastKnownLocation.filterNotNull().collect {
                if (userlocation == null) {
                    userlocation = it
                }
            }
        }
        viewModelScope.launch {
            combine(
                pickUpPlaceResponse,
                dropOffPlaceResponse
            ) { pickUpPlaceResponse, dropOffPlaceResponse ->
                val pickUpPlace = pickUpPlaceResponse.dataOptional
                val dropOffPlace = dropOffPlaceResponse.dataOptional
                if (pickUpPlace != null && dropOffPlace != null) {
                    quoteBooking(
                        pickUpPlace = pickUpPlace,
                        dropOffPlace = dropOffPlace
                    )
                }
            }.collect()
        }
        viewModelScope.launch {
            quoteBookingResponse.collect {
                if (it is Result.Success) {
                    bookingInfo = it.data
                    _rideStatusUI.value = if (it.data == null) {
                        RideStatusUI.UNAVAILABLE_IN_AREA
                    } else {
                        RideStatusUI.BOOK_NOW
                    }
                    resetQuoteBookingResponse()
                }
            }
        }
        viewModelScope.launch {
            currentBookingResponse.collect {
                if (it is Result.Success && it.data != null) {
                    resetDropOffPlace()
                    val booking = it.data!!
                    _rideStatusUI.value = bookingStatusToRideUIStatus(booking.status)
                    currentBooking = booking
                    updateBooking(id = booking.id)
                    resetCurrentBookingResponse()
                }
            }
        }
        viewModelScope.launch {
            createBookingResponse.collect {
                if (it is Result.Success) {
                    resetDropOffPlace()
                    val booking = it.data
                    if (booking != null) {
                        _rideStatusUI.value = bookingStatusToRideUIStatus(booking.status)
                        currentBooking = booking
                        updateBooking(id = booking.id)
                    } else {
                        _rideStatusUI.value = RideStatusUI.NO_AVAILABLE_DRIVER_IN_AREA
                    }
                    resetCreateBookingResponse()
                }
            }
        }
        viewModelScope.launch {
            getBookingResponse.collect {
                if (it is Result.Success) {
                    _rideStatusUI.value = bookingStatusToRideUIStatus(it.data.status)
                    currentBooking = it.data
                }
            }
        }
        viewModelScope.launch {
            searchPlaceRequestFilter.collect { search ->
                if (search != null) {
                    searchPlace(search)
                } else {
                    resetSearchPlaceResponse()
                }
            }
        }
        viewModelScope.launch {
            cancelBookingResponse.collect {
                if (it is Result.Success) {
                    onCancelBooking()
                }
            }
        }
        getCurrentBooking()
    }

    fun toInitialRideState() {
        _rideStatusUI.value = RideStatusUI.NOT_REQUESTED
    }

    private fun onCancelBooking() {
        updateBookingJob?.cancel()
        resetBooking()
        resetBookingInfo()
        resetGetBookingResponse()
        resetCancelBookingResponse()
        _rideStatusUI.value = RideStatusUI.NOT_REQUESTED
    }

    private fun updateBooking(id: String) {
        if (updateBookingJob.isActive) return
        updateBookingJob = viewModelScope.launch {
            while (_rideStatusUI.value !in listOf(
                    RideStatusUI.RIDE_COMPLETED,
                    RideStatusUI.NOT_REQUESTED
                )
            ) {
                Timber.d("updateBooking ${_rideStatusUI.value}")
                delay(30.seconds)
                if (!getBookingJob.isActive) {
                    getBooking(id = id)
                }
            }
        }
    }

    fun updateSearchText(textFieldValue: TextFieldValue) {
        searchPlaceTextField = textFieldValue
    }

    private fun getPlaceJob(
        latitude: Double,
        longitude: Double,
        onResult: (Result<GooglePlace>) -> Unit = {},
    ): Job {
        return viewModelScope.launch {
            locationsRepository.getAddress(Pair(latitude, longitude))
                .wrapAsResult()
                .collect {
                    onResult(it)
                }
        }
    }

    private fun searchPlace(search: String) {
        searchPlacesJob?.cancel()
        searchPlacesJob = viewModelScope.launch {
            locationsRepository.search(
                search = search
            )
                .wrapAsResult()
                .collect {
                    _searchPlacesResponse.value = it
                }
        }
    }

    fun resetSearchPlaceResponse() {
        _searchPlacesResponse.value = Result.Initial
    }

    private fun getCurrentBooking() {
        currentBookingJob?.cancel()
        currentBookingJob = viewModelScope.launch {
            bookingsRepository.getCurrentBooking()
                .wrapAsResult()
                .collect {
                    _currentBookingResponse.value = it
                }
        }
    }

    fun resetCurrentBookingResponse() {
        _currentBookingResponse.value = Result.Initial
    }

    private fun quoteBooking(
        pickUpPlace: GooglePlace,
        dropOffPlace: GooglePlace
    ) {
        quoteBookingJob?.cancel()
        quoteBookingJob = viewModelScope.launch {
            bookingsRepository.quoteBooking(
                pickupLocation = Coordinates(
                    latitude = pickUpPlace.latitude,
                    longitude = pickUpPlace.longitude,
                ),
                dropOffLocation = Coordinates(
                    latitude = dropOffPlace.latitude,
                    longitude = dropOffPlace.longitude,
                ),
            )
                .wrapAsResult()
                .collect {
                    _quoteBookingResponse.value = it
                }
        }
    }

    fun resetQuoteBookingResponse() {
        _quoteBookingResponse.value = Result.Initial
    }

    fun createBooking() {
        createBookingJob?.cancel()
        createBookingJob = viewModelScope.launch {
            val pickUpPlace = pickUpPlaceResponse.value.dataOptional!!
            val dropOffPlace = dropOffPlaceResponse.value.dataOptional!!
            bookingsRepository.createBooking(
                pickupLocation = Coordinates(
                    latitude = pickUpPlace.latitude,
                    longitude = pickUpPlace.longitude,
                ),
                dropOffLocation = Coordinates(
                    latitude = dropOffPlace.latitude,
                    longitude = dropOffPlace.longitude,
                ),
            )
                .wrapAsResult()
                .collect {
                    _createBookingResponse.value = it
                }
        }
    }

    fun resetCreateBookingResponse() {
        _createBookingResponse.value = Result.Initial
    }

    private fun getBooking(id: String) {
        if (getBookingJob.isActive) {
            getBookingJob?.cancel()
        }
        getBookingJob = viewModelScope.launch {
            bookingsRepository.getBooking(id = id)
                .wrapAsResult()
                .collect {
                    getBookingResponse.value = it
                }
        }
    }

    fun cancelBookingUpdates() {
        updateBookingJob?.cancel()
    }

    fun resetGetBookingResponse() {
        getBookingResponse.value = Result.Initial
    }

    fun cancelBooking() {
        cancelBookingJob?.cancel()
        cancelBookingJob = viewModelScope.launch {
            val id = currentBooking?.id ?: return@launch
            bookingsRepository.cancelBooking(id = id)
                .wrapAsResult()
                .collect {
                    _cancelBookingResponse.value = it
                }
        }
    }

    fun resetCancelBookingResponse() {
        _cancelBookingResponse.value = Result.Initial
    }

    fun onPlaceSet(googlePlace: GooglePlace, isPickUpPlace: Boolean) {
        if (isPickUpPlace) {
            stopAutomaticStartDestination()
            _pickUpPlaceResponse.value = Result.Success(googlePlace)
        } else {
            _dropOffPlaceResponse.value = Result.Success(googlePlace)
        }
    }

    fun onUserMapPlaceDrag(
        latitude: Double,
        longitude: Double,
    ) {
        getUserMapSelectedPlaceJob?.cancel()
        getUserMapSelectedPlaceJob = getPlaceJob(
            latitude = latitude,
            longitude = longitude,
            onResult = { place ->
                _dragPlaceResponse.value = place
            }
        )
    }

    fun onUserMapPlaceSet(
        isPickUpPlace: Boolean,
    ) {
        val place = _dragPlaceResponse.value.dataOptional ?: return
        if (isPickUpPlace) {
            stopAutomaticStartDestination()
            _pickUpPlaceResponse.value = Result.Success(place)
        } else {
            _dropOffPlaceResponse.value = Result.Success(place)
        }
    }

    fun onSelectingPlaceOnMap() {
        _rideStatusUI.value = RideStatusUI.SELECT_A_POINT
    }

    fun resetDragPlace() {
        _dragPlaceResponse.value = Result.Initial
    }

    fun resetPickUpPlace() {
        _pickUpPlaceResponse.value = Result.Initial
    }

    fun resetDropOffPlace() {
        _dropOffPlaceResponse.value = Result.Initial
    }

    fun resetBookingInfo() {
        bookingInfo = null
    }

    fun resetBooking() {
        currentBooking = null
    }

    /**
     * a user selected his start location manually so we need to stop automatic location of his location
     */
    fun startAutomaticStartDestination() {
        userlocation = null
        locationManager.start(withLastLocation = false)
    }

    /**
     * a user selected his start location manually so we need to stop automatic location of his location
     */
    private fun stopAutomaticStartDestination() {
        locationManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        locationManager.stop()
    }
}