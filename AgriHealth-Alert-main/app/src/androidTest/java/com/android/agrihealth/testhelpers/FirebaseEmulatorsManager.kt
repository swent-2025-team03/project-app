package com.android.agrihealth.testhelpers

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.functions
import com.google.firebase.storage.storage
import kotlinx.coroutines.runBlocking
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.nio.ByteBuffer
import kotlin.collections.iterator
import okhttp3.OkHttpClient
import okhttp3.Request

data class FirebaseEnvironment(
    val host: String,
    val firestorePort: Int,
    val authPort: Int,
    val storagePort: Int,
    val functionsPort: Int
)

/** Handles which Firebase emulator to run, and makes sure useEmulators() isn't called twice */
object FirebaseEmulatorsManager {
  private var emulatorInitialized = false
  private const val LOCAL_HOST = "10.0.2.2"
  private const val FIRESTORE_PORT = 8081
  private const val AUTH_PORT = 9099
  private const val STORAGE_PORT = 9199
  private const val FUNCTIONS_PORT = 5001

  lateinit var environment: FirebaseEnvironment
    private set

  private val firestoreEndpoint by lazy {
    "http://${environment.host}:${environment.firestorePort}/emulator/v1/projects/agrihealth-alert/databases/(default)/documents"
  }

  private val authEndpoint by lazy {
    "http://${environment.host}:${environment.authPort}/emulator/v1/projects/agrihealth-alert/accounts"
  }

  val httpClient = OkHttpClient()

  /**
   * Initializes Firebase emulators, trying locally and scanning the network in case the Android
   * device is on another machine (meaning, a physical device)
   */
  fun linkEmulators(force: Boolean = false) {
    if (emulatorInitialized && !force) return

    val emulatorHost = if (shouldUseLocal()) LOCAL_HOST else scanNetworkForEmulators()

    check(emulatorHost != null) {
      "Firebase emulators not running locally, run: firebase emulators:start"
    }

    environment =
        FirebaseEnvironment(
            host = emulatorHost,
            firestorePort = FIRESTORE_PORT,
            authPort = AUTH_PORT,
            storagePort = STORAGE_PORT,
            functionsPort = FUNCTIONS_PORT)

    with(environment) {
      try {
        Firebase.firestore.useEmulator(host, firestorePort)
        Firebase.auth.useEmulator(host, authPort)
        Firebase.storage.useEmulator(host, storagePort)
        Firebase.functions.useEmulator(host, functionsPort)
      } catch (e: IllegalStateException) {
        if (e.message?.contains("Cannot call useEmulator()") == true) emulatorInitialized = true
        else throw e
      }
    }

    emulatorInitialized = true
  }

  private fun clearEmulator(endpoint: String) {
    val client = httpClient
    val request = Request.Builder().url(endpoint).delete().build()
    val response = client.newCall(request).execute()

    assert(response.isSuccessful) { "Failed to clear emulator at $endpoint" }
  }

  fun clearEmulators() {
    clearEmulator(firestoreEndpoint)
    clearEmulator(authEndpoint)
  }

  fun pingEmulator(host: String, port: Int, timeoutMs: Int = 1000): Boolean {
    return try {
      Socket().use { socket ->
        socket.connect(InetSocketAddress(host, port), timeoutMs)
        true
      }
    } catch (_: Exception) {
      false
    }
  }

  fun isLocalRunning(port: Int, timeoutMs: Int = 1000): Boolean {
    return pingEmulator(LOCAL_HOST, port, timeoutMs)
  }

  fun shouldUseLocal(): Boolean {
    return isLocalRunning(FIRESTORE_PORT) &&
        isLocalRunning(AUTH_PORT) &&
        isLocalRunning(STORAGE_PORT) &&
        isLocalRunning(FUNCTIONS_PORT)
  }

  // Find emulator on the local network, this only runs once per "run all tests" so it's fine if it
  // takes a while
  private fun scanNetworkForEmulators(): String? {
    val netInfo = getNetworkInfo() ?: return null // subnet ip + prefix
    val hosts = getHostRange(netInfo.ip, netInfo.prefix) // list of i.e. 192.168.1.XXX

    val ports = listOf(FIRESTORE_PORT, AUTH_PORT, STORAGE_PORT, FUNCTIONS_PORT)
    val testPort = ports.first()

    val timeout = System.currentTimeMillis() + (90 * 1000)
    for (host in hosts) {
      if (System.currentTimeMillis() > timeout)
          throw Exception(
              """Failed to scan the network for Firebase emulators. More details:
                |
                |If you are trying to run tests on a physical device, you can modify the FirebaseEmulatorsManager class to replace the local IP with the IP of the computer running the emulators and try again.
                |
                |If this sentence didn't make sense to you, contact Nils"""
                  .trimMargin())

      if (pingEmulator(host, testPort, timeoutMs = 100)) {
        // Found potential, need to check every port
        var found = true
        for (port in ports) found = found && (pingEmulator(host, port))
        if (!found) continue

        return host
      }
    }

    return null
  }
}

// --- Network stuff ---

data class NetworkInfo(val ip: Inet4Address, val prefix: Short)

// Get subnet IP and prefix
private fun getNetworkInfo(): NetworkInfo? {
  val interfaces = NetworkInterface.getNetworkInterfaces()
  for (intf in interfaces) {
    for (addr in intf.interfaceAddresses) {
      val ip = addr.address
      if (ip is Inet4Address && !ip.isLoopbackAddress) {
        return NetworkInfo(ip, addr.networkPrefixLength)
      }
    }
  }
  return null
}

// Get a list of every possible host in the subnet
private fun getHostRange(ip: Inet4Address, prefix: Short): List<String> {
  val mask = -1 shl (32 - prefix)
  val ipInt = ByteBuffer.wrap(ip.address).int

  val network = ipInt and mask
  val broadcast = network or mask.inv()

  val hosts = mutableListOf<String>()
  for (i in network + 1 until broadcast) { // skip network + broadcast
    val hostIp = InetAddress.getByAddress(ByteBuffer.allocate(4).putInt(i).array()).hostAddress!!
    hosts.add(hostIp)
  }
  return hosts
}
