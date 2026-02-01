package app.arteh.easydialer.contacts.edit

import android.app.Activity
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
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.edit.models.ContactPhone
import app.arteh.easydialer.contacts.speed.SpeedDialEntry
import app.arteh.easydialer.ui.CustomDialogue
import app.arteh.easydialer.ui.CustomDigButtons
import app.arteh.easydialer.ui.PaddingSides
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor

@Composable
fun EditScreen(editVM: EditVM, padding: PaddingSides) {
    val editableContact = editVM.contact.collectAsStateWithLifecycle().value
    val uiState = editVM.uiState.collectAsStateWithLifecycle().value
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
                    .noRippleClickable({ editVM.saveContact(context) }),
                painter = painterResource(R.drawable.check),
                contentDescription = "Save",
                tint = AppColor.Icons.resolve()
            )
        }
        // Photo
        ContactPhoto(
            photoUri = editableContact.photoUri,
            onPickImage = editVM::setPhoto
        )

        if (editableContact.firstName.isNotEmpty() && editableContact.lastName.isNotEmpty()) {
            OutlinedTextField(
                value = editableContact.firstName,
                onValueChange = editVM::updateFirstName,
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
            )

            OutlinedTextField(
                value = editableContact.lastName,
                onValueChange = editVM::updateLastName,
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
            )
        }
        else
            OutlinedTextField(
                value = editableContact.fullName,
                onValueChange = editVM::updateName,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
            )

        // Phones
        Text("Phone Numbers", fontWeight = FontWeight.Bold)

        editableContact.phones.forEachIndexed { index, phone ->
            ItemPhoneNumber(
                index,
                phone,
                editVM::updatePhone,
                editVM::removePhone,
                editVM::showSpeedDial
            )
        }

        Button(
            onClick = editVM::showAddPhone,
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
                    .noRippleClickable({ editVM.showDelete() }),
                painter = painterResource(R.drawable.delete),
                contentDescription = null,
                tint = AppColor.GradRed.resolve()
            )

            Text(text = "Delete", color = AppColor.GradRed.resolve())
        }
    }

    if (uiState.showDelete)
        DigDelete(editVM::dismissPopup, { editVM.deleteContact(context) })
    if (uiState.showAdd)
        DigAddNumber(editVM::dismissPopup, editVM::addPhoneNumber)
    if (uiState.showSpeedList)
        DigSpeedDial(
            uiState.speedSlot,
            uiState.speedDialMap,
            editVM::dismissPopup,
            editVM::updateSpeedSlot
        )
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

@Composable
private fun ItemPhoneNumber(
    index: Int,
    phone: ContactPhone,
    updateNumber: (Int, String) -> Unit,
    removeNumber: (Int) -> Unit,
    showSpeedDial: (Int) -> Unit
) {
    if (!phone.isDeleted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = phone.number,
                onValueChange = { updateNumber(index, it) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                label = { Text("Phone") }
            )

            IconButton(onClick = { removeNumber(index) }) {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = "Remove phone",
                    tint = AppColor.GradRed.resolve()
                )
            }

            IconButton(onClick = { showSpeedDial(index) }) {
                Icon(
                    painter = painterResource(R.drawable.speed),
                    contentDescription = "Speed Dial",
                    tint = AppColor.GradPurple.resolve()
                )
            }
        }
}

@Composable
fun ContactPhoto(photoUri: Uri?, onPickImage: (Uri?) -> Unit) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onPickImage(uri)
    }

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
            .background(Color.LightGray.copy(alpha = 0.5f))
            .noRippleClickable({ picker.launch("image/*") }),
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
                contentDescription = "Contact image"
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