package com.example.efficientdet_lite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.efficientdet_lite.app.TripDetails

private val Navy = Color(0xFF0D1B5E)
private val LogoNavy = Color(0xFF13235E)
private val BodyGray = Color(0xFF6D7898)
private val Blue = Color(0xFF1F6BFF)
private val BorderBlue = Color(0xFFCFE0FF)

@Composable
fun BoardingPassFormScreen(
    initialTripDetails: TripDetails,
    onBackClick: () -> Unit,
    onSaveClick: (TripDetails) -> Unit
) {
    var from by remember { mutableStateOf(initialTripDetails.from) }
    var to by remember { mutableStateOf(initialTripDetails.to) }
    var date by remember { mutableStateOf(initialTripDetails.date) }
    var flight by remember { mutableStateOf(initialTripDetails.flight) }
    var gate by remember { mutableStateOf(initialTripDetails.gate) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF8FBFF),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 18.dp, bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Navy,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onBackClick() }
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Boarding Pass",
                    color = Navy,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add your trip details so AirSure can show relevant travel reminders.",
                color = BodyGray,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(26.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF4FAFF),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = BorderBlue,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .width(44.dp)
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = BorderBlue,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ConfirmationNumber,
                                contentDescription = null,
                                tint = Blue
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Trip details",
                                color = LogoNavy,
                                fontSize = 18.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Enter your trip details to personalize alerts.",
                                color = BodyGray,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    AirSureTextField(
                        value = from,
                        onValueChange = { from = it.uppercase() },
                        label = "From",
                        placeholder = "LAX"
                    )

                    AirSureTextField(
                        value = to,
                        onValueChange = { to = it.uppercase() },
                        label = "To",
                        placeholder = "JFK"
                    )

                    AirSureTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = "Date",
                        placeholder = "May 20"
                    )

                    AirSureTextField(
                        value = flight,
                        onValueChange = { flight = it.uppercase() },
                        label = "Flight",
                        placeholder = "AA123"
                    )

                    AirSureTextField(
                        value = gate,
                        onValueChange = { gate = it.uppercase() },
                        label = "Gate",
                        placeholder = "24B"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onSaveClick(
                            TripDetails(
                                from = from.trim().uppercase(),
                                to = to.trim().uppercase(),
                                date = date.trim(),
                                flight = flight.trim().uppercase(),
                                gate = gate.trim().uppercase()
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Flight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Save Trip",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFFE8EDF5),
                        disabledContentColor = Color(0xFF9AA5BC)
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Scan boarding pass · Coming soon",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "Reset all fields",
                    color = Color(0xFF8C94AA),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            from = ""
                            to = ""
                            date = ""
                            flight = ""
                            gate = ""
                        }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AirSureTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        placeholder = {
            Text(placeholder)
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue,
            unfocusedBorderColor = BorderBlue,
            focusedLabelColor = Blue,
            cursorColor = Blue,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}
