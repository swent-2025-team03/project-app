package com.android.agrihealth.data.model.firebase.emulators

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.nio.ByteBuffer

data class FirebaseEnvironment(
    val host: String,
    val firestorePort: Int,
    val authPort: Int,
    val storagePort: Int
)

/** Handles which Firebase emulator to run, and makes sure useEmulators() isn't called twice */
object FirebaseEmulatorsManager {
  private var emulatorInitialized = false
  private const val LOCAL_HOST = "10.0.2.2"
  private const val FIRESTORE_PORT = 8081
  private const val AUTH_PORT = 9099
  private const val STORAGE_PORT = 9199

  lateinit var environment: FirebaseEnvironment
    private set

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
            storagePort = STORAGE_PORT)

    with(environment) {
      Firebase.firestore.useEmulator(host, firestorePort)
      Firebase.auth.useEmulator(host, authPort)
      Firebase.storage.useEmulator(host, storagePort)
    }

    emulatorInitialized = true
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
        isLocalRunning(STORAGE_PORT)
  }

  // Find emulator on the local network, this only runs once per "run all tests" so it's fine if it
  // takes a while
  private fun scanNetworkForEmulators(): String? {
    val netInfo = getNetworkInfo() ?: return null // subnet ip + prefix
    val hosts = getHostRange(netInfo.ip, netInfo.prefix) // list of i.e. 192.168.1.XXX

    val ports = listOf(FIRESTORE_PORT, AUTH_PORT, STORAGE_PORT)
    val testPort = ports.first()

    for (host in hosts) {
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
