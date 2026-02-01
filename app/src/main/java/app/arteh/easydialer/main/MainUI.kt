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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.arteh.easydialer.R
import app.arteh.easydialer.clog.CLogScreen
import app.arteh.easydialer.clog.CLogVM
import app.arteh.easydialer.contacts.show.ContactScreen
import app.arteh.easydialer.contacts.show.ContactsVM
import app.arteh.easydialer.dial.DialPadScreen
import app.arteh.easydialer.dial.DialPadVM
import app.arteh.easydialer.ui.Divider2
import app.arteh.easydialer.ui.PaddingSides
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor

private lateinit var mainVM: MainVM

@Composable
fun MainScreen(
    vm: MainVM = viewModel(),
    contactsVM: ContactsVM,
    cLogVM: CLogVM,
    dialPadVM: DialPadVM,
    padding: PaddingSides
) {
    mainVM = vm
    val uiState = vm.uiState.collectAsStateWithLifecycle().value

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
                    CLogScreen(cLogVM)
                    cLogVM.load()
                }
            }
        }
        Divider2()
        BottomTabs(uiState.selectedTab)
    }
}

@Composable
private fun BottomTabs(selectedTab: BottomTab) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier
                .height(40.dp)
                .weight(1f)
                .noRippleClickable({ mainVM.setPage(BottomTab.Contact) }),
            painter = painterResource(R.drawable.contacts),
            contentDescription = "Contact",
            tint = if (selectedTab == BottomTab.Contact)
                MaterialTheme.colorScheme.primary
            else AppColor.Gray1.resolve()
        )
        Icon(
            modifier = Modifier
                .height(40.dp)
                .weight(1f)
                .noRippleClickable({ mainVM.setPage(BottomTab.Dial) }),
            painter = painterResource(R.drawable.dial),
            contentDescription = "Dial",
            tint = if (selectedTab == BottomTab.Dial)
                MaterialTheme.colorScheme.primary
            else AppColor.Gray1.resolve()
        )
        Icon(
            modifier = Modifier
                .height(40.dp)
                .weight(1f)
                .noRippleClickable({ mainVM.setPage(BottomTab.CallLog) }),
            painter = painterResource(R.drawable.call_log),
            contentDescription = "Call log",
            tint = if (selectedTab == BottomTab.CallLog)
                MaterialTheme.colorScheme.primary
            else AppColor.Gray1.resolve()
        )
    }
}