package app.arteh.easydialer.clog

import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.arteh.easydialer.Holder
import app.arteh.easydialer.R
import app.arteh.easydialer.clog.models.Clog
import app.arteh.easydialer.ui.theme.AppColor
import app.arteh.easydialer.ui.theme.appTypography

@Composable
fun CLogScreen(cLogVM: CLogVM) {

    val callLogs = cLogVM.logsFlow.collectAsStateWithLifecycle().value

    Column() {
        LazyColumn() {
            itemsIndexed(callLogs) { idx, log ->
                ItemCallLog(idx, log)
            }
        }
    }
}

@Composable
private fun ItemCallLog(idx: Int, log: Clog) {
    val context = LocalContext.current
    var bitmap by remember(log) { mutableStateOf<ImageBitmap?>(null) }

    val firstChar = log.contact?.name?.firstOrNull()?.uppercaseChar() ?: '#'
    val color = if (log.contact != null)
        Holder.colors[(firstChar).toInt() % 7]
    else AppColor.Gray1.resolve()

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

    Row(
        Modifier
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                RoundedCornerShape(5.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (bitmap != null)
            Image(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                bitmap = bitmap!!,
                contentDescription = null
            )
        else
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(color, CircleShape),
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
                    tint = Color.Black,
                )
            }

        Column(
            Modifier
                .padding(horizontal = 10.dp)
                .weight(1f)
        ) {
            if (log.contact == null)
                Text(text = log.number, fontWeight = FontWeight.Bold)
            else {
                Text(text = log.contact.name, fontWeight = FontWeight.Bold)
                Text(text = log.number, style = MaterialTheme.appTypography.desc)
            }
        }

        val pair = when (log.status) {
            1 -> ("Received" to R.drawable.call_received)
            2 -> ("Made" to R.drawable.call_made)
            3 -> ("Missed" to R.drawable.call_missed)
            5 -> ("Rejected" to R.drawable.call_rejected)

            else -> ("Others" to R.drawable.local_phone)
        }

        Column {
            Image(painter = painterResource(pair.second), contentDescription = pair.first)
            Text(log.date)
        }
    }
}