package dev.sumanth.spd.ui.screen

import android.app.Activity
import android.content.Intent
import android.os.Environment
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sumanth.spd.utils.SharedPref

@Composable
fun PreferencesScreen() {
    val context = LocalContext.current
    val sharedPref by remember { mutableStateOf(SharedPref(context)) }
    var downloadPath by remember { mutableStateOf(sharedPref.getDownloadPath()) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data?.path
            if(uri != null) {
                val folder = uri.split(":")[1]
                downloadPath = Environment.getExternalStorageDirectory().path + "/" + folder
                sharedPref.storeDownloadPath(downloadPath)
            }
        }
    }

    Column(modifier = Modifier.padding(10.dp, 20.dp).fillMaxSize()) {
        Card(onClick = { launch(launcher) }, modifier = Modifier.fillMaxWidth() ) {
            Text("Download location: ", modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 10.dp))
            Text(downloadPath, modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 10.dp), fontSize = 14.sp)
        }
    }

}

private fun launch(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
    }
    launcher.launch(intent)
}

