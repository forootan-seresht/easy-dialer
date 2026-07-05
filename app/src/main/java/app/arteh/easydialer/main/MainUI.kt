package app.arteh.easydialer.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.arteh.easydialer.R
import app.arteh.easydialer.clog.CLogScreen
import app.arteh.easydialer.clog.CallLogVM
import app.arteh.easydialer.contacts.list.ContactScreen
import app.arteh.easydialer.contacts.list.MainContent
import app.arteh.easydialer.contacts.list.ContactsVM
import app.arteh.easydialer.dial.DialPadScreen
import app.arteh.easydialer.dial.DialPadVM
import app.arteh.easydialer.ui.PaddingSides
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor

@Composable
fun MainScreen(
    mainVM: MainVM = viewModel(),
    contactsVM: ContactsVM,
    callLogVM: CallLogVM,
    dialPadVM: DialPadVM,
    padding: PaddingSides
) {
    val uiState = mainVM.uiState.collectAsStateWithLifecycle().value

    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                start = padding.start,
                top = padding.top,
                end = padding.end,
                bottom = padding.bottom
            )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (uiState.selectedTab) {
                BottomTab.Contact -> {
                    ContactScreen(contactsVM)
                    contactsVM.load()
                }

                BottomTab.Dial -> DialPadScreen(dialPadVM)

                BottomTab.CallLog -> {
                    CLogScreen(callLogVM)
                    callLogVM.load()
                }
            }
        }
        BottomTabs(uiState.selectedTab) { mainVM.setPage(it) }
    }
}

@Composable
private fun BottomTabs(selectedTab: BottomTab, onClick: (BottomTab) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier
                .height(40.dp)
                .padding(5.dp)
                .weight(1f)
                .noRippleClickable { onClick(BottomTab.Contact) },
            painter = painterResource(R.drawable.contacts),
            contentDescription = stringResource(R.string.contacts),
            tint = if (selectedTab == BottomTab.Contact)
                MaterialTheme.colorScheme.primary
            else AppColor.Gray1.resolve()
        )
        Icon(
            modifier = Modifier
                .height(40.dp)
                .padding(5.dp)
                .weight(1f)
                .noRippleClickable { onClick(BottomTab.Dial) },
            painter = painterResource(R.drawable.dial),
            contentDescription = stringResource(R.string.dial),
            tint = if (selectedTab == BottomTab.Dial)
                MaterialTheme.colorScheme.primary
            else AppColor.Gray1.resolve()
        )
        Icon(
            modifier = Modifier
                .height(40.dp)
                .padding(5.dp)
                .weight(1f)
                .noRippleClickable { onClick(BottomTab.CallLog) },
            painter = painterResource(R.drawable.call_log),
            contentDescription = stringResource(R.string.call_logs),
            tint = if (selectedTab == BottomTab.CallLog)
                MaterialTheme.colorScheme.primary
            else AppColor.Gray1.resolve()
        )
    }
}