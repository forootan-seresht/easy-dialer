package app.arteh.easydialer.main

import android.app.Application
import android.app.role.RoleManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.arteh.easydialer.clog.CLogVM
import app.arteh.easydialer.contacts.ContactRP
import app.arteh.easydialer.contacts.show.ContactsVM
import app.arteh.easydialer.dial.DialPadVM
import app.arteh.easydialer.ui.EdgePadding
import app.arteh.easydialer.ui.theme.EasyDialer

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkRole()
        val appContext = this.applicationContext as Application
        val contactRP = ContactRP(appContext)

        val contactsVM: ContactsVM by viewModels {
            ContactsVM.Factory(appContext, contactRP)
        }
        val cLogVM: CLogVM by viewModels {
            CLogVM.Factory(appContext, contactRP)
        }
        val dialPadVM: DialPadVM by viewModels {
            DialPadVM.Factory(appContext, contactRP)
        }

        enableEdgeToEdge()

        setContent {
            EasyDialer {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        contactsVM = contactsVM,
                        cLogVM = cLogVM,
                        dialPadVM = dialPadVM,
                        padding = EdgePadding(innerPadding, extra = 0.dp)
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                println("OK")
            }
            else {
                println("Failed")
            }
        }
//        when (requestCode) {
//            REQUEST_CODE_SET_DEFAULT_DIALER -> checkSetDefaultDialerResult(resultCode)
//        }
    }

    fun checkRole() {
        val roleManager = getSystemService(RoleManager::class.java)

        if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
            val intent =
                roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            startActivityForResult(intent, 1)
        }
    }
}