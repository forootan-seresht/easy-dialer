package app.arteh.easydialer.permissions

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import app.arteh.easydialer.R
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.EasyDialer
import app.arteh.easydialer.ui.theme.appTypography

@Composable
fun PermissionScreen(permissionVM: PermissionVM = viewModel(), modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME)
                permissionVM.goNext(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = stringResource(R.string.back),
                modifier = Modifier
                    .size(dimensionResource(R.dimen.little_go))
                    .padding(5.dp)
                    .noRippleClickable({ (context as Activity).finish() }),
                tint = colorResource(R.color.colorAccent)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.preparations),
                style = MaterialTheme.appTypography.h3
            )
        }

        Image(
            painter = painterResource(id = R.drawable.permission),
            contentDescription = null,
            modifier = Modifier
                .width(170.dp)
                .height(150.dp)
                .padding(top = 10.dp, bottom = 20.dp)
        )

        permissionVM.permissions.forEachIndexed { idx, row ->
            if (row.isVisible.value)
                PermissionRow(row)
        }
        Spacer(modifier = Modifier.weight(1f))

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .alpha(if (permissionVM.next) 1f else 0.7f)
                    .background(colorResource(R.color.colorAccent))
                    .clickable(onClick = { permissionVM.goNext(context) }),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.padding(top = 20.dp),
                    text = stringResource(R.string.next),
                    style = MaterialTheme.appTypography.normwhite
                )
                Image(
                    modifier = Modifier
                        .padding(top = 20.dp, start = 10.dp)
                        .size(15.dp),
                    painter = painterResource(R.drawable.go_white),
                    contentDescription = null
                )
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(
                        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp),
                        color = MaterialTheme.colorScheme.background
                    ),
            )
        }
    }
}

@Composable
private fun PermissionRow(row: PermissionVM.PermissionRow) {
    val context = LocalContext.current
    val activity = context as Activity

    val permissionLauncher: ManagedActivityResultLauncher<String, Boolean> =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted)
                Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
            else {
                val shouldShowRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        row.permission
                    )

                if (!shouldShowRationale) {
                    Toast.makeText(
                        context,
                        "Permission denied permanently or not asked yet",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    Column(
        Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .weight(1f),
                text = stringResource(row.title),
                style = MaterialTheme.appTypography.h3
            )

            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 5.dp,
                        shape = MaterialTheme.shapes.extraSmall,
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
                    .background(colorResource(R.color.colorPrimary))
                    .clickable { permissionLauncher.launch(row.permission) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.permission_lock))
                        .padding(10.dp),
                    painter = painterResource(R.drawable.unlock2),
                    contentDescription = "Arrow",
                )
            }
        }

        Text(
            modifier = Modifier.padding(horizontal = 15.dp),
            text = stringResource(row.body),
            style = MaterialTheme.appTypography.desc
        )
    }
}

@Preview(
    showBackground = true,
    name = "Light Preview"
)
@Composable
private fun prev() {
    EasyDialer(darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PermissionScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp)
            )
        }
    }
}