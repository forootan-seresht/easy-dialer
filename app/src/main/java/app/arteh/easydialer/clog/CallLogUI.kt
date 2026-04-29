package app.arteh.easydialer.clog

import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.arteh.easydialer.R
import app.arteh.easydialer.clog.models.CLogAction
import app.arteh.easydialer.clog.models.Clog
import app.arteh.easydialer.clog.models.LogStatus
import app.arteh.easydialer.dialer.DigMySimCards
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor
import app.arteh.easydialer.ui.theme.appTypography
import app.arteh.easydialer.utility.Holder

@Composable
fun CLogScreen(callLogVM: CallLogVM) {

    val uiState by callLogVM.uiState.collectAsStateWithLifecycle()
    val dialerShowState by callLogVM.dialerHR.showState.collectAsStateWithLifecycle()

    Column {
        TopCategory(uiState.selectedStatus, callLogVM::changeLogType)

        LazyColumn {
            uiState.logs.forEach { (date, clogs) ->
                stickyHeader {
                    itemHeader(date)
                }

                itemsIndexed(clogs) { index, log ->
                    ItemCallLog(index, log, callLogVM::onAction)
                }
            }
        }
    }

    if (dialerShowState.showMyNumbers)
        DigMySimCards(
            callLogVM.dialerHR::dismissPopup,
            callLogVM.simCardHR.simCardList,
            callLogVM.dialerHR::selectSim
        )
}

@Composable
private fun TopCategory(selectedType: LogStatus, onChangeType: (LogStatus) -> Unit) {
    Row(
        modifier = Modifier
            .padding(10.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        LogStatus.entries.forEach {
            ItemCategory(it.icon, it.name, selectedType == it, { onChangeType(it) })
        }
    }
}

@Composable
private fun ItemCategory(icon: Int, title: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderAlpha = if (isSelected) 1f else 0.4f

    Row(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .border(
                1.dp,
                AppColor.Icons.resolve().copy(alpha = borderAlpha),
                RoundedCornerShape(7.dp)
            )
            .padding(7.dp)
            .noRippleClickable(onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            modifier = Modifier
                .padding(end = 5.dp)
                .size(20.dp),
            painter = painterResource(icon),
            contentDescription = null,
        )

        Text(title)
    }
}

@Composable
private fun itemHeader(date: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColor.BackTrans.resolve())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = date,
    )
}

@Composable
private fun ItemCallLog(index: Int, log: Clog, onAction: (CLogAction) -> Unit) {
    var expanded by remember(log) { mutableStateOf(false) }
    val context = LocalContext.current
    var bitmap by remember(log) { mutableStateOf<ImageBitmap?>(null) }

    val grayColor = AppColor.UnContactBack.resolve()

    val firstChar = log.contact?.name?.firstOrNull()?.uppercaseChar() ?: '#'
    val color = if (log.contact != null)
        Holder.colors[(firstChar).toInt() % 7]
    else grayColor

    if (log.contact != null) {
        LaunchedEffect(log) {
            try {
                bitmap =
                    log.contact.thumbUri?.let {
                        MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            it
                        ).asImageBitmap()
                    }
            } catch (e: Exception) {
            }
        }
    }

    Column(
        Modifier
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                RoundedCornerShape(5.dp)
            )
            .padding(10.dp)
            .noRippleClickable { expanded = !expanded },
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (bitmap != null)
                Image(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .noRippleClickable {
                            onAction(CLogAction.ShowContact(log.contact?.id ?: 0L))
                        },
                    bitmap = bitmap!!,
                    contentDescription = null
                )
            else
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(color, CircleShape)
                        .noRippleClickable {
                            onAction(CLogAction.ShowContact(log.contact?.id ?: 0L))
                        },
                    contentAlignment = Alignment.Center
                ) {

                    if (log.contact != null)
                        Text(
                            text = firstChar.toString(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    else Icon(
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
                if (log.contact == null)
                    Text(
                        text = log.number,
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(
                            textDirection = TextDirection.Ltr,
                            color = AppColor.Desc.resolve()
                        )
                    )
                else {
                    Text(text = log.contact.name, fontWeight = FontWeight.Bold)
                    Text(
                        text = log.number, style = LocalTextStyle.current.copy(
                            textDirection = TextDirection.Ltr,
                            color = AppColor.Desc.resolve()
                        )
                    )
                }
            }

            Column {
                Image(
                    painter = painterResource(log.status.icon),
                    contentDescription = log.status.fullName
                )
                Text(
                    modifier = Modifier.padding(top = 5.dp),
                    text = log.time,
                    style = MaterialTheme.appTypography.desc
                )
            }
        }

        if (expanded) {
            Spacer(Modifier.height(10.dp))

            ItemExpand(
                grayColor,
                0.dp,
                AppColor.GradGreen.resolve(),
                R.drawable.call,
                R.string.call,
                RoundedCornerShape(7.dp, 7.dp, 0.dp, 0.dp),
                { onAction(CLogAction.ShowMakeCall(log)) }
            )
            ItemExpand(
                grayColor,
                3.dp,
                AppColor.GradPurple.resolve(),
                R.drawable.sms,
                R.string.send_message,
                RoundedCornerShape(0.dp)
            ) { onAction(CLogAction.ShowSendSMS(log)) }
            ItemExpand(
                grayColor,
                0.dp,
                AppColor.GradBlue.resolve(),
                R.drawable.history,
                R.string.show_history,
                RoundedCornerShape(0.dp, 0.dp, 7.dp, 7.dp),
                { onAction(CLogAction.ShowHistory(index)) }
            )
        }
    }
}

@Composable
private fun ItemExpand(
    grayColor: Color,
    paddVert: Dp,
    color: Color,
    drawable: Int,
    text: Int,
    shape: RoundedCornerShape,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = paddVert)
            .fillMaxWidth()
            .background(grayColor, shape)
            .padding(10.dp)
            .noRippleClickable(onAction),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 10.dp)
                .size(45.dp)
                .background(color.copy(alpha = 0.1f), CircleShape)
                .padding(10.dp),
            painter = painterResource(drawable),
            contentDescription = null,
            tint = color
        )

        Text(stringResource(text))
    }
}