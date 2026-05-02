package app.arteh.easydialer.dial

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.arteh.easydialer.R
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor

@Composable
internal fun BigDialer(number: String, onAction: (DialAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DialedNumberDisplay(number) { onAction(DialAction.BackSpace) }

        BigDialPadGrid(
            onNumberClick = { onAction(DialAction.NumberCLicked(it)) },
            onNumberLongPress = { onAction(DialAction.NumberLongCLicked(it)) }
        )

        CallControls(onCall = { onAction(DialAction.ShowMakeCall(number)) })
    }
}

@Composable
private fun DialedNumberDisplay(number: String, onBackspace: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = number.ifEmpty { "—" },
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Icon(
            modifier = Modifier
                .size(50.dp)
                .noRippleClickable(onBackspace),
            painter = painterResource(R.drawable.backspace),
            contentDescription = stringResource(R.string.clear),
            tint = AppColor.GradYoda.resolve()
        )
    }
}

@Composable
fun BigDialPadGrid(onNumberClick: (String) -> Unit, onNumberLongPress: (String) -> Unit) {
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
            .size(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(getDialColor(number))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun CallControls(onCall: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .height(70.dp)
            .width(150.dp)
            .background(AppColor.GradGreen.resolve(), RoundedCornerShape(5.dp))
            .noRippleClickable(onCall),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(50.dp),
            painter = painterResource(R.drawable.call),
            contentDescription = stringResource(R.string.call),
            tint = Color.White
        )
    }
}

private fun getDialColor(number: String): Color =
    when (number) {
        "1" -> Color(0xFF1E88E5)
        "2" -> Color(0xFF57D45D)
        "3" -> Color(0xFFFF4C49)
        "4" -> Color(0xFFFB8C00)
        "5" -> Color(0xFFFF33D3)
        "6" -> Color(0xFF3949AB)
        "7" -> Color(0xFF8647FF)
        "8" -> Color(0xFF009E8B)
        "9" -> Color(0xFFFF1F70)
        "0" -> Color(0xFF546E7A)
        "*" -> Color.DarkGray
        "#" -> Color.Gray
        else -> Color.Black
    }