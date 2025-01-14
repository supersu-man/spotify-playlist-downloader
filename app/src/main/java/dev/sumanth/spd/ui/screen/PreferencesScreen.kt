package dev.sumanth.spd.ui.screen

import android.app.Activity
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sumanth.spd.model.downloadPath
import dev.sumanth.spd.ui.viewmodel.PreferencesScreenViewModel

@Composable
fun PreferencesScreen(preferencesViewModel: PreferencesScreenViewModel) {

    preferencesViewModel.launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data?.path
            if(uri != null) {
                val folder = uri.split(":")[1]
                preferencesViewModel.downloadPath = Environment.getExternalStorageDirectory().path + "/" + folder
                preferencesViewModel.storePath(preferencesViewModel.downloadPath)
                downloadPath = preferencesViewModel.downloadPath
            }
        }
    }

    Column(modifier = Modifier.padding(10.dp, 20.dp).fillMaxSize()) {
        Card(onClick = { preferencesViewModel.openDocumentTree() }, modifier = Modifier.fillMaxWidth() ) {
            Text("Download location: ", modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 10.dp))
            Text(preferencesViewModel.downloadPath, modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 10.dp), fontSize = 14.sp)
        }
    }

}

