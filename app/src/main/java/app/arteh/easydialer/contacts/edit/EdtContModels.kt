package app.arteh.easydialer.contacts.edit

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import app.arteh.easydialer.R
import app.arteh.easydialer.contacts.models.SpeedDialEntry
import com.image.cropview.CropType

data class EdtContUIState(
    val contact: EditableContact = EditableContact(),
    val showAdd: Boolean = false,
    val showDelete: Boolean = false,
    val showSpeedList: Boolean = false,
    val speedSlot: Int = -1,
    val speedDialMap: Map<Int, SpeedDialEntry> = emptyMap(),
    val phoneNumber: String = "",
    val cropState: CropState = CropState()
)

data class CropState(
    val isCropping: Boolean = false,
    val croppingImage: Bitmap? = null,
    val cropType: CropType = CropType.PROFILE_CIRCLE,
    val rotation: Float = 0f
)

data class EditableContact(
    val contactID: Long = 0,
    val rawContactID: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val job: String = "",
    val business: String = "",
    val email: String = "",
    val note: String = "",
    val fullName: String = "",
    val isStarred: Boolean = false,
    val phones: List<ContactPhone> = listOf(),
    val photoUri: Uri? = null,
    val defaultSimID: Int = -1,
)

data class ContactPhone(
    val phoneID: Long = 0,
    val number: String,
    val type: PhoneType,
    val isBLocked: Boolean = false,
    val isDeleted: Boolean = false,
    val isDefault: Boolean = false,
    val defaultSimID: Int = -1,
)

enum class PhoneType(val fullName: Int, val icon: Int) {
    Mobile(R.string.mobile, R.drawable.mobile),
    Home(R.string.home, R.drawable.home),
    Work(R.string.work, R.drawable.work),
    Other(R.string.other, R.drawable.call)
}

sealed interface EditContactAction {
    data object ShowAddPhone : EditContactAction
    data class SetPhoto(val uri: Uri?) : EditContactAction
    data class UpdateFirstName(val name: String) : EditContactAction
    data class UpdateLastName(val lastName: String) : EditContactAction
    data class UpdateJob(val job: String) : EditContactAction
    data class UpdateBusiness(val company: String) : EditContactAction
    data class UpdateEmail(val email: String) : EditContactAction
    data class UpdateNote(val note: String) : EditContactAction
    data class UpdatePhone(val index: Int, val phone: String) : EditContactAction
    data class RemovePhone(val index: Int) : EditContactAction
    data class ChangeType(val index: Int, val type: PhoneType) : EditContactAction

    data class UpdatePhoneNumber(val number: String) : EditContactAction
    data class AddNumber(val type: PhoneType) : EditContactAction

    data object DismissPopup : EditContactAction
}

sealed interface ImageCropAction {
    data object CancelCrop : ImageCropAction
    data object DeleteBanner : ImageCropAction

    data class BannerSelected(val uri: Uri, val context: Context) : ImageCropAction
    data class ProfileImageSelected(val uri: Uri, val context: Context) : ImageCropAction
    data class RotateImage(val isClockWise: Boolean) : ImageCropAction

    data class SaveCroppedImage(val cropped: Bitmap, val context: Context) : ImageCropAction
}