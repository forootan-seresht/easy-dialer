package app.arteh.easydialer.contacts.show.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.edit.ContactPhone
import app.arteh.easydialer.contacts.edit.EditContactActivity
import app.arteh.easydialer.contacts.edit.EditableContact
import app.arteh.easydialer.contacts.edit.PhoneType
import app.arteh.easydialer.contacts.speed.SpeedDialEntry
import app.arteh.easydialer.contacts.show.ContactUIAction
import app.arteh.easydialer.contacts.show.ContactVM
import app.arteh.easydialer.contacts.show.ShareChecks
import app.arteh.easydialer.ui.PaddingSides
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor
import app.arteh.easydialer.ui.theme.appTypography
import app.arteh.easydialer.utility.Holder
import app.arteh.easydialer.utility.dialer_hr.DigContactNumbers
import app.arteh.easydialer.utility.dialer_hr.DigMySimCards
import kotlin.random.Random

//todo show list of recent log for this contact

@Composable
fun ShowScreen(contactVM: ContactVM = viewModel(), padding: PaddingSides) {
    val uiState = contactVM.uiState.collectAsStateWithLifecycle().value
    val showState = contactVM.showState.collectAsStateWithLifecycle().value
    val dialerShowState = contactVM.dialerHR.showState.collectAsStateWithLifecycle().value

    val context = LocalContext.current

    if (uiState.contact != null)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColor.BackTrans.resolve())
                .padding(
                    start = padding.start,
                    top = padding.top,
                    end = padding.end,
                    bottom = padding.bottom
                )

                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopRow(uiState.contact.contactID, uiState.contact.isStarred, contactVM::onAction)

            ContactInfo(uiState.contact, uiState.speedDialMap, contactVM::onAction)

            OptionsButtons(contactVM::onAction)
        }

    val dismissPopup = contactVM::dismissPopup

    if (dialerShowState.showMyNumbers)
        DigMySimCards(
            contactVM.dialerHR::dismissPopup,
            contactVM.simCardHR.simCardList,
            contactVM.dialerHR::selectSim
        )
    else if (dialerShowState.showContactNumbers)
        DigContactNumbers(
            contactVM.dialerHR::dismissPopup,
            uiState.contact!!.phones,
            contactVM.dialerHR::selectNumber
        )
    else if (showState.showBlock)
        DigBlockNumbers(uiState.contact!!.phones, dismissPopup)
        { contactVM.onAction(ContactUIAction.BlockNumbers) }
    else if (showState.showDelete)
        DigDelete(dismissPopup) { contactVM.onAction(ContactUIAction.DeleteContact(context)) }
    else if (showState.showSpeedList)
        DigSpeedDial(uiState.speedSlot, uiState.speedDialMap, dismissPopup)
        { contactVM.onAction(ContactUIAction.UpdateSpeedSlot(it)) }
    else if (showState.showShare)
        DigShareContact(
            uiState.contact!!, dismissPopup
        ) { shareChecks: ShareChecks, asFile: Boolean ->
            contactVM.onAction(ContactUIAction.ShareContact(shareChecks, asFile, context))
        }
}

@Composable
private fun QuickButtons(onAction: (ContactUIAction) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center) {
        Icon(
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .size(60.dp)
                .background(AppColor.GradGreen.resolve().copy(alpha = 0.1f), CircleShape)
                .padding(10.dp)
                .noRippleClickable({ onAction(ContactUIAction.ShowMakeCall) }),
            painter = painterResource(R.drawable.call),
            contentDescription = null,
            tint = AppColor.GradGreen.resolve()
        )

        Icon(
            modifier = Modifier
                .size(60.dp)
                .background(AppColor.GradPurple.resolve().copy(alpha = 0.1f), CircleShape)
                .padding(15.dp)
                .noRippleClickable { onAction(ContactUIAction.ShowSendSMS) },
            painter = painterResource(R.drawable.sms),
            contentDescription = null,
            tint = AppColor.GradPurple.resolve()
        )
    }
}

@Composable
private fun TopRow(contactID: Long, isStarred: Boolean, onAction: (ContactUIAction) -> Unit) {
    val context = LocalContext.current

    val editLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result -> onAction(ContactUIAction.ReloadContact) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier
                .size(35.dp)
                .padding(5.dp)
                .noRippleClickable({ (context as Activity).finish() }),
            painter = painterResource(R.drawable.back),
            contentDescription = stringResource(R.string.back),
            tint = AppColor.Icons.resolve()
        )

        Spacer(Modifier.weight(1f))

        Icon(
            modifier = Modifier
                .size(35.dp)
                .padding(5.dp)
                .noRippleClickable { onAction(ContactUIAction.AddFavorite) },
            painter = painterResource(if (isStarred) R.drawable.star else R.drawable.star_empty),
            contentDescription = null,
            tint = if (isStarred) AppColor.GradYoda.resolve() else AppColor.Icons.resolve()
        )
        Icon(
            modifier = Modifier
                .size(35.dp)
                .padding(5.dp)
                .noRippleClickable({
                    val intent = Intent(context, EditContactActivity::class.java)
                    intent.putExtra("id", contactID)
                    editLauncher.launch(intent)
                }),
            painter = painterResource(R.drawable.edit),
            contentDescription = stringResource(R.string.edit),
            tint = AppColor.Icons.resolve()
        )
    }
}

@Composable
private fun ContactInfo(
    contact: EditableContact,
    speedDialMap: Map<Int, SpeedDialEntry>,
    onAction: (ContactUIAction) -> Unit
) {
    ContactPhoto(contact.photoUri)

    Text(contact.fullName, style = MaterialTheme.appTypography.h3)

    if (contact.job.isNotEmpty() || contact.business.isNotEmpty())
        Row {
            if (contact.job.isNotEmpty())
                Text(contact.job)

            if (contact.job.isNotEmpty() && contact.business.isNotEmpty())
                Text(" - ")

            if (contact.business.isNotEmpty())
                Text(contact.business)
        }

    QuickButtons(onAction)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(5.dp))
            .padding(10.dp)
    ) {
        contact.phones.forEachIndexed { index, phone ->
            val isSpeedDial = speedDialMap.values.any { it.phoneId == phone.phoneID }
            ItemPhoneNumber(
                phone,
                isSpeedDial,
                { onAction(ContactUIAction.MakeCall(index)) },
                { onAction(ContactUIAction.SendSMS(index)) },
                { onAction(ContactUIAction.ShowSpeedDial(index)) }
            )
        }
    }

    if (contact.email.isNotEmpty() || contact.note.isNotEmpty())
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(5.dp))
                .padding(10.dp)
        ) {
            if (contact.email.isNotEmpty())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .noRippleClickable({ onAction(ContactUIAction.OpenEmail) }),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .size(25.dp),
                        painter = painterResource(R.drawable.email),
                        contentDescription = stringResource(R.string.email),
                        tint = AppColor.Icons.resolve()
                    )

                    Text(contact.email)
                }

            if (contact.note.isNotEmpty())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                ) {

                    Icon(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .size(25.dp),
                        painter = painterResource(R.drawable.notes),
                        contentDescription = stringResource(R.string.note),
                        tint = AppColor.Icons.resolve()
                    )

                    Text(contact.note)
                }
        }
}

@Composable
private fun OptionsButtons(onAction: (ContactUIAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(5.dp))
            .padding(10.dp)
    ) {

        ItemOption(
            R.drawable.share,
            AppColor.Icons.resolve(),
            stringResource(R.string.share_contact),
            { onAction(ContactUIAction.ShowShareContact) })

        ItemOption(
            R.drawable.block,
            AppColor.GradRed.resolve(),
            stringResource(R.string.block_numbers),
            { onAction(ContactUIAction.ShowBlocK) })

        ItemOption(
            R.drawable.delete,
            AppColor.GradRed.resolve(),
            stringResource(R.string.delete)
        ) { onAction(ContactUIAction.ShowDelete) }
    }
}

@Composable
private fun ItemOption(icon: Int, color: Color, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .noRippleClickable(onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(25.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = color
        )

        Text(modifier = Modifier.padding(start = 10.dp), text = text, color = color)
    }
}

@Composable
fun ContactPhoto(photoUri: Uri?) {
    val colorIndex = remember { Random.nextInt(0, 6) }

    val context = LocalContext.current
    var bitmap by remember(photoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUri) {
        try {
            bitmap =
                photoUri?.let { MediaStore.Images.Media.getBitmap(context.contentResolver, it) }
        } catch (e: Exception) {
        }
    }

    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(Holder.colors[colorIndex]),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null)
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(140.dp),
                contentScale = ContentScale.Crop
            )
        else
            Icon(
                painter = painterResource(R.drawable.person),
                modifier = Modifier.size(80.dp),
                contentDescription = stringResource(R.string.contact_image),
                tint = Color.White
            )
    }
}

@Composable
private fun ItemPhoneNumber(
    phone: ContactPhone,
    isSpeedDial: Boolean,
    onCall: () -> Unit,
    onSMS: () -> Unit,
    onSpeedDial: () -> Unit
) {
    val icon = when (phone.type) {
        PhoneType.Mobile -> R.drawable.mobile
        PhoneType.Home -> R.drawable.home
        PhoneType.Work -> R.drawable.work
        PhoneType.Other -> R.drawable.call
    }

    Row(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .clickable(onClick = onCall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (phone.isDefault)
            Icon(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .size(25.dp),
                painter = painterResource(R.drawable.call),
                contentDescription = null,
                tint = AppColor.Icons.resolve()
            )

        Icon(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .size(25.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = AppColor.Icons.resolve()
        )

        Text(
            modifier = Modifier.padding(horizontal = 10.dp),
            text = phone.number,
            style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
        )

        Spacer(Modifier.weight(1f))

        Icon(
            modifier = Modifier
                .size(40.dp)
                .background(AppColor.GradPurple.resolve().copy(alpha = 0.1f), CircleShape)
                .padding(10.dp)
                .noRippleClickable(onSMS),
            painter = painterResource(R.drawable.sms),
            contentDescription = null,
            tint = AppColor.GradPurple.resolve()
        )

        Icon(
            modifier = Modifier
                .padding(start = 5.dp)
                .size(40.dp)
                .background(
                    if (isSpeedDial) AppColor.GradYoda.resolve()
                    else AppColor.GradYoda.resolve().copy(alpha = 0.1f),
                    CircleShape
                )
                .padding(10.dp)
                .noRippleClickable(onSpeedDial),
            painter = painterResource(R.drawable.speed),
            contentDescription = stringResource(R.string.speed_dial),
            tint = if (isSpeedDial) Color.White else AppColor.GradYoda.resolve()
        )
    }
}
