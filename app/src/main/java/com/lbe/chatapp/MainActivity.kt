package com.lbe.chatapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lbe.chatapp.ui.theme.IMChatSdkNativeTheme
import com.lbe.imsdk.pages.LbeMainActivity
import com.lbe.imsdk.repository.model.SDKInitConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IMChatSdkNativeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        val lbeSign =
//                            remember { mutableStateOf("0x4f227352cf96fab9e67064e08219a86cd398fdbb067aa53fc7ad49deb882a0ad49b1d073ae0b3f74d39d288f3cf3feab6f102c1993532e1239e2f48e4afb534b1c") }
                            remember { mutableStateOf("0x9da959d63e2f35332fc8336b2f78dc4fdae3637f6b9502a6278a06794f649f21330c2b50c494b60d1723c67d6240d63a46c27e3c22ee028cfb116218ca5526921b") }

                        val lbeIdentity =
                            remember { mutableStateOf("4fmphnfgc4zq") }

                        val headerIcon =
//                            remember { mutableStateOf("http://10.40.92.203:9910/openimttt/lbe_65f8d397953b979b4be0d098e8d4f5.jpg") }
                            remember { mutableStateOf("{\"url\":\"https://abpay-pub.s3.ap-northeast-1.amazonaws.com/1078_1724306513153.png\",\"key\":\"\"}") }

                        val groupId =
                            remember { mutableStateOf("1002") }

                        val nickId =
//                            remember { mutableStateOf("112233445566778899") }
                            remember { mutableStateOf("2710512892936192") }
//                            remember { mutableStateOf("android001") }

                        val nickName =
//                            remember { mutableStateOf("android001") }
                            remember { mutableStateOf("平哥哥") }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(15.dp),
                            verticalArrangement = Arrangement.spacedBy(
                                10.dp,
                                alignment = Alignment.Top
                            ),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            item {
                                TextField(
                                    value = lbeSign.value,
                                    onValueChange = { lbeSign.value = it },
                                    label = {
                                        Text("LBE Sign")
                                    },
                                    maxLines = 1,
                                    singleLine = true,
                                )
                            }
                            item {
                                TextField(
                                    value = lbeIdentity.value,
                                    onValueChange = { lbeIdentity.value = it },
                                    label = {
                                        Text("LBE Identity")
                                    },
                                    maxLines = 1,
                                    singleLine = true,
                                )
                            }
                            item {
                                TextField(
                                    value = headerIcon.value,
                                    onValueChange = { headerIcon.value = it },
                                    label = {
                                        Text("Header Icon")
                                    },
                                    maxLines = 1,
                                    singleLine = true,
                                )
                            }
                            item {
                                TextField(
                                    value = nickId.value,
                                    onValueChange = { nickId.value = it },
                                    label = {
                                        Text("Nick Id")
                                    },
                                    maxLines = 1,
                                    singleLine = true,
                                )
                            }
                            item {
                                TextField(
                                    value = nickName.value,
                                    onValueChange = { nickName.value = it },
                                    label = {
                                        Text("Nick Name")
                                    },
                                    maxLines = 1,
                                    singleLine = true,
                                )
                            }
                            item {
                                TextField(
                                    value = groupId.value,
                                    onValueChange = { groupId.value = it },
                                    label = {
                                        Text("Group Id")
                                    },
                                    maxLines = 1,
                                    singleLine = true,
                                )
                            }
                        }
                        ElevatedButton(onClick = {
                            LbeMainActivity.start(
                                this@MainActivity, SDKInitConfig(
                                    lbeSign = lbeSign.value,
                                    lbeIdentity = lbeIdentity.value,
                                    phone = "",
                                    email = "",
                                    language = "zh",
                                    device = "",
                                    headerIcon = headerIcon.value,
                                    groupID = groupId.value,
                                    domain = "",
                                    source = "",
                                    nickId = nickId.value,
                                    nickName = nickName.value,
                                    extraInfo = mutableMapOf()
                                )
                            )
                            finish()
                        }) {
                            Text("Start Chat")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IMChatSdkNativeTheme {
        Greeting("Android")
    }
}