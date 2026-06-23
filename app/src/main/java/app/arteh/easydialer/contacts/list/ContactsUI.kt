package app.arteh.easydialer.contacts.list

import android.provider.MediaStore
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.Contact
import app.arteh.easydialer.contacts.ContactHeader
import app.arteh.easydialer.contacts.list.models.ContactAction
import app.arteh.easydialer.dialer.DigMySimCards
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor
import app.arteh.easydialer.ui.theme.appTypography

@Composable
fun ContactScreen(contactsVM: ContactsVM) {
    val uiState by contactsVM.uiState.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(AppColor.BackTrans.resolve())
    ) {
        SearchBar(uiState.searchText, contactsVM)

        FavoriteList(uiState.favorites, contactsVM::onAction)

        ContactList(
            uiState.contactList,
            contactsVM, Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun SearchBar(searchText: String, contactsVM: ContactsVM) {
    val dialerShowState by contactsVM.dialerHR.showState.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .fillMaxWidth()
            .background(AppColor.SearchBack.resolve(), CircleShape)
            .padding(horizontal = 5.dp)
            .noRippleClickable({}),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (searchText.isEmpty())
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
            value = searchText,
            onValueChange = { contactsVM.updateSearchText(it) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            placeholder = {
                Text(text = stringResource(R.string.search_contacts))
            })

        if (searchText.isEmpty())
            Icon(
                modifier = Modifier
                    .size(35.dp)
                    .padding(5.dp)
                    .noRippleClickable(contactsVM::goAddContact),
                painter = painterResource(R.drawable.add_contact),
                contentDescription = stringResource(R.string.add_contact),
                tint = AppColor.Icons.resolve()
            )
    }

    if (dialerShowState.showMyNumbers)
        DigMySimCards(
            contactsVM.dialerHR::dismissPopup,
            contactsVM.simCardHR.simCardList,
            contactsVM.dialerHR::selectSim
        )
}

@Composable
private fun FavoriteList(favorites: List<Contact>, onAction: (ContactAction) -> Unit) {
    if (favorites.isEmpty()) return

    Text(
        modifier = Modifier.padding(15.dp),
        text = "Favorites",
        style = MaterialTheme.appTypography.h4
    )
    LazyRow(modifier = Modifier.padding(horizontal = 15.dp)) {
        items(favorites) { contact ->
            ItemFavContact(contact) { onAction(ContactAction.ShowContact(contact.id)) }
        }
    }
}

@Composable
private fun ItemFavContact(contact: Contact, onClicked: () -> Unit) {
    val grayColor = AppColor.UnContactBack.resolve()

    val context = LocalContext.current
    var bitmap by remember(contact) { mutableStateOf<ImageBitmap?>(null) }

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

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (bitmap != null)
            Image(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .noRippleClickable(onClicked),
                bitmap = bitmap!!,
                contentDescription = null
            )
        else
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(grayColor, CircleShape)
                    .noRippleClickable(onClicked),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name[0].toString(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

        Text(
            modifier = Modifier.padding(top = 5.dp),
            text = contact.name,
            style = MaterialTheme.appTypography.h4
        )
    }
}

@Composable
private fun ContactList(
    contacts: Map<ContactHeader, List<Contact>>,
    contactsVM: ContactsVM,
    modifier: Modifier
) {
    LazyColumn(modifier) {
        contacts.forEach { (header, data) ->
            stickyHeader {
                ItemHeader(header.char)
            }

            items(data, key = { it.key }) {
                ItemContact(it, header.color, header.char, contactsVM::onAction)
            }
        }
    }
}

@Composable
private fun ItemHeader(char: Char) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColor.BackTrans.resolve())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = char.toString(),
        style = MaterialTheme.appTypography.h4
    )
}

@Composable
private fun ItemContact(
    contact: Contact, color: Color, char: Char, onAction: (ContactAction) -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember(contact) { mutableStateOf<ImageBitmap?>(null) }

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
            .noRippleClickable { onAction(ContactAction.ShowMakeCall(contact)) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (bitmap != null)
            Image(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .noRippleClickable { onAction(ContactAction.ShowContact(contact.id)) },
                bitmap = bitmap!!,
                contentDescription = null
            )
        else
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(color, CircleShape)
                    .noRippleClickable { onAction(ContactAction.ShowContact(contact.id)) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char.toString(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .weight(1f)
        ) {
            Text(text = contact.name, style = MaterialTheme.appTypography.h4)
            Text(
                text = contact.phone,
                style = LocalTextStyle.current.copy(
                    textDirection = TextDirection.Ltr,
                    color = AppColor.Desc.resolve()
                )
            )
        }

        Icon(
            modifier = Modifier
                .padding(end = 10.dp)
                .size(40.dp)
                .background(AppColor.GradPurple.resolve().copy(alpha = 0.1f), CircleShape)
                .padding(10.dp)
                .noRippleClickable({ onAction(ContactAction.ShowSendSMS(contact)) }),
            painter = painterResource(R.drawable.sms),
            contentDescription = null,
            tint = AppColor.GradPurple.resolve()
        )
    }
}