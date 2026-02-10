package app.arteh.easydialer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import app.arteh.easydialer.main.MainActivity
import app.arteh.easydialer.permissions.PermissionActivity
import app.arteh.easydialer.permissions.PermissionChecker

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (
            !PermissionChecker.MakeCallPermission(this) ||
            !PermissionChecker.ReadPhoneSPermission(this) ||
            !PermissionChecker.ReadCallLogPermission(this) ||
            !PermissionChecker.WriteCallLogPermission(this) ||
            !PermissionChecker.WriteContactPermission(this) ||
            !PermissionChecker.ReadContactPermission(this)
        ) {
            val intent = Intent(this, PermissionActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}