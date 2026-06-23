package app.arteh.easydialer.dial

import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.arteh.easydialer.R
import app.arteh.easydialer.clog.models.Clog
import app.arteh.easydialer.contacts.Contact
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor
import app.arteh.easydialer.ui.theme.appTypography
import app.arteh.easydialer.utility.Holder
import kotlin.random.Random

@Composable
fun DialPadScreen(viewModel: DialPadVM) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isBigDial)
        BigDialer(uiState, viewModel::onAction)
    else
        NormalDialer(uiState, viewModel::onAction)
}

@Composable
private fun NormalDialer(uiState: DialUIState, onAction: (DialAction) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        SearchedNumbers(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            uiState.contactList,
            uiState.dialedList,
            onAction
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColor.BackTrans.resolve())
                .padding(10.dp),
        ) {
            DialedNumberDisplay(uiState.number, uiState.showDial, onAction)

            AnimatedVisibility(uiState.showDial) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NormalDialPadGrid(
                        onNumberClick = { onAction(DialAction.NumberCLicked(it)) },
                        onNumberLongPress = { onAction(DialAction.NumberLongCLicked(it)) }
                    )

                    CallControls(onCall = { onAction(DialAction.ShowMakeCall(uiState.number)) })
                }
            }
        }
    }
}

@Composable
private fun SearchedNumbers(
    modifier: Modifier,
    contactList: List<Contact>,
    dialedList: List<Clog>,
    onAction: (DialAction) -> Unit
) {
    Column(modifier) {
        if (contactList.isNotEmpty()) {
            Text(modifier = Modifier.padding(horizontal = 10.dp), text = "All Contact")
            contactList.forEachIndexed { index, contact ->
                ItemContact(contact, onAction)
            }
        }

        if (dialedList.isNotEmpty()) {
            Text(modifier = Modifier.padding(horizontal = 10.dp), text = "Not in your contacts")
            dialedList.forEachIndexed { index, callLog ->
                ItemNonContact(callLog, onAction)
            }
        }
    }
}

@Composable
private fun DialedNumberDisplay(
    number: String, showDial: Boolean, onAction: (DialAction) -> Unit
) {
    val foldIcon = if (showDial) R.drawable.arrow_down else R.drawable.arrow_up
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(35.dp)
                .padding(5.dp)
                .noRippleClickable { onAction(DialAction.ChangeFold) },
            painter = painterResource(foldIcon),
            contentDescription = stringResource(R.string.clear),
            tint = AppColor.Icons.resolve()
        )
        Text(
            modifier = Modifier.weight(1f),
            text = number,
            fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp,
            textAlign = TextAlign.Center
        )
        Icon(
            modifier = Modifier
                .size(35.dp)
                .padding(5.dp)
                .combinedClickable(
                    onClick = { onAction(DialAction.BackSpace) },
                    onLongClick = { onAction(DialAction.LongBackSpace) }),
            painter = painterResource(R.drawable.backspace),
            contentDescription = stringResource(R.string.clear),
            tint = AppColor.Icons.resolve()
        )
    }
}

@Composable
fun NormalDialPadGrid(onNumberClick: (String) -> Unit, onNumberLongPress: (String) -> Unit) {
    val dialNumbers = remember {
        listOf(
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "*", "0", "#"
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(dialNumbers) { number ->
            DialButton(
                number = number,
                onClick = { onNumberClick(number) },
                onLongPress = { onNumberLongPress(number) }
            )
        }
    }
}

@Composable
private fun DialButton(number: String, onClick: () -> Unit, onLongPress: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CallControls(onCall: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 10.dp)
            .height(50.dp)
            .width(100.dp)
            .background(AppColor.GradGreen.resolve(), CircleShape)
            .noRippleClickable(onCall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 5.dp)
                .size(25.dp),
            painter = painterResource(R.drawable.call),
            contentDescription = stringResource(R.string.call),
            tint = Color.White
        )

        Text("Call", color = Color.White, fontSize = 18.sp)
    }
}

@Composable
private fun ItemContact(contact: Contact, onAction: (DialAction) -> Unit) {
    val context = LocalContext.current
    var bitmap by remember(contact) { mutableStateOf<ImageBitmap?>(null) }
    val color = remember(contact) { Holder.colors[Random.nextInt(0, Holder.colors.size)] }

    LaunchedEffect(contact) {
        try {
            bitmap =
                contact.thumbUri?.let {
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        it
                    ).asImageBitmap()
                }
        } catch (e: Exception) {
        }
    }

    Row(
        Modifier
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
            .noRippleClickable { onAction(DialAction.ShowMakeCallContact(contact)) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (bitmap != null)
            Image(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .noRippleClickable { onAction(DialAction.ShowContact(contact.id)) },
                bitmap = bitmap!!,
                contentDescription = null
            )
        else
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(color, CircleShape)
                    .noRippleClickable { onAction(DialAction.ShowContact(contact.id)) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name[0].toString(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .weight(1f)
        ) {
            Text(text = contact.name, style = MaterialTheme.appTypography.h4)
            Text(
                text = contact.phone,
                style = LocalTextStyle.current.copy(
                    textDirection = TextDirection.Ltr,
                    color = AppColor.Desc.resolve()
                )
            )
        }

        Icon(
            modifier = Modifier
                .padding(end = 10.dp)
                .size(45.dp)
                .background(AppColor.GradPurple.resolve().copy(alpha = 0.1f), CircleShape)
                .padding(10.dp)
                .noRippleClickable({ onAction(DialAction.ShowSendSMSContact(contact)) }),
            painter = painterResource(R.drawable.sms),
            contentDescription = null,
            tint = AppColor.GradPurple.resolve()
        )
    }
}

@Composable
private fun ItemNonContact(callLog: Clog, onAction: (DialAction) -> Unit) {
    val grayColor = AppColor.UnContactBack.resolve()

    Row(
        Modifier
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                RoundedCornerShape(5.dp)
            )
            .padding(10.dp)
            .noRippleClickable { onAction(DialAction.ShowMakeCall(callLog.number)) },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(60.dp)
                .background(grayColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(R.drawable.person),
                contentDescription = null,
                tint = AppColor.Font.resolve(),
            )
        }

        Column(
            Modifier
                .padding(horizontal = 10.dp)
                .weight(1f)
        ) {
            Text(
                text = callLog.number,
                fontWeight = FontWeight.Bold,
                style = LocalTextStyle.current.copy(
                    textDirection = TextDirection.Ltr,
                    color = AppColor.Desc.resolve()
                )
            )
        }

        Icon(
            modifier = Modifier
                .padding(end = 10.dp)
                .size(45.dp)
                .background(AppColor.GradPurple.resolve().copy(alpha = 0.1f), CircleShape)
                .padding(10.dp)
                .noRippleClickable({ onAction(DialAction.ShowSendSMS(callLog.number)) }),
            painter = painterResource(R.drawable.sms),
            contentDescription = null,
            tint = AppColor.GradPurple.resolve()
        )
    }
}