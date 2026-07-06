package app.arteh.easydialer.utility.dialer_hr

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import app.arteh.easydialer.R
import app.arteh.easydialer.clog.models.SimCard
import app.arteh.easydialer.contacts.edit.models.ContactPhone
import app.arteh.easydialer.ui.CustomPopup
import app.arteh.easydialer.ui.Divider2
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.utility.Holder

@Composable
fun DigMySimCards(
    dismissPopup: () -> Unit,
    sims: MutableList<SimCard>,
    onClick: (Int, Boolean) -> Unit
) {
    CustomPopup(dismissPopup) {
        var remember by remember { mutableStateOf(false) }

        Text(stringResource(R.string.choose_sim), fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(10.dp))

        sims.forEachIndexed { index, card ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
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
            Text(stringResource(R.string.remember_this_choice))
        }
    }
}

@Composable
fun DigContactNumbers(
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

        return@remember mutableIntStateOf(index)
    }

    CustomPopup(dismissPopup) {

        Text(stringResource(R.string.choose_phone_number), fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(10.dp))

        phones.forEachIndexed { index, phone ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .noRippleClickable { radioIndex.intValue = index },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val selected = radioIndex.intValue == index
                RadioButton(selected, { radioIndex.intValue = index })

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
                    .noRippleClickable { onClick(radioIndex.intValue, false) },
                text = stringResource(R.string.just_once)
            )
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 15.dp)
                    .noRippleClickable { onClick(radioIndex.intValue, true) },
                text = stringResource(R.string.always)
            )
        }
    }
}