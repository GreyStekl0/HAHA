import okhttp3.Dns
import org.xbill.DNS.AAAARecord
import org.xbill.DNS.ARecord
import org.xbill.DNS.Lookup
import org.xbill.DNS.SimpleResolver
import org.xbill.DNS.Type
import java.net.InetAddress

class CustomDns(
    private val dnsServerIp: String,
) : Dns {
    private val systemDns = Dns.SYSTEM

    override fun lookup(hostname: String): List<InetAddress> {
        try {
            val resolver = SimpleResolver(dnsServerIp)
            val result = mutableListOf<InetAddress>()

            // IPv4
            val lookupA = Lookup(hostname, Type.A)
            lookupA.setResolver(resolver)
            val recordsA = lookupA.run() ?: emptyArray()

            // IPv6
            val lookupAAAA = Lookup(hostname, Type.AAAA)
            lookupAAAA.setResolver(resolver)
            val recordsAAAA = lookupAAAA.run() ?: emptyArray()

            recordsA.forEach { record ->
                if (record is ARecord) {
                    result.add(record.address)
                }
            }

            recordsAAAA.forEach { record ->
                if (record is AAAARecord) {
                    result.add(record.address)
                }
            }

            return result.ifEmpty { systemDns.lookup(hostname) }
        } catch (e: Exception) {
            return systemDns.lookup(hostname)
        }
    }
}
