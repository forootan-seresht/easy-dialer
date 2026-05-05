package app.arteh.easydialer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.arteh.easydialer.R

data class AppTypography(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val bodyBold: TextStyle,
    val bold3: TextStyle,
    val desc: TextStyle,
    val body18: TextStyle,
    val body14: TextStyle,
    val desc13: TextStyle,
    val cancel: TextStyle,
    val ok: TextStyle,
    val normwhite: TextStyle,
)

val fontFamily = FontFamily(Font(R.font.vazir))

val LightAppTypography = AppTypography(
    h1 = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = TitleLight,
        fontFamily = fontFamily
    ),
    h2 = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = TitleLight,
        fontFamily = fontFamily
    ),
    h3 = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TitleLight,
        fontFamily = fontFamily
    ),
    h4 = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = TitleLight,
        fontFamily = fontFamily
    ),
    bold3 = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = BodyLight,
        fontFamily = fontFamily
    ),
    desc = TextStyle(
        fontSize = 14.sp,
        color = DescLight,
        fontFamily = fontFamily
    ),
    bodyBold = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = BodyLight,
        fontFamily = fontFamily
    ),
    body18 = TextStyle(
        fontSize = 18.sp,
        color = BodyLight,
        fontFamily = fontFamily
    ),
    cancel = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = AppColor.CancelDig.light,
        fontFamily = fontFamily
    ),
    ok = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = AppColor.GradGreen.light,
        fontFamily = fontFamily
    ),
    normwhite = TextStyle(
        fontSize = 16.sp,
        color = Color(0xFFFFFFFF),
        fontFamily = fontFamily
    ),
    desc13 = TextStyle(
        fontSize = 13.sp,
        color = DescLight,
        fontFamily = fontFamily
    ),
    body14 = TextStyle(
        fontSize = 14.sp,
        color = BodyLight,
        fontFamily = fontFamily
    ),
)

val DarkAppTypography = AppTypography(
    h1 = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = TitleDark,
        fontFamily = fontFamily
    ),
    h2 = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = TitleDark,
        fontFamily = fontFamily
    ),
    h3 = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TitleDark,
        fontFamily = fontFamily
    ),
    h4 = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = TitleDark,
        fontFamily = fontFamily
    ),
    bold3 = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = BodyDark,
        fontFamily = fontFamily
    ),
    desc = TextStyle(
        fontSize = 14.sp,
        color = DescDark,
        fontFamily = fontFamily
    ),
    bodyBold = TextStyle(
        fontSize = 16.sp,
        color = BodyDark,
        fontFamily = fontFamily
    ),
    body18 = TextStyle(
        fontSize = 18.sp,
        color = BodyDark,
        fontFamily = fontFamily
    ),
    cancel = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = AppColor.CancelDig.dark,
        fontFamily = fontFamily
    ),
    ok = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = AppColor.GradGreen.dark,
        fontFamily = fontFamily
    ),
    normwhite = TextStyle(
        fontSize = 16.sp,
        color = Color(0xFFFFFFFF),
        fontFamily = fontFamily
    ),
    desc13 = TextStyle(
        fontSize = 13.sp,
        color = DescDark,
        fontFamily = fontFamily
    ),
    body14 = TextStyle(
        fontSize = 14.sp,
        color = BodyDark,
        fontFamily = fontFamily
    ),
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)