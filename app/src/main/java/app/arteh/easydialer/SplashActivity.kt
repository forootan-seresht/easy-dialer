package app.arteh.easydialer

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import app.arteh.easydialer.main.MainActivity
import app.arteh.easydialer.permissions.PermissionActivity
import app.arteh.easydialer.permissions.PermissionChecker

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permissionChecker = PermissionChecker()

        if (
            !permissionChecker.MakeCallPermission(this) ||
            !permissionChecker.ReadPhoneSPermission(this) ||
            !permissionChecker.ReadCallLogPermission(this) ||
            !permissionChecker.WriteCallLogPermission(this) ||
            !permissionChecker.WriteContactPermission(this) ||
            !permissionChecker.ReadContactPermission(this)
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