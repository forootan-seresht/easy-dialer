package app.arteh.easydialer.contacts.show.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.edit.ContactPhone
import app.arteh.easydialer.contacts.edit.EditableContact
import app.arteh.easydialer.contacts.models.SpeedDialEntry
import app.arteh.easydialer.contacts.show.ShareChecks
import app.arteh.easydialer.ui.CustomDialogue
import app.arteh.easydialer.ui.CustomDigButtons
import app.arteh.easydialer.ui.CustomPopup
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor

@Composable
internal fun DigDelete(dismissPopup: () -> Unit, deleteClicked: () -> Unit) {
    CustomDialogue(
        Modifier
            .padding(20.dp)
            .fillMaxWidth(), dismissPopup
    ) {
        Text(stringResource(R.string.sure_delete))
        CustomDigButtons(
            stringResource(R.string.delete),
            AppColor.GradRed.resolve(),
            deleteClicked,
            dismissPopup
        )
    }
}

@Composable
internal fun DigBlockNumbers(
    phones: List<ContactPhone>, dismissPopup: () -> Unit, onBlock: () -> Unit
) {
    CustomPopup(dismissPopup) {

        Text(stringResource(R.string.no_longer), fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(10.dp))

        phones.forEach { phone ->
            Text(
                modifier = Modifier.padding(horizontal = 10.dp),
                text = phone.number,
                style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
            )
        }

        CustomDigButtons(
            stringResource(R.string.block),
            AppColor.GradRed.resolve(),
            onBlock,
            dismissPopup
        )
    }
}

@Composable
internal fun DigUnblockNumbers(
    phones: List<ContactPhone>, dismissPopup: () -> Unit, onUnblock: () -> Unit
) {
    CustomPopup(dismissPopup) {

        Text(stringResource(R.string.unblock), fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(10.dp))

        phones.forEach { phone ->
            Text(
                modifier = Modifier.padding(horizontal = 10.dp),
                text = phone.number,
                style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
            )
        }

        CustomDigButtons(
            stringResource(R.string.unblock),
            AppColor.GradGreen.resolve(),
            onUnblock,
            dismissPopup
        )
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
    val numbers = remember { listOf<Int?>(1, 2, 3, 4, 5, 6, 7, 8, 9, null, 0, null) }

    ModalBottomSheet(
        onDismissRequest = dismissPopup,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(
                text = stringResource(R.string.speed_dial),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .noRippleClickable { updateSlot(-1) },
                shape = RoundedCornerShape(12.dp),
                color = if (selectedSlot == -1)
                    AppColor.GradGreen.resolve().copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = if (selectedSlot == -1) BorderStroke(1.5.dp, AppColor.GradGreen.resolve()) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (selectedSlot == -1) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = stringResource(R.string.save_changes),
                            tint = AppColor.GradGreen.resolve(),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = stringResource(R.string.none),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                items(numbers) { i ->
                    if (i == null) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                    } else {
                        val isSelected = selectedSlot == i
                        val entry = speedMap[i]

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .noRippleClickable { updateSlot(i) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected)
                                AppColor.GradGreen.resolve().copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) BorderStroke(
                                1.5.dp,
                                AppColor.GradGreen.resolve()
                            ) else null
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = i.toString(),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            painter = painterResource(R.drawable.check),
                                            contentDescription = stringResource(R.string.save_changes),
                                            tint = AppColor.GradGreen.resolve(),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                if (entry != null) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = entry.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = entry.phoneNumber,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DigShareContact(
    contact: EditableContact,
    dismissPopup: () -> Unit,
    onShare: (shareChecks: ShareChecks, asFile: Boolean) -> Unit
) {
    var shareChecks by remember { mutableStateOf(ShareChecks()) }

    ModalBottomSheet(
        onDismissRequest = dismissPopup,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            Modifier
                .padding(15.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(shareChecks.name, {}, enabled = false)
                Text(stringResource(R.string.include_name))
            }
            Text(
                modifier = Modifier
                    .padding(start = 50.dp)
                    .alpha(if (shareChecks.name) 1f else 0.5f),
                text = contact.fullName
            )

            Spacer(Modifier.height(10.dp))

            if (contact.phones.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(shareChecks.phones, { shareChecks = shareChecks.copy(phones = it) })
                    Text(stringResource(R.string.include_number_s))
                }

                for (phone in contact.phones) {
                    Row(
                        modifier = Modifier.padding(top = 5.dp, start = 40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.padding(horizontal = 5.dp),
                            painter = painterResource(phone.type.icon),
                            contentDescription = stringResource(phone.type.fullName)
                        )
                        Text(
                            modifier = Modifier.alpha(if (shareChecks.phones) 1f else 0.5f),
                            text = phone.number
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
            }

            if (contact.email.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(shareChecks.email, { shareChecks = shareChecks.copy(email = it) })
                    Text(stringResource(R.string.include_email))
                }

                Text(
                    modifier = Modifier
                        .padding(start = 50.dp)
                        .alpha(if (shareChecks.email) 1f else 0.5f),
                    text = contact.email
                )

                Spacer(Modifier.height(10.dp))
            }

            if (contact.job.isNotEmpty() || contact.business.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        shareChecks.jobCompany,
                        { shareChecks = shareChecks.copy(jobCompany = it) })
                    Text(stringResource(R.string.include_job_company))
                }

                Text(
                    modifier = Modifier
                        .padding(start = 50.dp)
                        .alpha(if (shareChecks.jobCompany) 1f else 0.5f),
                    text = "${contact.business} - ${contact.job}"
                )

                Spacer(Modifier.height(10.dp))
            }

            if (contact.note.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(shareChecks.note, { shareChecks = shareChecks.copy(note = it) })
                    Text(stringResource(R.string.include_note))
                }

                Text(
                    modifier = Modifier
                        .padding(start = 50.dp)
                        .alpha(if (shareChecks.note) 1f else 0.5f),
                    text = contact.note
                )

                Spacer(Modifier.height(10.dp))
            }

            //save buttons
            Row {
                Row(
                    Modifier
                        .padding(horizontal = 10.dp)
                        .weight(1f)
                        .background(AppColor.GradBlue.resolve(), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                        .noRippleClickable { onShare(shareChecks, false) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painterResource(R.drawable.paste),
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(stringResource(R.string.share_as_text), color = Color.White)
                }

                Row(
                    Modifier
                        .padding(horizontal = 10.dp)
                        .weight(1f)
                        .background(AppColor.GradBlue.resolve(), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                        .noRippleClickable { onShare(shareChecks, true) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painterResource(R.drawable.file_share),
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(stringResource(R.string.share_as_file), color = Color.White)
                }
            }
        }
    }
}