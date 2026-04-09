package app.arteh.easydialer.permissions

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

enum class PermissionType {
    General,
    Miui,
    DefDialer,
}

data class PermissionRow(
    val title: Int,
    val body: Int,
    val permission: String,
    val type: PermissionType = PermissionType.General,
    val checkerFunction: (Context) -> Boolean,
    var isVisible: MutableState<Boolean> = mutableStateOf(false)
)