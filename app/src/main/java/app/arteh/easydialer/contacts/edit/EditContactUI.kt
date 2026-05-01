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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.edit.models.ContactPhone
import app.arteh.easydialer.contacts.edit.models.EditContactAction
import app.arteh.easydialer.contacts.edit.models.EditableContact
import app.arteh.easydialer.contacts.edit.models.PhoneType
import app.arteh.easydialer.ui.CustomDigButtons
import app.arteh.easydialer.ui.CustomPopup
import app.arteh.easydialer.ui.PaddingSides
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor
import app.arteh.easydialer.utility.Holder
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

@Composable
fun EditScreen(editContactVM: EditContactVM = viewModel(), padding: PaddingSides) {
    val uiState = editContactVM.uiState.collectAsStateWithLifecycle().value

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
        TopRow(editContactVM::saveContact)

        ContactInfo(editContactVM.contact, editContactVM::onAction)
    }

    if (uiState.showAdd)
        DigAddNumber(editContactVM::dismissPopup, editContactVM::addPhoneNumber)
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
            contentDescription = stringResource(R.string.back),
            tint = AppColor.Icons.resolve()
        )

        Spacer(Modifier.weight(1f))

        Icon(
            modifier = Modifier
                .size(35.dp)
                .padding(5.dp)
                .noRippleClickable { onSaveContact(context) },
            painter = painterResource(R.drawable.check),
            contentDescription = stringResource(R.string.save_changes),
            tint = MaterialTheme.colorScheme.primary
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
        label = { Text(stringResource(R.string.first_name)) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = null
            )
        },
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = AppColor.Gray1.resolve()),
    )

    OutlinedTextField(
        value = editableContact.lastName,
        onValueChange = { onAction(EditContactAction.UpdateLastName(it)) },
        label = { Text(stringResource(R.string.last_name)) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = null
            )
        },
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = AppColor.Gray1.resolve()),
    )

    Spacer(modifier = Modifier.height(10.dp))

    NumberSection(editableContact, onAction)

    Spacer(modifier = Modifier.height(10.dp))

    DetailsSection(editableContact, onAction)
}

@Composable
private fun ContactPhoto(photoUri: Uri?, onPickImage: (Uri?) -> Unit) {
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
                contentDescription = stringResource(R.string.contact_image),
                tint = Color.White
            )
    }
}

@Composable
private fun DetailsSection(
    editableContact: EditableContact, onAction: (EditContactAction) -> Unit
) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.details),
        fontWeight = FontWeight.SemiBold
    )

    OutlinedTextField(
        value = editableContact.job,
        onValueChange = { onAction(EditContactAction.UpdateJob(it)) },
        label = { Text(stringResource(R.string.job_title)) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = null
            )
        },
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = AppColor.Gray1.resolve()),
    )

    OutlinedTextField(
        value = editableContact.company,
        onValueChange = { onAction(EditContactAction.UpdateCompany(it)) },
        label = { Text(stringResource(R.string.company)) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = null
            )
        },
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = AppColor.Gray1.resolve()),
    )

    OutlinedTextField(
        value = editableContact.email,
        onValueChange = { onAction(EditContactAction.UpdateEmail(it)) },
        label = { Text(stringResource(R.string.email_address)) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.email),
                contentDescription = null
            )
        },
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = AppColor.Gray1.resolve()),
    )

    OutlinedTextField(
        value = editableContact.note,
        onValueChange = { onAction(EditContactAction.UpdateNote(it)) },
        label = { Text(stringResource(R.string.note)) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.notes),
                contentDescription = null
            )
        },
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = AppColor.Gray1.resolve()),
    )

    Spacer(modifier = Modifier.height(15.dp))
}

@Composable
private fun NumberSection(editableContact: EditableContact, onAction: (EditContactAction) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.phone_numbers),
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = { onAction(EditContactAction.ShowAddPhone) }) {
            Icon(
                painterResource(R.drawable.add),
                contentDescription = stringResource(R.string.add_phone),
                tint = AppColor.Icons.resolve()
            )
        }
    }

    editableContact.phones.forEachIndexed { index, phone ->
        ItemPhoneNumber(
            phone,
            { onAction(EditContactAction.UpdatePhone(index, it)) },
            { onAction(EditContactAction.RemovePhone(index)) },
            { onAction(EditContactAction.ChangeType(index, it)) },
        )
    }
}

@Composable
private fun ItemPhoneNumber(
    phone: ContactPhone,
    updateNumber: (String) -> Unit,
    removeNumber: () -> Unit,
    changeType: (PhoneType) -> Unit,
) {
    if (!phone.isDeleted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            PhoneTypeDropdown(phone.type, changeType)

            OutlinedTextField(
                value = phone.number,
                onValueChange = { updateNumber(it) },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(phone.type.fullName)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone
                ),
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = AppColor.Gray1.resolve()),
            )

            IconButton(onClick = removeNumber) {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = stringResource(R.string.remove_phone),
                    tint = AppColor.GradRed.resolve()
                )
            }
        }
}

@Composable
fun PhoneTypeDropdown(currentType: PhoneType, changeType: (PhoneType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.noRippleClickable { expanded = true }) {
        Icon(
            modifier = Modifier.padding(horizontal = 5.dp),
            painter = painterResource(currentType.icon),
            contentDescription = null,
            tint = AppColor.Icons.resolve()
        )
        DropdownMenu(
            expanded = expanded,
            { expanded = false },
            Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            PhoneType.entries.forEach {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                modifier = Modifier.padding(end = 10.dp),
                                painter = painterResource(it.icon),
                                contentDescription = stringResource(it.fullName),
                                tint = AppColor.Icons.resolve()
                            )

                            Text(text = stringResource(it.fullName))
                        }
                    },
                    onClick = { changeType(it); expanded = false })
            }
        }
    }
}

@Composable
private fun DigAddNumber(dismissPopup: () -> Unit, onAddClicked: (String, PhoneType) -> Unit) {
    var type by remember { mutableStateOf(PhoneType.Mobile) }
    var phoneNumber by remember { mutableStateOf("") }

    CustomPopup(dismissPopup) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth(),
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(stringResource(R.string.phone_number)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone
                ),
            )

            PhoneTypeDropdown(type) { type = it }
        }

        CustomDigButtons(
            stringResource(R.string.add), AppColor.GradGreen.resolve(),
            { onAddClicked(phoneNumber, type) }, dismissPopup
        )
    }
}