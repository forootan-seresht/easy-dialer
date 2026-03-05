package app.arteh.easydialer.contacts.edit

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.edit.models.ContactPhone
import app.arteh.easydialer.contacts.edit.models.EditContactAction
import app.arteh.easydialer.contacts.edit.models.EditableContact
import app.arteh.easydialer.contacts.speed.SpeedDialEntry
import app.arteh.easydialer.ui.CustomDialogue
import app.arteh.easydialer.ui.CustomDigButtons
import app.arteh.easydialer.ui.PaddingSides
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor
import app.arteh.easydialer.utility.Holder
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

//todo for phone numbers and other field, by default it is not editable. show click on it to edit it then click on its tick to save it.
//todo because it is also show contact, each number should have button to call and sms
//todo add number should be under all numbers lke delete button
//todo add block contact, share contact, add to favorite
//todo show list of recent log for this contact

@Composable
fun EditScreen(editContactVM: EditContactVM = viewModel(), padding: PaddingSides) {
    val uiState = editContactVM.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
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
        TopRow(editContactVM::saveContact)

        ContactInfo(editContactVM.contact, editContactVM::onAction)

        Button(
            onClick = editContactVM::showAddPhone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add another phone")
        }

        Spacer(Modifier.height(12.dp))

        // Delete Contact
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(25.dp)
                    .noRippleClickable({ editContactVM.showDelete() }),
                painter = painterResource(R.drawable.delete),
                contentDescription = null,
                tint = AppColor.GradRed.resolve()
            )

            Text(text = "Delete", color = AppColor.GradRed.resolve())
        }
    }

    if (uiState.showDelete)
        DigDelete(editContactVM::dismissPopup, { editContactVM.deleteContact(context) })
    if (uiState.showAdd)
        DigAddNumber(editContactVM::dismissPopup, editContactVM::addPhoneNumber)
    if (uiState.showSpeedList)
        DigSpeedDial(
            uiState.speedSlot,
            uiState.speedDialMap,
            editContactVM::dismissPopup,
            editContactVM::updateSpeedSlot
        )
}

@Composable
private fun TopRow(onSaveContact: (Context) -> Unit) {
    val context = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier
                .size(35.dp)
                .padding(5.dp)
                .noRippleClickable({ (context as Activity).finish() }),
            painter = painterResource(R.drawable.back),
            contentDescription = "Back",
            tint = AppColor.Icons.resolve()
        )

        Spacer(Modifier.weight(1f))

        Icon(
            modifier = Modifier
                .size(35.dp)
                .padding(5.dp)
                .noRippleClickable { onSaveContact(context) },
            painter = painterResource(R.drawable.check),
            contentDescription = "Save",
            tint = AppColor.Icons.resolve()
        )
    }
}

@Composable
private fun ContactInfo(
    contact: StateFlow<EditableContact>,
    onAction: (EditContactAction) -> Unit
) {
    val editableContact = contact.collectAsStateWithLifecycle().value

    // Photo
    ContactPhoto(
        photoUri = editableContact.photoUri,
        onPickImage = { onAction(EditContactAction.SetPhoto(it)) }
    )

    OutlinedTextField(
        value = editableContact.firstName,
        onValueChange = { onAction(EditContactAction.UpdateFirstName(it)) },
        label = { Text("First Name") },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
    )

    OutlinedTextField(
        value = editableContact.lastName,
        onValueChange = { onAction(EditContactAction.UpdateLastName(it)) },
        label = { Text("Last Name") },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = editableContact.job,
        onValueChange = { onAction(EditContactAction.UpdateJob(it)) },
        label = { Text("Last Name") },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
    )

    OutlinedTextField(
        value = editableContact.company,
        onValueChange = { onAction(EditContactAction.UpdateCompany(it)) },
        label = { Text("Last Name") },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
    )

    // Phones
    Text("Phone Numbers", fontWeight = FontWeight.Bold)

    editableContact.phones.forEachIndexed { index, phone ->
        ItemPhoneNumber(
            phone,
            { onAction(EditContactAction.UpdatePhone(index, it)) },
            { onAction(EditContactAction.RemovePhone(index)) },
            { onAction(EditContactAction.ShowSpeedDial(index)) }
        )
    }
}

@Composable
fun ContactPhoto(photoUri: Uri?, onPickImage: (Uri?) -> Unit) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onPickImage(uri)
    }

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
            .background(Holder.colors[colorIndex])
            .noRippleClickable { picker.launch("image/*") },
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
                contentDescription = "Contact image",
                tint = Color.White
            )
    }
}

@Composable
private fun ItemPhoneNumber(
    phone: ContactPhone,
    updateNumber: (String) -> Unit,
    removeNumber: () -> Unit,
    showSpeedDial: () -> Unit
) {
    if (!phone.isDeleted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = phone.number,
                onValueChange = { updateNumber(it) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                label = { Text("Phone") }
            )

            IconButton(onClick = removeNumber) {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = "Remove phone",
                    tint = AppColor.GradRed.resolve()
                )
            }

            IconButton(onClick = showSpeedDial) {
                Icon(
                    painter = painterResource(R.drawable.speed),
                    contentDescription = "Speed Dial",
                    tint = AppColor.GradPurple.resolve()
                )
            }
        }
}


@Composable
private fun DigDelete(dismissPopup: () -> Unit, deleteClicked: () -> Unit) {
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
private fun DigAddNumber(dismissPopup: () -> Unit, addClicked: (String) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }

    CustomDialogue(
        Modifier
            .padding(20.dp)
            .fillMaxWidth(), dismissPopup
    ) {
        OutlinedTextField(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth(),
            value = phoneNumber,
            onValueChange = { phoneNumber = it })

        CustomDigButtons(
            "Delete", AppColor.GradRed.resolve(),
            { addClicked(phoneNumber) }, dismissPopup
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigSpeedDial(
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