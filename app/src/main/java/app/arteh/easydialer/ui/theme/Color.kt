package app.arteh.easydialer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppColor(val light: Color, val dark: Color) {
    BackTrans(Color(0xFFF6F8FF), Color(0xFF161618)),
    Gray1(Color(0xFFA2A2A2), Color(0xFFABABAB)),
    Divider(Color(0xFFE8E8E8), Color(0xFF474747)),
    Icons(Color(0xFF868686), Color(0xFFABABAB)),
    GradBlue(Color(0xFF189AE0), Color(0xFF189AE0)),
    GradGreen(Color(0xFF32C5B3), Color(0xFF32C5B3)),
    GradPurple(Color(0xFF7D59FC), Color(0xFF7D59FC)),
    GradRed(Color(0xFFDB395C), Color(0xFFDB395C)),
    GradYoda(Color(0xFFffa751), Color(0xFFffa751)),
    LayerBack(Color(0x88424242), Color(0x88424242)),
    ChartColor(Color(0xFF45b08c), Color(0xFF45b08c)),
    CancelDig(Color(0xFF818181), Color(0xFFC9C9C9)),
    Font(Color(0xFF3D3D3D), Color(0xFFFFFFFF));

    @Composable
    fun resolve() = if (isSystemInDarkTheme()) dark else light
}


//light
val Primary_light = Color(0xFF45b08c)
val Accent_light = Color(0xFF516091)
val background_light = Color(0xFFFAFAFA)
val surface_light = Color(0xFFFFFFFF)
val TitleLight = Color(0xFF212121)
val BodyLight = Color(0xFF3D3D3D)
val DescLight = Color(0xFFA2A2A2)

//dark
val Primary_dark = Color(0xFF45b08c)
val Accent_dark = Color(0xFF7C93DD)
val background_dark = Color(0xFF212121)
val surface_dark = Color(0xFF303030)
val TitleDark = Color(0xFFFFFFFF)
val BodyDark = Color(0xFFFFFFFF)
val DescDark = Color(0xFFABABAB)