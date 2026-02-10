package app.arteh.easydialer.contacts.show

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.edit.EditActivity
import app.arteh.easydialer.contacts.show.models.Contact
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor
import app.arteh.easydialer.ui.theme.appTypography

@Composable
fun ContactScreen(contactsVM: ContactsVM) {
    Column(
        Modifier
            .fillMaxSize()
            .background(AppColor.BackTrans.resolve())
    ) {
        SearchBar(contactsVM)
        ContactList(
            contactsVM, Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun SearchBar(contactsVM: ContactsVM) {
    val uiState = contactsVM.uiState.collectAsStateWithLifecycle().value

    Row(
        modifier = Modifier
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .fillMaxWidth()
            .background(AppColor.LayerBack.resolve(), CircleShape)
            .padding(horizontal = 5.dp)
            .noRippleClickable({}),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (uiState.searchText.isEmpty())
            Icon(
                modifier = Modifier
                    .size(35.dp)
                    .padding(5.dp),
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                tint = AppColor.Icons.resolve()
            )
        else
            Icon(
                modifier = Modifier
                    .size(35.dp)
                    .padding(5.dp)
                    .noRippleClickable { contactsVM.updateSearchText("") },
                painter = painterResource(R.drawable.close),
                contentDescription = null,
                tint = AppColor.Icons.resolve()
            )
        TextField(
            modifier = Modifier.weight(1f),
            value = uiState.searchText,
            onValueChange = { contactsVM.updateSearchText(it) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            placeholder = {
                Text(text = "Search Contacts")
            })
        Icon(
            modifier = Modifier
                .size(35.dp)
                .padding(5.dp)
                .noRippleClickable({}),
            painter = painterResource(R.drawable.add_contact),
            contentDescription = "Add Contact",
            tint = AppColor.Icons.resolve()
        )
    }
}

@Composable
private fun ContactList(contactsVM: ContactsVM, modifier: Modifier) {
    val contacts = contactsVM.items.collectAsStateWithLifecycle().value

    LazyColumn(modifier) {
        contacts.forEach { (header, data) ->
            stickyHeader {
                itemHeader(header.char)
            }

            items(data, key = { it.key }) {
                ItemContact(it, header.color, header.char, contactsVM::loadContacts)
            }
        }
    }
}

@Composable
private fun itemHeader(char: Char) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColor.BackTrans.resolve())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = char.toString(),
    )
}

@Composable
private fun ItemContact(contact: Contact, color: Color, char: Char, reload: () -> Unit) {
    val context = LocalContext.current
    var bitmap by remember(contact) { mutableStateOf<ImageBitmap?>(null) }

    val editLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) reload()
    }

    LaunchedEffect(contact) {
        try {
            bitmap =
                contact.thumbUri?.let {
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        it
                    ).asImageBitmap()
                }
        } catch (e: Exception) {
        }
    }

    Row(
        Modifier
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
            .noRippleClickable({
                val intent = Intent(context, EditActivity::class.java)
                intent.putExtra("id", contact.id)
                editLauncher.launch(intent)
            }),
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
                Text(
                    text = char.toString(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

        Column(modifier = Modifier.padding(horizontal = 10.dp)) {
            Text(text = contact.name, style = MaterialTheme.appTypography.h4)
            Text(text = contact.phone, style = MaterialTheme.appTypography.desc)
        }
    }
}