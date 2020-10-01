import cryptonyan.Feistel
import cryptonyan.Shuffler
import java.io.File

fun main(args: Array<String>){
    val feistel = Feistel("jorlng82n5y9hr62")
    val shuffler = Shuffler(intArrayOf(4,2,1,5,3), intArrayOf(2,5,3,4,1))
    var reader = File("example")
    var writer = File("encrypt")
    val encryptedText = shuffler.encrypt(reader.readText())
    val encryptedBytes = feistel.encrypt(encryptedText.toByteArray())
    writer.writeBytes(encryptedBytes)

    reader = File("encrypt")
    val decryptedBytes = feistel.decrypt(reader.readBytes())
    writer = File("decrypt")
    writer.writeBytes(decryptedBytes)
    reader = File("decrypt")
    val decryptedText = shuffler.decrypt(reader.readText())
    writer = File("decrypt")
    writer.writeText(decryptedText)
}