package app.arteh.easydialer.clog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.arteh.easydialer.R
import app.arteh.easydialer.clog.models.Clog

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
    Row(
        Modifier
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                RoundedCornerShape(5.dp)
            )
            .padding(10.dp)
    ) {
        Column(Modifier.weight(1f)) {
            if (log.contact == null)
                Text(text = "Unknown")
            else
                Text(text = log.contact.name, fontWeight = FontWeight.Bold)

            Text(text = log.number)
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