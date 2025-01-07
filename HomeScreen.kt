package com.zippe.client.features.home

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.view.View
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.zippe.client.common_ui.components.AppAlertDialog
import com.zippe.client.common_ui.components.AppAlertDialogNetworkIssue
import com.zippe.client.common_ui.components.AppBottomSheet
import com.zippe.client.common_ui.components.AppDivider
import com.zippe.client.common_ui.components.AppFilledButton
import com.zippe.client.common_ui.components.AppOutlinedButton
import com.zippe.client.common_ui.theme.AppColor
import com.zippe.client.common_ui.theme.AppTheme
import com.zippe.client.common_ui.theme.AppTypography
import com.zippe.client.common_ui.utils.Formats
import com.zippe.client.common_ui.utils.IntentUtils
import com.zippe.client.common_ui.utils.getActivity
import com.zippe.client.core.errors.NetworkException
import com.zippe.client.core.errors.UnauthorizedException
import com.zippe.client.core.result.Result
import com.zippe.client.core.result.loading
import com.zippe.client.core.util.PermissionsUtil
import com.zippe.client.entities.bookings.Booking
import com.zippe.client.entities.bookings.BookingInfo
import com.zippe.client.entities.bookings.BookingStatus
import com.zippe.client.entities.bookings.Vehicle
import com.zippe.client.entities.general.Coordinates
import com.zippe.client.entities.locations.GooglePlace
import com.zippe.client.features.BuildConfig
import com.zippe.client.features.R
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.math.roundToInt


@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    openDrawer: () -> Unit,
) {
    val context = LocalContext.current

    var errorForDialog by rememberSaveable { mutableStateOf<Throwable?>(null) }

    val dragPlaceResponse by homeViewModel.dragPlaceResponse.collectAsStateWithLifecycle()
    val pickUpPlaceResponse by homeViewModel.pickUpPlaceResponse.collectAsStateWithLifecycle()
    val dropOffPlaceResponse by homeViewModel.dropOffPlaceResponse.collectAsStateWithLifecycle()
    val searchPlacesResponse by homeViewModel.searchPlacesResponse.collectAsStateWithLifecycle()
    val quoteBookingResponse by homeViewModel.quoteBookingResponse.collectAsStateWithLifecycle()
    val cancelBookingResponse by homeViewModel.cancelBookingResponse.collectAsStateWithLifecycle()
    val createBookingResponse by homeViewModel.createBookingResponse.collectAsStateWithLifecycle()
    val currentBookingResponse by homeViewModel.currentBookingResponse.collectAsStateWithLifecycle()
    val route by homeViewModel.route.collectAsStateWithLifecycle()
    val currency by homeViewModel.currency.collectAsStateWithLifecycle()

    val rideStatus by homeViewModel.rideStatusUI.collectAsStateWithLifecycle()

    val toInitialState = { resetPickUpLocation: Boolean ->
        homeViewModel.cancelBookingUpdates()
        homeViewModel.resetGetBookingResponse()
        homeViewModel.resetDropOffPlace()
        homeViewModel.resetBookingInfo()
        homeViewModel.resetBooking()
        homeViewModel.toInitialRideState()
        if (resetPickUpLocation) {
            homeViewModel.resetPickUpPlace()
            homeViewModel.startAutomaticStartDestination()
        }
    }

    BackHandler(
        rideStatus in listOf(
            RideStatusUI.SELECT_A_POINT,
            RideStatusUI.BOOK_NOW,
            RideStatusUI.NO_AVAILABLE_DRIVER_IN_AREA,
            RideStatusUI.UNAVAILABLE_IN_AREA
        )
    ) {
        toInitialState(rideStatus != RideStatusUI.SELECT_A_POINT)
    }

    // TODO: to UI state
    LaunchedEffect(key1 = searchPlacesResponse) {
        val result = searchPlacesResponse
        if (result is Result.Error) {
            errorForDialog = result.exception
            homeViewModel.resetSearchPlaceResponse()
        }
    }
    LaunchedEffect(key1 = dragPlaceResponse) {
        val result = dragPlaceResponse
        if (result is Result.Error) {
            errorForDialog = result.exception
            homeViewModel.resetDragPlace()
        }
    }
    LaunchedEffect(key1 = pickUpPlaceResponse) {
        val result = pickUpPlaceResponse
        if (result is Result.Error) {
            errorForDialog = result.exception
            homeViewModel.resetPickUpPlace()
        }
    }
    LaunchedEffect(key1 = dropOffPlaceResponse) {
        val result = dropOffPlaceResponse
        if (result is Result.Error) {
            errorForDialog = result.exception
            homeViewModel.resetDropOffPlace()
        }
    }
    LaunchedEffect(key1 = quoteBookingResponse) {
        val result = quoteBookingResponse
        if (result is Result.Error) {
            errorForDialog = result.exception
            homeViewModel.resetQuoteBookingResponse()
            homeViewModel.resetDropOffPlace()
        }
    }
    LaunchedEffect(key1 = cancelBookingResponse) {
        val result = cancelBookingResponse
        if (result is Result.Error) {
            errorForDialog = result.exception
            homeViewModel.resetCancelBookingResponse()
        }
    }
    LaunchedEffect(key1 = createBookingResponse) {
        val result = createBookingResponse
        if (result is Result.Error) {
            errorForDialog = result.exception
            homeViewModel.resetCreateBookingResponse()
        }
    }
    LaunchedEffect(key1 = currentBookingResponse) {
        val result = currentBookingResponse
        if (result is Result.Error) {
            errorForDialog = result.exception
            homeViewModel.resetCurrentBookingResponse()
        }
    }

    var showRideCancelConfirmationDlg by rememberSaveable { mutableStateOf(false) }

    var isLocationPermissionGranted by rememberSaveable {
        mutableStateOf(PermissionsUtil.isLocationPermissionGranted(context))
    }

    LocationPermissionsHandler(
        locationManager = homeViewModel.locationManager,
        onPermissionGranted = {
            isLocationPermissionGranted = true
        }
    )

    var isPickUpPlace by rememberSaveable { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val sheetStateEnterAddress = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val sheetStateSelectAddressMethod = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )
    val sheetStateUnavailableInArea = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val focusManager = LocalFocusManager.current
    DisposableEffect(key1 = sheetStateEnterAddress.isVisible, effect = {
        onDispose {
            focusManager.clearFocus()
        }
    })
    AppBottomSheet(
        sheetState = sheetStateUnavailableInArea,
        coroutineScope = coroutineScope,
        shapeCornerRadius = 26.dp,
        sheetContent = {
            SheetContentUnavailableInArea()
        },
    ) {
        AppBottomSheet(
            sheetState = sheetStateEnterAddress,
            coroutineScope = coroutineScope,
            shapeCornerRadius = 26.dp,
            sheetContent = {
                SheetContentEnterAddress(
                    searchPlaceTextField = homeViewModel.searchPlaceTextField,
                    onSearchPlaceTextUpdate = {
                        homeViewModel.updateSearchText(it)
                    },
                    isPickUpPlace = isPickUpPlace,
                    googlePlacesResponse = searchPlacesResponse,
                    onSelect = { place ->
                        homeViewModel.onPlaceSet(place, isPickUpPlace)
                        coroutineScope.launch {
                            sheetStateEnterAddress.hide()
                        }
                        homeViewModel.updateSearchText(TextFieldValue())
                        homeViewModel.resetSearchPlaceResponse()
                    },
                )
            }
        ) {
            AppBottomSheet(
                sheetState = sheetStateSelectAddressMethod,
                coroutineScope = coroutineScope,
                sheetContent = {
                    SheetContentSelectAddressMethod(
                        onHide = {
                            coroutineScope.launch {
                                sheetStateSelectAddressMethod.hide()
                            }
                        },
                        onAddressEnter = {
                            homeViewModel.updateSearchText(TextFieldValue())
                            homeViewModel.resetSearchPlaceResponse()
                            coroutineScope.launch {
                                sheetStateEnterAddress.show()
                            }
                        },
                        onAddressSelect = {
                            homeViewModel.onSelectingPlaceOnMap()
                        }
                    )
                }
            ) {
                HomeContent(
                    openDrawer = openDrawer,
                    isLocationPermissionGranted = isLocationPermissionGranted,
                    userLocation = homeViewModel.userlocation,
                    dragPlaceResponse = dragPlaceResponse,
                    pickUpPlace = pickUpPlaceResponse.dataOptional,
                    dropOffPlace = dropOffPlaceResponse.dataOptional,
                    onPlaceSelect = { pickUpPlace ->
                        isPickUpPlace = pickUpPlace
                        coroutineScope.launch {
                            sheetStateSelectAddressMethod.show()
                        }
                    },
                    rideStatusUI = rideStatus,
                    toInitialState = toInitialState,
                    onRideCancel = {
                        showRideCancelConfirmationDlg = true
                    },
                    onRideBook = {
                        homeViewModel.createBooking()
                    },
                    onRideDone = {
                        toInitialState(true)
                    },
                    onShowUnavailableInAreaSheet = {
                        coroutineScope.launch {
                            sheetStateUnavailableInArea.show()
                        }
                    },
                    isPickUpPlace = isPickUpPlace,
                    route = route,
                    onDragLocationUpdate = {
                        homeViewModel.onUserMapPlaceDrag(
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                    },
                    onSelectPlaceOnTheMapConfirm = {
                        homeViewModel.toInitialRideState()
                        homeViewModel.onUserMapPlaceSet(isPickUpPlace = isPickUpPlace)
                    },
                    currency = currency,
                    booking = homeViewModel.currentBooking,
                    bookingInfo = homeViewModel.bookingInfo,
                    creatingBooking = createBookingResponse.loading
                )
            }
        }

        errorForDialog?.let { exception ->
            if (exception !is UnauthorizedException) {
                val closeDialog = { errorForDialog = null }
                if (exception is NetworkException) {
                    AppAlertDialogNetworkIssue(onDismiss = closeDialog)
                } else {
                    AppAlertDialog(
                        title = stringResource(id = R.string.error),
                        subtitle = exception.message ?: exception.toString(),
                        onDismiss = closeDialog,
                        onPositiveBtnClicked = closeDialog
                    )
                }
            } else {
                // ignore 401 HTTP code errors
                errorForDialog = null
            }
        }

        if (showRideCancelConfirmationDlg) {
            val closeDialog = { showRideCancelConfirmationDlg = false }
            AppAlertDialog(
                title = stringResource(R.string.home_booking_cancel_confirmation_dlg_title),
                onDismiss = closeDialog,
                positiveBtnText = stringResource(R.string.home_booking_cancel_confirmation_dlg_btn_keep),
                negativeBtnText = stringResource(R.string.home_booking_cancel_confirmation_dlg_btn_cancel),
                onPositiveBtnClicked = closeDialog,
                onNegativeBtnClicked = {
                    closeDialog()
                    homeViewModel.cancelBooking()
                }
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun HomeContent(
    openDrawer: () -> Unit,
    isLocationPermissionGranted: Boolean,
    userLocation: Location?,
    currency: String,
    bookingInfo: BookingInfo?,
    booking: Booking?,
    creatingBooking: Boolean,
    dragPlaceResponse: Result<GooglePlace>,
    pickUpPlace: GooglePlace?,
    dropOffPlace: GooglePlace?,
    onPlaceSelect: (pickUpPlace: Boolean) -> Unit,
    rideStatusUI: RideStatusUI,
    toInitialState: (resetPickUpLocation: Boolean) -> Unit,
    onRideCancel: () -> Unit,
    onRideBook: () -> Unit,
    onRideDone: () -> Unit,
    onShowUnavailableInAreaSheet: () -> Unit,
    isPickUpPlace: Boolean,
    route: List<LatLng>,
    onDragLocationUpdate: (LatLng) -> Unit,
    onSelectPlaceOnTheMapConfirm: () -> Unit,
) {
    var topBarHeight by remember { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            HomeTopBar(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    topBarHeight = coordinates.size.height
                },
                rideStatusUI = rideStatusUI,
                openDrawer = openDrawer,
                toInitialState = toInitialState,
                onRideCancel = onRideCancel
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            var cardsHeight by remember { mutableIntStateOf(0) }
            var cameraPositionMoving by remember { mutableStateOf(false) }
            Map(
                topBarHeight = topBarHeight,
                booking = booking,
                bookingInfo = bookingInfo,
                pickUpPlace = pickUpPlace,
                isPickUpPlace = isPickUpPlace,
                cardsHeight = cardsHeight,
                userLocation = userLocation,
                isLocationPermissionGranted = isLocationPermissionGranted,
                rideStatusUI = rideStatusUI,
                route = route,
                onDragLocationUpdate = onDragLocationUpdate,
                onCameraPositionMoving = {
                    cameraPositionMoving = it
                }
            )
            Box(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .onGloballyPositioned { coordinates ->
                    cardsHeight = coordinates.size.height
                }
            ) {
                if (rideStatusUI == RideStatusUI.SELECT_A_POINT) {
                    CardSelectPoint(
                        cameraPositionMoving = cameraPositionMoving,
                        dragPlaceResponse = dragPlaceResponse,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        isPickUpPlace = isPickUpPlace,
                        onSelectPlaceOnTheMapConfirm = onSelectPlaceOnTheMapConfirm
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    CardStatus(
                        rideStatusUI = rideStatusUI,
                        onShowUnavailableInAreaSheet = onShowUnavailableInAreaSheet,
                        vehicle = booking?.vehicle
                    )
                    when (rideStatusUI) {
                        RideStatusUI.NOT_REQUESTED,
                        RideStatusUI.BOOK_NOW,
                        RideStatusUI.UNAVAILABLE_IN_AREA -> {
                            CardAddresses(
                                pickUpPlace = pickUpPlace,
                                dropOffPlace = dropOffPlace,
                                onPlaceSelect = onPlaceSelect
                            )
                            if (rideStatusUI == RideStatusUI.BOOK_NOW) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        else -> {
                            // ignore
                        }
                    }
                    when (rideStatusUI) {
                        RideStatusUI.BOOK_NOW,
                        RideStatusUI.LOOKING_FOR_DRIVER,
                        RideStatusUI.NO_AVAILABLE_DRIVER_IN_AREA,
                        RideStatusUI.DRIVER_ON_THE_WAY,
                        RideStatusUI.DRIVER_ARRIVED,
                        RideStatusUI.ON_JOURNEY,
                        RideStatusUI.RIDE_COMPLETED -> {
                            CardCash(
                                currency = currency,
                                isBookingNow = rideStatusUI == RideStatusUI.BOOK_NOW,
                                booking = booking,
                                bookingInfo = bookingInfo,
                            )
                            if (rideStatusUI == RideStatusUI.BOOK_NOW
                                || rideStatusUI == RideStatusUI.RIDE_COMPLETED
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        else -> {
                            // ignore
                        }
                    }
                    if (rideStatusUI == RideStatusUI.RIDE_COMPLETED) {
                        AppOutlinedButton(
                            text = stringResource(R.string.done_text),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            onClick = onRideDone,
                            containerColor = AppColor.gray1
                        )
                    } else if (rideStatusUI == RideStatusUI.BOOK_NOW) {
                        AppFilledButton(
                            text = stringResource(R.string.home_book_now_btn_text),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            onClick = onRideBook,
                            trailingIcon = R.drawable.ic_arrow_right,
                            containerColor = AppColor.colors3,
                            progressLoading = creatingBooking
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (BuildConfig.DEBUG || BuildConfig.FLAVOR == "dev") {
                Text(
                    text = booking?.id ?: "",
                    modifier = Modifier.align(
                        Alignment.CenterStart
                    )
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    modifier: Modifier = Modifier,
    rideStatusUI: RideStatusUI,
    openDrawer: () -> Unit,
    toInitialState: (resetPickUpLocation: Boolean) -> Unit,
    onRideCancel: () -> Unit
) {
    val rideStatusUIUpdated = rememberUpdatedState(newValue = rideStatusUI)
    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = {},
        navigationIcon = {
            val icon = when (rideStatusUI) {
                RideStatusUI.NOT_REQUESTED -> R.drawable.ic_menu

                RideStatusUI.SELECT_A_POINT,
                RideStatusUI.BOOK_NOW,
                RideStatusUI.UNAVAILABLE_IN_AREA -> R.drawable.ic_arrow_left

                RideStatusUI.LOOKING_FOR_DRIVER,
                RideStatusUI.NO_AVAILABLE_DRIVER_IN_AREA,
                RideStatusUI.DRIVER_ON_THE_WAY,
                RideStatusUI.DRIVER_ARRIVED -> R.drawable.ic_close

                else -> null
            }
            icon?.let {
                Button(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .height(40.dp)
                        .widthIn(min = 40.dp),
                    onClick = {
                        when (rideStatusUIUpdated.value) {
                            RideStatusUI.NOT_REQUESTED -> {
                                openDrawer()
                            }

                            RideStatusUI.SELECT_A_POINT -> {
                                toInitialState(false)
                            }

                            RideStatusUI.BOOK_NOW,
                            RideStatusUI.NO_AVAILABLE_DRIVER_IN_AREA,
                            RideStatusUI.UNAVAILABLE_IN_AREA -> {
                                toInitialState(true)
                            }

                            RideStatusUI.LOOKING_FOR_DRIVER,
                            RideStatusUI.DRIVER_ON_THE_WAY,
                            RideStatusUI.DRIVER_ARRIVED -> {
                                onRideCancel()
                            }

                            else -> {
                                // ignore
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColor.blueGray14,
                        contentColor = AppColor.gray1
                    ),
                    contentPadding = PaddingValues(
                        vertical = 8.dp,
                        horizontal = if (rideStatusUI == RideStatusUI.BOOK_NOW) {
                            12.dp
                        } else {
                            8.dp
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = icon),
                        contentDescription = "Open side menu"
                    )
                    if (rideStatusUI in listOf(
                            RideStatusUI.BOOK_NOW,
                            RideStatusUI.UNAVAILABLE_IN_AREA
                        )
                    ) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.home_back_to_home_toolbar_btn_title),
                            style = AppTypography.androidButton
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
    )
}

@Composable
private fun CardStatus(
    vehicle: Vehicle?,
    rideStatusUI: RideStatusUI,
    onShowUnavailableInAreaSheet: () -> Unit
) {
    when (rideStatusUI) {
        RideStatusUI.LOOKING_FOR_DRIVER -> {
            CardStatus(
                text = stringResource(R.string.home_ride_status_looking_for_a_driver),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        RideStatusUI.NO_AVAILABLE_DRIVER_IN_AREA -> {
            CardStatus(
                text = stringResource(R.string.home_ride_status_no_available_driver_in_area),
                icon = R.drawable.ic_status_no_available_driver_in_area
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        RideStatusUI.UNAVAILABLE_IN_AREA -> {
            CardStatusNoAvailableInArea(
                onShowUnavailableInAreaSheet = onShowUnavailableInAreaSheet
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        RideStatusUI.DRIVER_ON_THE_WAY -> {
            CardStatus(
                text = stringResource(R.string.home_ride_status_driver_is_on_the_way),
                icon = R.drawable.ic_status_driver_on_the_way,
                vehicle = vehicle
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        RideStatusUI.DRIVER_ARRIVED -> {
            CardStatus(
                text = stringResource(R.string.home_ride_status_driver_has_arrived),
                icon = R.drawable.ic_status_driver_arrived,
                vehicle = vehicle
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        RideStatusUI.ON_JOURNEY -> {
            CardStatus(
                text = stringResource(R.string.home_ride_status_on_journey),
                icon = R.drawable.ic_status_on_journey,
                vehicle = vehicle
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        RideStatusUI.RIDE_COMPLETED -> {
            CardStatus(
                text = stringResource(R.string.home_ride_status_ride_completed),
                icon = R.drawable.ic_status_ride_completed,
                vehicle = vehicle
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        else -> {}
    }
}

@Composable
private fun AppCard(
    modifier: Modifier = Modifier,
    paddings: PaddingValues = PaddingValues(horizontal = 12.dp),
    containerColor: Color = AppColor.gray1,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.padding(paddings),
        border = BorderStroke(
            width = 1.dp,
            color = AppColor.blueGray6
        ),
        shape = RoundedCornerShape(size = 26.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        content = content
    )
}

@Composable
private fun CardStatus(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes icon: Int? = null,
    iconContentDescription: String? = null,
    vehicle: Vehicle? = null
) {
    AppCard(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColor.blueGray14)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            icon?.let {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = iconContentDescription,
                    tint = AppColor.blue2,
                )
            } ?: CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp),
                color = AppColor.white,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = AppColor.blue2,
                style = AppTypography.androidSubtitle2
            )
        }
        vehicle?.let { vehicle ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Image(
                    modifier = Modifier.size(80.dp, 64.dp),
                    painter = painterResource(id = R.drawable.ic_placeholder_car),
                    contentDescription = "Car photo",
                )
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(
                            id = R.string.ph_2_spaced,
                            vehicle.model,
                            vehicle.color
                        ),
                        color = AppColor.blueGray14,
                        style = AppTypography.androidSubtitle2
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = vehicle.plateNumber,
                        color = AppColor.blueGray14,
                        style = AppTypography.androidBody1
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person),
                            contentDescription = null,
                            tint = AppColor.blueGray9,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = vehicle.driverName,
                            color = AppColor.blueGray9,
                            style = AppTypography.androidBody2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardStatusNoAvailableInArea(
    modifier: Modifier = Modifier,
    onShowUnavailableInAreaSheet: () -> Unit
) {
    AppCard(
        modifier = modifier,
        containerColor = AppColor.blueGray14
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    onShowUnavailableInAreaSheet()
                }
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_status_unavailable_in_area),
                contentDescription = null,
                tint = AppColor.blue2,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.home_ride_status_unavailable_in_this_area),
                color = AppColor.blue2,
                style = AppTypography.androidSubtitle2
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_info),
                contentDescription = null,
                tint = AppColor.blueGray6,
            )
        }
    }
}


@Composable
private fun CardSelectPoint(
    cameraPositionMoving: Boolean,
    dragPlaceResponse: Result<GooglePlace>,
    modifier: Modifier = Modifier,
    isPickUpPlace: Boolean,
    onSelectPlaceOnTheMapConfirm: () -> Unit,
) {
    Card(
        modifier = modifier,
        border = BorderStroke(
            width = 1.dp,
            color = AppColor.blueGray6
        ),
        shape = RoundedCornerShape(size = 26.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColor.blue1
        )
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = if (isPickUpPlace) {
                stringResource(R.string.home_select_point_card_start_title)
            } else {
                stringResource(R.string.home_select_point_card_end_title)
            },
            color = AppColor.blue8,
            style = AppTypography.androidSubtitle2,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(R.string.home_select_point_drag_marker_desc),
            color = AppColor.blue6,
            style = AppTypography.androidCaption1,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    spotColor = Color(0x0A7F89AF),
                    ambientColor = Color(0x0A7F89AF)
                )
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(
                    color = AppColor.gray1,
                    shape = RoundedCornerShape(100)
                )
                .padding(horizontal = 8.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painterResource(
                    id = if (isPickUpPlace) {
                        R.drawable.ic_map_point
                    } else {
                        R.drawable.ic_arrow_right
                    }
                ),
                contentDescription = "Select address",
                tint = AppColor.gray16,
                modifier = Modifier
                    .size(24.dp)
            )
            val address = dragPlaceResponse.dataOptional?.address
            if (cameraPositionMoving || address == null) {
                Spacer(modifier = Modifier.weight(1f))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AppColor.gray21,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(32.dp))
            } else {
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = address,
                    color = AppColor.gray21,
                    style = AppTypography.androidBody1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        AppFilledButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(id = R.string.done_text),
            enabled = dragPlaceResponse is Result.Success && !cameraPositionMoving,
            onClick = onSelectPlaceOnTheMapConfirm
        )
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}


@Composable
private fun CardAddresses(
    modifier: Modifier = Modifier,
    pickUpPlace: GooglePlace?,
    dropOffPlace: GooglePlace?,
    onPlaceSelect: (pickUpPlace: Boolean) -> Unit
) {
    AppCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.ic_pickup),
                    contentDescription = null,
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_dropoff),
                    contentDescription = null,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Address(
                    title = pickUpPlace?.address,
                    placeholder = stringResource(R.string.home_adress_card_point_placeholder_pickup),
                    onPlaceSelect = {
                        onPlaceSelect(true)
                    }
                )
                AppDivider()
                Address(
                    title = dropOffPlace?.address,
                    placeholder = stringResource(R.string.home_adress_card_point_placeholder_dropoff),
                    onPlaceSelect = {
                        onPlaceSelect(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun CardCash(
    modifier: Modifier = Modifier,
    currency: String,
    isBookingNow: Boolean,
    bookingInfo: BookingInfo?,
    booking: Booking?,
) {
    AppCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.width(22.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_money),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = currency,
                        style = AppTypography.androidHeadline5,
                        color = AppColor.blue8,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    if (isBookingNow) {
                        bookingInfo?.let {
                            Prices(priceMin = it.priceMin, priceMax = it.priceMax)
                        }
                    } else {
                        booking?.let {
                            if (booking.status == BookingStatus.RIDE_COMPLETED) {
                                Prices(priceMin = it.price)
                            } else {
                                Prices(priceMin = it.priceMin, priceMax = it.priceMax)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = stringResource(R.string.home_book_card_cash_only),
                        style = AppTypography.androidCaption2,
                        color = AppColor.blueGray14,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.home_book_card_cash_only_desc),
                        style = AppTypography.androidCaption1,
                        color = AppColor.blueGray9,
                    )
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

@Composable
private fun Prices(priceMin: Double, priceMax: Double? = null) {
    Text(
        text = buildAnnotatedString {
            append(
                text = Formats.formatDecimalValue(
                    decimalValue = priceMin / 100,
                    decimalScale = 0
                )
            )
            priceMax?.let {
                append(" ")
                withStyle(
                    style = SpanStyle(letterSpacing = (-3).sp)
                ) {
                    append("–––")
                }
                append(" ")
                append(
                    text = Formats.formatDecimalValue(
                        decimalValue = priceMax / 100,
                        decimalScale = 0
                    )
                )
            }
        },
        style = AppTypography.androidHeadline5,
        color = AppColor.blueGray14,
    )
}

@Composable
private fun Address(
    title: String?,
    placeholder: String,
    onPlaceSelect: () -> Unit
) {
    Text(
        text = title ?: placeholder,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onPlaceSelect() }
            .padding(vertical = 15.dp)
            .padding(end = 16.dp),
        style = AppTypography.androidBody1,
        color = if (title == null) {
            AppColor.blueGray9
        } else {
            AppColor.gray21
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}


@Composable
private fun SheetContentUnavailableInArea() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Divider(
            modifier = Modifier.width(64.dp),
            color = AppColor.blueGray6,
            thickness = 2.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.home_sheet_service_unavailable_in_area_title),
            style = AppTypography.androidHeadline6,
            color = AppColor.blue8,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.home_sheet_service_unavailable_in_area_desc),
            modifier = Modifier.fillMaxWidth(),
            style = AppTypography.androidBody1,
            color = AppColor.gray21,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Image(
            painterResource(id = R.drawable.ic_globe_illustration),
            contentDescription = "Globe"
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.home_sheet_service_unavailable_in_area_desc_2),
            style = AppTypography.androidBody2,
            color = AppColor.blue8
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SheetContentEnterAddress(
    searchPlaceTextField: TextFieldValue,
    onSearchPlaceTextUpdate: (TextFieldValue) -> Unit,
    isPickUpPlace: Boolean,
    googlePlacesResponse: Result<List<GooglePlace>>,
    onSelect: (googlePlace: GooglePlace) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.88f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Divider(
            modifier = Modifier.width(64.dp),
            color = AppColor.blueGray6,
            thickness = 2.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(
                if (isPickUpPlace) {
                    R.string.home_enter_address_sheet_title_start_destination
                } else {
                    R.string.home_enter_address_sheet_title_end_destination
                }
            ),
            style = AppTypography.androidHeadline6,
            color = AppColor.blue8
        )
        Spacer(modifier = Modifier.height(32.dp))
        SearchAddressBar(
            modifier = Modifier.padding(horizontal = 16.dp),
            isPickUpPlace = isPickUpPlace,
            searchText = searchPlaceTextField,
            onSearchTextChanged = onSearchPlaceTextUpdate
        )
        if (googlePlacesResponse is Result.Success) {
            val results = googlePlacesResponse.data
            if (results.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                val focusManager = LocalFocusManager.current
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(results) { index, place ->
                        EnterAddressListResultItem(
                            googlePlace = place,
                            onClick = {
                                focusManager.clearFocus()
                                onSelect(place)
                            }
                        )
                        if (index != 0) {
                            AppDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.home_enter_address_sheet_empy_results),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp),
                    style = AppTypography.androidBody2,
                    color = AppColor.blueGray9,
                )
            }
        } else if (googlePlacesResponse.loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(32.dp)
                    .size(24.dp),
                color = AppColor.gray14,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun SearchAddressBar(
    modifier: Modifier = Modifier,
    isPickUpPlace: Boolean,
    searchText: TextFieldValue,
    onSearchTextChanged: (TextFieldValue) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x0A7F89AF),
                ambientColor = Color(0x0A7F89AF)
            )
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused },
        value = searchText,
        onValueChange = onSearchTextChanged,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColor.gray21,
            unfocusedTextColor = AppColor.gray21,
            disabledTextColor = AppColor.blue6,
            focusedBorderColor = AppColor.blueGray6,
            unfocusedBorderColor = AppColor.transparent,
            disabledBorderColor = AppColor.transparent,
            focusedContainerColor = AppColor.gray1,
            unfocusedContainerColor = AppColor.gray1,
            disabledContainerColor = AppColor.gray1,
            cursorColor = AppColor.colors1
        ),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        textStyle = AppTypography.androidBody1,
        leadingIcon = {
            Icon(
                painterResource(
                    id = if (isPickUpPlace) {
                        R.drawable.ic_map_point
                    } else {
                        R.drawable.ic_arrow_right
                    }
                ),
                contentDescription = "Enter address",
                tint = AppColor.gray16,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
            )
        },
        trailingIcon = {
            if (isFocused && searchText.text.isNotEmpty()) {
                IconButton(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = {
                        onSearchTextChanged(TextFieldValue())
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_clear),
                        contentDescription = "Clear",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = stringResource(R.string.home_enter_address_search_bar_hint),
                color = AppColor.blue6,
                style = AppTypography.androidBody1,
            )
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        )
    )
}

@Composable
private fun SheetContentSelectAddressMethod(
    onHide: () -> Unit,
    onAddressEnter: () -> Unit,
    onAddressSelect: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SheetSelectAddressMethodItem(
            title = stringResource(R.string.home_address_select_method_pick_on_map),
            onClick = {
                onHide()
                onAddressSelect()
            }
        )
        AppDivider()
        SheetSelectAddressMethodItem(
            title = stringResource(R.string.home_address_select_method_enter_address),
            onClick = {
                onHide()
                onAddressEnter()
            }
        )
        AppDivider()
        SheetSelectAddressMethodItem(
            title = stringResource(id = R.string.cancel_text),
            onClick = {
                onHide()
            }
        )
    }
}

@Composable
private fun SheetSelectAddressMethodItem(
    title: String,
    onClick: () -> Unit
) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 20.dp, horizontal = 20.dp),
        style = AppTypography.androidSubtitle1,
        color = AppColor.blue8,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun EnterAddressListResultItem(
    googlePlace: GooglePlace,
    onClick: () -> Unit
) {
    Text(
        text = googlePlace.address,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 20.dp, horizontal = 28.dp),
        style = AppTypography.androidBody1,
        color = AppColor.blueGray14,
    )
}

@Composable
private fun Map(
    topBarHeight: Int,
    bookingInfo: BookingInfo?,
    booking: Booking?,
    pickUpPlace: GooglePlace?,
    isPickUpPlace: Boolean,
    cardsHeight: Int,
    userLocation: Location?,
    isLocationPermissionGranted: Boolean,
    rideStatusUI: RideStatusUI,
    route: List<LatLng>,
    onDragLocationUpdate: (LatLng) -> Unit,
    onCameraPositionMoving: (Boolean) -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState()
    LaunchedEffect(key1 = userLocation) {
        userLocation?.let { userLocation ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(
                    userLocation.latitude,
                    userLocation.longitude
                ), 16f
            )
        }
    }
    val context = LocalContext.current
    if (isLocationPermissionGranted) {
        val isRideActiveAndRouteAvailable = rideStatusUI != RideStatusUI.NOT_REQUESTED &&
                rideStatusUI != RideStatusUI.NO_AVAILABLE_DRIVER_IN_AREA &&
                rideStatusUI != RideStatusUI.UNAVAILABLE_IN_AREA &&
                route.size >= 2
        val topPaddingForNorthMarkerImage =
            if (isRideActiveAndRouteAvailable && route.first().latitude < route.last().latitude) {
                48.dp
            } else {
                0.dp
            }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false
            ),
            contentPadding = with(LocalDensity.current) {
                PaddingValues(
                    top = topBarHeight.toDp() + topPaddingForNorthMarkerImage,
                    bottom = cardsHeight.toDp()
                )
            }
        ) {
            if (rideStatusUI == RideStatusUI.SELECT_A_POINT) {
                LaunchedEffect(key1 = cardsHeight) {
                    cameraPositionState.animate(
                        CameraUpdateFactory
                            .newCameraPosition(
                                cameraPositionState.position
                            ),
                        durationMs = 10
                    )
                }
                val centerPosition = cameraPositionState.position.target
                val centerMarkerState = rememberMarkerState(
                    key = "selectAPoint",
                    position = centerPosition
                )
                LaunchedEffect(key1 = centerPosition) {
                    centerMarkerState.position = centerPosition
                }
                LaunchedEffect(key1 = cameraPositionState.isMoving) {
                    onCameraPositionMoving(cameraPositionState.isMoving)
                    if (!cameraPositionState.isMoving) {
                        onDragLocationUpdate(cameraPositionState.position.target)
                    }
                }
                Marker(
                    state = centerMarkerState,
                    title = stringResource(
                        if (isPickUpPlace) {
                            R.string.home_enter_address_sheet_title_start_destination
                        } else {
                            R.string.home_enter_address_sheet_title_end_destination
                        }
                    ),
                    icon = remember {
                        bitmapDescriptorFromVector(context, R.drawable.ic_marker)
                    }
                )
            } else if (isRideActiveAndRouteAvailable) {
                val pickUpLocation = route.first()
                if (rideStatusUI != RideStatusUI.BOOK_NOW && booking != null) {
                    val markerState = rememberMarkerState(
                        key = "markerPickUpRoute",
                        position = pickUpLocation
                    )
                    LaunchedEffect(key1 = pickUpLocation) {
                        markerState.position = pickUpLocation
                    }
                    Marker(
                        markerState,
                        title = stringResource(id = R.string.home_adress_card_point_placeholder_pickup),
                        icon = remember(booking.pickup.address) {
                            View.inflate(context, R.layout.marker_card_booking_pick_up, null).let {
                                it.findViewById<TextView>(R.id.marker_booking_pick_up_address)
                                    .text = booking.pickup.address
                                BitmapDescriptorFactory.fromBitmap(
                                    createBitmapFromLayout(it)
                                )
                            }
                        },
                    )
                }
                val dropOffLocation = route.last()
                if (rideStatusUI == RideStatusUI.BOOK_NOW && bookingInfo != null) {
                    val markerDropOffCardBookNowState = rememberMarkerState(
                        key = "markerDropOffCardBookNow",
                        position = dropOffLocation
                    )
                    LaunchedEffect(key1 = dropOffLocation) {
                        markerDropOffCardBookNowState.position = dropOffLocation
                    }
                    Marker(
                        state = markerDropOffCardBookNowState,
                        title = stringResource(id = R.string.home_adress_card_point_placeholder_dropoff),
                        icon = remember(bookingInfo.estimatedDistance, bookingInfo.estimatedTime) {
                            View.inflate(context, R.layout.marker_card_book_now_drop_off, null)
                                .let {
                                    it.findViewById<TextView>(R.id.marker_book_now_km_value)
                                        .text = Formats.formatDecimalValue(
                                        decimalValue = bookingInfo.estimatedDistance / 1000,
                                        decimalScale = 1,
                                        stripTrailingZeros = true
                                    )
                                    it.findViewById<TextView>(R.id.marker_book_now_min_value)
                                        .text =
                                        (bookingInfo.estimatedTime / 60.0).roundToInt().toString()

                                    BitmapDescriptorFactory.fromBitmap(
                                        createBitmapFromLayout(it)
                                    )
                                }
                        },
                    )
                } else if (booking != null) {
                    val markerDropOffCardBookingState = rememberMarkerState(
                        key = "markerDropOffCardBooking",
                        position = dropOffLocation
                    )
                    LaunchedEffect(key1 = dropOffLocation) {
                        markerDropOffCardBookingState.position = dropOffLocation
                    }
                    Marker(
                        state = markerDropOffCardBookingState,
                        title = stringResource(id = R.string.home_adress_card_point_placeholder_dropoff),
                        icon = remember(
                            booking.estimatedDistance,
                            booking.estimatedTime,
                            booking.dropOff.address
                        ) {
                            View.inflate(context, R.layout.marker_card_booking_drop_off, null)
                                .let {
                                    it.findViewById<TextView>(R.id.marker_booking_drop_off_address)
                                        .text = booking.dropOff.address
                                    it.findViewById<TextView>(R.id.marker_booking_drop_off_km_value)
                                        .text = Formats.formatDecimalValue(
                                        decimalValue = booking.estimatedDistance / 1000,
                                        decimalScale = 1,
                                        stripTrailingZeros = true
                                    )
                                    it.findViewById<TextView>(R.id.marker_booking_drop_off_min_value)
                                        .text =
                                        (booking.estimatedTime / 60.0).roundToInt().toString()

                                    BitmapDescriptorFactory.fromBitmap(
                                        createBitmapFromLayout(it)
                                    )
                                }
                        },
                    )
                }
                Polyline(
                    points = route,
                    color = Color(0xFF1868D2),
                    width = 14f,
                )
                val bitmapDescriptor = remember {
                    bitmapDescriptorFromVector(context, R.drawable.ic_point)
                }
                Polyline(
                    points = route,
                    startCap = CustomCap(bitmapDescriptor),
                    endCap = CustomCap(bitmapDescriptor),
                    color = Color(0xFF00B0FF),
                    width = 10f,
                )
                val density = LocalDensity.current
                LaunchedEffect(key1 = route, key2 = cardsHeight) {
                    val bounds = LatLngBounds.Builder().apply {
                        include(pickUpLocation)
                        include(dropOffLocation)
                    }.build()
                    val cu = CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        with(density) { 24.dp.toPx().roundToInt() }
                    )
                    cameraPositionState.move(cu)
                }
            } else if (pickUpPlace != null && rideStatusUI == RideStatusUI.NOT_REQUESTED) {
                val latLng = remember(pickUpPlace) {
                    LatLng(pickUpPlace.latitude, pickUpPlace.longitude)
                }
                val pickUpMarkerState = rememberMarkerState(
                    key = "markerPickUp",
                    position = latLng
                )
                LaunchedEffect(key1 = latLng) {
                    pickUpMarkerState.position = latLng
                }
                Marker(
                    state = pickUpMarkerState,
                    title = stringResource(R.string.home_enter_address_sheet_title_start_destination),
                    icon = remember {
                        bitmapDescriptorFromVector(context, R.drawable.ic_marker)
                    }
                )
            }
        }
    }
}

@Composable
private fun LocationPermissionsHandler(
    locationManager: LocationManager,
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current

    var shouldShowRationale by rememberSaveable { mutableStateOf(false) }
    var shouldOpenSettingsToGrantPermissions by rememberSaveable { mutableStateOf(false) }

    val locationEnableRequestResult = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            // show a msg that a user's location won't be available
        }
    }

    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val activity = context.getActivity() ?: return@rememberLauncherForActivityResult
        if (permissionsMap.values.any { it }) {
            onPermissionGranted()
            locationManager.start()
        } else if (permissionsMap.keys.any { shouldShowRequestPermissionRationale(activity, it) }) {
            shouldShowRationale = true
        } else if (permissionsMap.values.all { !it }) {
            shouldOpenSettingsToGrantPermissions = true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(true) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                if (!PermissionsUtil.isLocationPermissionGranted(context)) {
                    launcherMultiplePermissions.launch(PermissionsUtil.LOCATION_PERMISSIONS)
                } else {
                    locationManager.start()
                }
                LocationManager.checkSystemLocationSettings(
                    context,
                    locationEnableRequestResult
                )
            }
        }

        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.DESTROYED) {
            launch {
                locationManager.stop()
            }
        }
    }

    if (shouldShowRationale) {
        val closeDialog = { shouldShowRationale = false }
        AppAlertDialog(
            title = "Please grant Location permission",
            subtitle = "Location permission is needed to ",
            //confirmButton = stringResource(id = R.string.ok_text),
            //cancelButton = stringResource(id = R.string.cancel_text),
            onPositiveBtnClicked = {
                closeDialog()
                launcherMultiplePermissions.launch(PermissionsUtil.LOCATION_PERMISSIONS)
            },
            //onCancel = closeDialog,
            onDismiss = closeDialog,
        )
    }

    if (shouldOpenSettingsToGrantPermissions) {
        val closeDialog = { shouldOpenSettingsToGrantPermissions = false }
        AppAlertDialog(
            title = "Please grant Location permission",
            subtitle = "Location permission is needed to ",
            // confirmButton = "Settings",
            //cancelButton = stringResource(id = R.string.cancel_text),
            onPositiveBtnClicked = {
                closeDialog()
                IntentUtils.openAppSettings(context)
            },
            //onCancel = closeDialog,
            onDismiss = closeDialog,
        )
    }
}


@Composable
private fun HomeContentPreview(rideStatusUI: RideStatusUI) {
    AppTheme {
        Surface {
            HomeContent(
                isLocationPermissionGranted = true,
                userLocation = null,
                pickUpPlace = GooglePlace("First", "1", 0.0, 0.0),
                dropOffPlace = GooglePlace("Second", "2", 0.0, 0.0),
                onPlaceSelect = {},
                openDrawer = {},
                rideStatusUI = rideStatusUI,
                onRideCancel = {},
                toInitialState = {},
                onRideBook = {},
                onRideDone = {},
                isPickUpPlace = true,
                onShowUnavailableInAreaSheet = {},
                route = emptyList(),
                dragPlaceResponse = Result.Initial,
                onDragLocationUpdate = {},
                onSelectPlaceOnTheMapConfirm = {},
                booking = BOOKING_TEST,
                bookingInfo = BOOKING_INFO_TEST,
                currency = "$",
                creatingBooking = false
            )
        }
    }
}

@Composable
@Preview
private fun HomeContentPreviewNotRequested() {
    HomeContentPreview(rideStatusUI = RideStatusUI.NOT_REQUESTED)
}

@Composable
@Preview
private fun HomeContentPreviewSelectAPoint() {
    HomeContentPreview(rideStatusUI = RideStatusUI.SELECT_A_POINT)
}

@Composable
@Preview
private fun HomeContentPreviewBookNow() {
    HomeContentPreview(rideStatusUI = RideStatusUI.BOOK_NOW)
}

@Composable
@Preview
private fun HomeContentPreviewLookingForDriver() {
    HomeContentPreview(rideStatusUI = RideStatusUI.LOOKING_FOR_DRIVER)
}

@Composable
@Preview
private fun HomeContentPreviewNoAvailableDriverInArea() {
    HomeContentPreview(rideStatusUI = RideStatusUI.NO_AVAILABLE_DRIVER_IN_AREA)
}

@Composable
@Preview
private fun HomeContentPreviewUnavailableInArea() {
    HomeContentPreview(rideStatusUI = RideStatusUI.UNAVAILABLE_IN_AREA)
}

@Composable
@Preview
private fun HomeContentPreviewDriverOnTheWay() {
    HomeContentPreview(rideStatusUI = RideStatusUI.DRIVER_ON_THE_WAY)
}

@Composable
@Preview
private fun HomeContentPreviewDriverArrived() {
    HomeContentPreview(rideStatusUI = RideStatusUI.DRIVER_ARRIVED)
}

@Composable
@Preview
private fun HomeContentPreviewOnJourney() {
    HomeContentPreview(rideStatusUI = RideStatusUI.ON_JOURNEY)
}

@Composable
@Preview
private fun HomeContentPreviewRideCompleted() {
    HomeContentPreview(rideStatusUI = RideStatusUI.RIDE_COMPLETED)
}

private val BOOKING_TEST = Booking(
    passengerId = "1",
    id = "1",
    status = BookingStatus.ON_JOURNEY,
    pickup = GooglePlace("", "", 0.0, 0.0),
    dropOff = GooglePlace("", "", 0.0, 0.0),
    priceMin = 5.0,
    priceMax = 10.0,
    price = 7.0,
    estimatedTime = 10,
    estimatedDistance = 134124.0,
    vehicle = Vehicle(
        driverId = "1",
        driverName = "Nathan Cooper",
        model = "Nissan Leaf",
        color = "White",
        plateNumber = "BD51 SMR",
    ),
    dateCreated = Instant.now(),
)

private val BOOKING_INFO_TEST = BookingInfo(
    pickupCoordinates = Coordinates(0.0, 0.0),
    dropOffCoordinates = Coordinates(0.0, 0.0),
    priceMin = 5.0,
    priceMax = 10.0,
    estimatedTime = 10,
    route = emptyList(),
    estimatedDistance = 40.0
)