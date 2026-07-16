package app.arteh.easydialer.calling

import android.app.Activity
import android.provider.MediaStore
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.Contact
import app.arteh.easydialer.dial.BigDialPadGrid
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CallScreen(vm: CallVM = viewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Box {
        when (uiState.state) {
            CallState.Incoming -> IncomingCallUI(uiState.phoneNumber, uiState.contact, vm::onAction)

            CallState.Calling -> CallingUI(
                uiState.phoneNumber,
                uiState.contact,
                uiState.isMute,
                uiState.isSpeaker,
                vm::onAction
            )

            CallState.Rejected -> (context as Activity).finish()
            CallState.Talking -> TalkingUI(
                uiState.phoneNumber,
                uiState.contact,
                uiState.isMute,
                uiState.isSpeaker,
                vm::onAction
            )
        }

        if (uiState.showDialPad)
            DialPadUI(vm::onAction)
    }
}

@Composable
private fun DialPadUI(onAction: (CallAction) -> Unit) {
    Column {
        BigDialPadGrid(
            onNumberClick = { onAction(CallAction.SendDtmf(it)) },
            onNumberLongPress = {}
        )

        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .height(70.dp)
                .width(150.dp)
                .background(AppColor.GradGreen.resolve(), RoundedCornerShape(5.dp))
                .noRippleClickable { onAction(CallAction.HideDialPad) },
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
                text = stringResource(R.string.return_to_call),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun CallingUI(
    number: String,
    contact: Contact?,
    isMute: Boolean,
    isSpeaker: Boolean,
    onAction: (CallAction) -> Unit
) {
    val context = LocalContext.current

    var contactPic by remember { mutableStateOf<ImageBitmap?>(null) }

    if (contact?.photoUri != null)
        LaunchedEffect(contact.photoUri) {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, contact.photoUri)
                }
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

        Text(
            text = stringResource(R.string.calling),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (contactPic != null)
            Image(
                modifier = Modifier.size(150.dp),
                bitmap = contactPic!!,
                contentDescription = null
            )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            BigCallButton(
                if (isMute) R.drawable.mic_on
                else R.drawable.mic_off, AppColor.GradYoda.resolve()
            ) { onAction(CallAction.ToggleMute) }

            BigCallButton(
                if (isSpeaker) R.drawable.speaker_off
                else R.drawable.speaker_on, Color(0xFF30A3FF)
            ) { onAction(CallAction.ToggleSpeaker) }

            BigCallButton(R.drawable.dial, Color(0xFF9E67FF)) { onAction(CallAction.ShowDialPad) }
        }

        Row(modifier = Modifier.padding(top = 15.dp)) {
            BigCallButton(
                R.drawable.call_end, Color(0xFFFF2E2E)
            ) { onAction(CallAction.HangUp) }
        }
    }
}

@Composable
private fun TalkingUI(
    number: String,
    contact: Contact?,
    isMute: Boolean,
    isSpeaker: Boolean,
    onAction: (CallAction) -> Unit
) {
    val context = LocalContext.current

    var contactPic by remember { mutableStateOf<ImageBitmap?>(null) }

    if (contact?.photoUri != null)
        LaunchedEffect(contact.photoUri) {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, contact.photoUri)
                }
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            BigCallButton(
                if (isMute) R.drawable.mic_on
                else R.drawable.mic_off, AppColor.GradYoda.resolve()
            ) { onAction(CallAction.ToggleMute) }

            BigCallButton(
                if (isSpeaker) R.drawable.speaker_off
                else R.drawable.speaker_on, Color(0xFF30A3FF)
            ) { onAction(CallAction.ToggleSpeaker) }

            BigCallButton(R.drawable.dial, Color(0xFF9E67FF)) { onAction(CallAction.ShowDialPad) }
        }

        Row(modifier = Modifier.padding(top = 15.dp)) {
            BigCallButton(
                R.drawable.call_end, Color(0xFFFF2E2E)
            ) { onAction(CallAction.HangUp) }
        }
    }
}

@Composable
private fun IncomingCallUI(number: String, contact: Contact?, onAction: (CallAction) -> Unit) {
    val context = LocalContext.current

    var contactPic by remember { mutableStateOf<ImageBitmap?>(null) }

    if (contact?.photoUri != null)
        LaunchedEffect(contact.photoUri) {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, contact.photoUri)
                }
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
                    .padding(top = 40.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 100.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BigCallButton(
                    R.drawable.call, Color(0xFF33C385),
                ) { onAction(CallAction.Answer) }

                BigCallButton(
                    R.drawable.call_end, Color(0xFFF53F5A)
                ) { onAction(CallAction.Reject) }
            }
        }
    }
}

@Composable
fun BigCallButton(icon: Int, color: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(80.dp)
            .background(color, CircleShape)
            .padding(20.dp)
            .noRippleClickable(onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(60.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.White
        )
    }
}