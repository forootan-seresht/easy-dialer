package app.arteh.easydialer.calling

import android.app.Activity
import android.provider.MediaStore
import android.telecom.Call
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.arteh.easydialer.R
import app.arteh.easydialer.calling.models.CallState
import app.arteh.easydialer.contacts.show.models.Contact
import app.arteh.easydialer.dial.DialPadGrid
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor

private val LocalVM = staticCompositionLocalOf<CallVM> {
    error("CallVM not provided")
}

@Composable
fun CallScreen(vm: CallVM = viewModel()) {
    CompositionLocalProvider(LocalVM provides vm) {
        CallContent()
    }
}

@Composable
fun CallContent() {
    val callVM = LocalVM.current
    val uiState = callVM.uiState.collectAsStateWithLifecycle().value

    val context = LocalContext.current

    Box {
        when (uiState.state) {
            CallState.Incoming -> IncomingCallUI(uiState.phoneNumber, uiState.contact)
            CallState.Calling -> {}
            CallState.Rejected -> (context as Activity).finish()
            CallState.Talking -> TalkingUI(
                uiState.phoneNumber,
                uiState.contact,
                uiState.isMute,
                uiState.isSpeaker
            )
        }

        if (uiState.showDialPad)
            DialPadUI(callVM::sendDtmf, callVM::hideDialPad)
    }
}

@Composable
private fun DialPadUI(onClick: (String) -> Unit, onBack: () -> Unit) {
    Column {
        DialPadGrid(onClick, {})

        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .height(70.dp)
                .width(150.dp)
                .background(AppColor.GradGreen.resolve(), RoundedCornerShape(5.dp))
                .noRippleClickable(onBack),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(35.dp),
                painter = painterResource(R.drawable.back),
                contentDescription = null,
                tint = Color.White
            )
            Text(
                text = "Return to call",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun TalkingUI(number: String, contact: Contact?, isMute: Boolean, isSpeaker: Boolean) {
    val callVM = LocalVM.current
    val context = LocalContext.current

    var contactPic by remember { mutableStateOf<ImageBitmap?>(null) }

    if (contact != null && contact.thumbUri != null)
        LaunchedEffect(Unit) {
            try {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(context.contentResolver, contact.photoUri)
                contactPic = bitmap.asImageBitmap()
            } catch (e: Exception) {
            }
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = contact?.name ?: "-",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = number,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
        )

        if (contactPic != null)
            Image(
                modifier = Modifier.size(150.dp),
                bitmap = contactPic!!,
                contentDescription = null
            )

        Row() {
            BigCallButton(
                text = "Mute", if (isMute) R.drawable.mic_on
                else R.drawable.mic_off, Modifier
                    .padding(10.dp)
                    .weight(1f)
                    .height(100.dp)
                    .background(Color(0xFFFFFF3F), RoundedCornerShape(5.dp))
                    .noRippleClickable(callVM::toggleMute)
            )

            BigCallButton(
                text = "Mute", if (isSpeaker) R.drawable.speaker_off
                else R.drawable.speaker_on, Modifier
                    .padding(10.dp)
                    .weight(1f)
                    .height(100.dp)
                    .background(Color(0xFF30A3FF), RoundedCornerShape(5.dp))
                    .noRippleClickable(callVM::toggleSpeaker)
            )
        }

        Row() {
            BigCallButton(
                text = "Mute", R.drawable.call_end,
                Modifier
                    .padding(10.dp)
                    .weight(1f)
                    .height(100.dp)
                    .background(Color(0xFFFF2E2E), RoundedCornerShape(5.dp))
                    .noRippleClickable(callVM::hangUp)
            )

            BigCallButton(
                text = "Mute", if (isSpeaker) R.drawable.speaker_off
                else R.drawable.speaker_on, Modifier
                    .padding(10.dp)
                    .weight(1f)
                    .height(100.dp)
                    .background(Color(0xFF9E67FF), RoundedCornerShape(5.dp))
                    .noRippleClickable(callVM::showDialPad)
            )
        }
    }
}

@Composable
private fun IncomingCallUI(number: String, contact: Contact?) {
    val callVM = LocalVM.current
    val context = LocalContext.current

    var contactPic by remember { mutableStateOf<ImageBitmap?>(null) }

    if (contact != null && contact.thumbUri != null)
        LaunchedEffect(Unit) {
            try {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(context.contentResolver, contact.photoUri)
                contactPic = bitmap.asImageBitmap()
            } catch (e: Exception) {
            }
        }

    Box {
        if (contactPic != null)
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = contactPic!!,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF000000),
                                Color(0x00000000),
                            )
                        )
                    )
                    .padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = contact?.name ?: "-",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = number,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                BigCallButton(
                    "Answer", R.drawable.call,
                    Modifier
                        .padding(10.dp)
                        .weight(1f)
                        .height(100.dp)
                        .background(Color.Green, RoundedCornerShape(5.dp))
                        .noRippleClickable(callVM::answer),
                )

                BigCallButton(
                    "Reject", R.drawable.call_end,
                    Modifier
                        .padding(10.dp)
                        .weight(1f)
                        .height(100.dp)
                        .background(Color.Red, RoundedCornerShape(5.dp))
                        .noRippleClickable(callVM::reject)
                )
            }
        }
    }
}

@Composable
fun OngoingCallUI(call: Call) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(32.dp),
//        verticalArrangement = Arrangement.SpaceEvenly,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        Text("Call in Progress", fontSize = 40.sp)
//
//        BigCallButton("HANG UP", Color.Red) {
//            call.disconnect()
//        }
//    }
}

@Composable
fun BigCallButton(
    text: String,
    icon: Int,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(60.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.White
        )
        Text(text = text, color = Color.White)
    }
}