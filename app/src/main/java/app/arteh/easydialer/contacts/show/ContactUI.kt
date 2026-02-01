package app.arteh.easydialer.contacts.show

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.arteh.easydialer.contacts.edit.EditActivity
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.appTypography

@Composable
fun ContactScreen(contactsVM: ContactsVM) {
    val contacts = contactsVM.items.collectAsStateWithLifecycle().value

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(contacts, key = { it.key }) {
                ItemContact(it, contactsVM::loadContacts)
            }
        }
    }
}

@Composable
private fun ItemContact(contact: Contact, reload: () -> Unit) {
    val context = LocalContext.current

    val editLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) reload()
    }

    Row(
        Modifier
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                RoundedCornerShape(5.dp)
            )
            .padding(10.dp)
            .noRippleClickable({
                val intent = Intent(context, EditActivity::class.java)
                intent.putExtra("id", contact.id)
                editLauncher.launch(intent)
            })
    ) {
        Column() {
            Text(text = contact.name, style = MaterialTheme.appTypography.h4)
            Text(text = contact.phone, style = MaterialTheme.appTypography.desc)
        }
    }
}