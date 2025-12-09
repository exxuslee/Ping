package org.haos.exxuslee.ping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.haos.exxuslee.ping.ui.theme.PingTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PingTheme {
                PingApp()
            }
        }
    }
}

@Composable
fun PingApp() {
    val scope = rememberCoroutineScope()
    var address by rememberSaveable { mutableStateOf("https://google.com") }
    var isPinging by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val pingResults = remember { mutableStateListOf<String>() }
    val timeFormatter = remember {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ping",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Start
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Адрес") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val host = address.trim()
                        if (host.isEmpty()) {
                            errorText = "Введите адрес для пинга"
                            return@Button
                        }
                        errorText = null
                        isPinging = true
                        scope.launch {
                            val resultText = runCatching { pingHost(host) }
                                .fold(
                                    onSuccess = { latency ->
                                        "${timeFormatter.format(Date())} • $host • ${latency} ms"
                                    },
                                    onFailure = { error ->
                                        "${timeFormatter.format(Date())} • $host • ошибка: ${error.message.orEmpty()}"
                                    }
                                )
                            pingResults.add(0, resultText)
                            isPinging = false
                        }
                    },
                    enabled = !isPinging
                ) {
                    Text(text = if (isPinging) "Пингую..." else "PING")
                }

                if (isPinging) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            }

            errorText?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "Результаты",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pingResults) { result ->
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (pingResults.isEmpty()) {
                    item {
                        Text(
                            text = "Нажмите PING, чтобы увидеть время ответа",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private suspend fun pingHost(url: String): Long = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    val request = Request.Builder()
        .url(url)
        .head() // Используем HEAD для минимального трафика
        .build()

    val startTime = System.currentTimeMillis()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw IOException("Unexpected code $response")
        }
        System.currentTimeMillis() - startTime
    }
}

@Preview(showBackground = true)
@Composable
private fun PingAppPreview() {
    PingTheme {
        PingApp()
    }
}

