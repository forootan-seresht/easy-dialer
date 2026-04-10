package app.arteh.easydialer.contacts.show.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.arteh.easydialer.Holder
import app.arteh.easydialer.R
import app.arteh.easydialer.clog.models.SimCard
import app.arteh.easydialer.contacts.edit.models.ContactPhone
import app.arteh.easydialer.contacts.speed.SpeedDialEntry
import app.arteh.easydialer.ui.CustomDialogue
import app.arteh.easydialer.ui.CustomDigButtons
import app.arteh.easydialer.ui.CustomPopup
import app.arteh.easydialer.ui.Divider2
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor

@Composable
internal fun DigDelete(dismissPopup: () -> Unit, deleteClicked: () -> Unit) {
    CustomDialogue(
        Modifier
            .padding(20.dp)
            .fillMaxWidth(), dismissPopup
    ) {
        Text("Are you sure to permanently delete this contact?")
        CustomDigButtons("Delete", AppColor.GradRed.resolve(), deleteClicked, dismissPopup)
    }
}

@Composable
internal fun DigMyNumbers(
    dismissPopup: () -> Unit,
    sims: MutableList<SimCard>,
    onClick: (Int, Boolean) -> Unit
) {
    CustomPopup(dismissPopup) {
        var remember by remember { mutableStateOf(false) }

        Text("Choose SIM for this call", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(10.dp))

        sims.forEachIndexed { index, card ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .noRippleClickable { onClick(index, remember) },
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.sim_card),
                    contentDescription = null,
                    tint = Holder.colors[Holder.colors.size - 1 - index]
                )

                Text(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    text = card.carrier ?: index.toString(),
                )
            }
        }

        Divider2()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(remember, { remember = it })
            Text("Remember this choice")
        }
    }
}

@Composable
internal fun DigContactNumbers(
    dismissPopup: () -> Unit,
    phones: List<ContactPhone>,
    onClick: (Int, Boolean) -> Unit
) {
    val radioIndex = remember {
        var index = 0
        for (i in 0 until phones.size)
            if (phones[i].isDefault) {
                index = i
                break
            }

        return@remember mutableStateOf(index)
    }

    CustomPopup(dismissPopup) {

        Text("Choose phone number", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(10.dp))

        phones.forEachIndexed { index, phone ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .noRippleClickable { radioIndex.value = index },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val selected = radioIndex.value == index
                RadioButton(selected, { radioIndex.value = index })

                Text(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    text = phone.number,
                    style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                )
            }
        }

        Divider2()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 15.dp)
                    .noRippleClickable { onClick(radioIndex.value, false) }, text = "Just once"
            )
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 15.dp)
                    .noRippleClickable { onClick(radioIndex.value, true) }, text = "Always"
            )
        }
    }
}

@Composable
internal fun DigBlockNumbers(
    dismissPopup: () -> Unit,
    phones: List<ContactPhone>,
    onBlock: () -> Unit
) {
    CustomPopup(dismissPopup) {

        Text("You will no longer receive calls or text from", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(10.dp))

        phones.forEach { phone ->
            Text(
                modifier = Modifier.padding(horizontal = 10.dp),
                text = phone.number,
                style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
            )
        }

        CustomDigButtons("Block", AppColor.GradRed.resolve(), onBlock, dismissPopup)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DigSpeedDial(
    selectedSlot: Int,
    speedMap: Map<Int, SpeedDialEntry>,
    dismissPopup: () -> Unit,
    updateSlot: (Int) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = dismissPopup,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .noRippleClickable { updateSlot(-1) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedSlot == -1)
                    Icon(
                        painterResource(R.drawable.check),
                        contentDescription = "Selected",
                        tint = AppColor.GradGreen.resolve()
                    )
                Text(modifier = Modifier.padding(horizontal = 10.dp), text = "None")
            }

            for (i in 0 until 10)
                Row(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .noRippleClickable { updateSlot(i) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedSlot == i)
                        Icon(
                            painterResource(R.drawable.check),
                            contentDescription = "Selected",
                            tint = AppColor.GradGreen.resolve()
                        )
                    Text(modifier = Modifier.padding(horizontal = 15.dp), text = i.toString())
                    Column {
                        if (speedMap[i] != null) {
                            Text(
                                text = speedMap[i]!!.displayName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = speedMap[i]!!.phoneNumber)
                        }
                    }
                }
        }
    }
}