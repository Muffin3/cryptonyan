package cryptonyan

import kotlin.experimental.xor
import kotlin.math.ceil

class Feistel(private val keyString: String) {
    private val blockSize: Int = 16
    private val flowCount: Int = 4
    private val flowSize: Int = blockSize / flowCount
    private val roundCount: Int = 16
    private val keys: Array<Array<Byte>> = Array(roundCount){ Array(flowCount) {' '.toByte()} }

    init{
        generateKeys()
    }

    /**
     * Encrypts input data
     */
    fun encrypt(data: ByteArray): ByteArray{
        val blocks = separateToBlocks(data)
        for (i in blocks.indices){
            val (flow1, flow2, flow3, flow4) = blocks[i]
            for (j in 0 until roundCount){
                blocks[i] = doRound(flow1, flow2, flow3, flow4, j)
            }
        }
        return restoreSeparatedArray(blocks)
    }

    /**
     * Decrypts encrypted data
     */
    fun decrypt(data: ByteArray): ByteArray{
        if (data.size % blockSize != 0)
            throw Nyanception("Cannot decrypt text. Text length doesn't multiple to block size")
        val blocks = separateToBlocks(data)
        for (i in blocks.indices){
            val (flow1, flow2, flow3, flow4) = blocks[i]
            for (j in roundCount-1 downTo 0){
                blocks[i] = doReversedRound(flow1, flow2, flow3, flow4, j)
            }
        }
        return restoreSeparatedArray(blocks, true)
    }

    /**
     * Does one round of Feistel's network to encrypt data
     * Returns block (128 bits) of data
     */
    private fun doRound(f1: ByteArray, f2: ByteArray, f3: ByteArray, f4: ByteArray, roundNum: Int):Array<ByteArray> {
        val result = Array(flowCount) {ByteArray(flowSize)}
        var tmp: Byte
        for (i in 0 until flowSize){
            tmp = f1[i].xor(keys[roundNum][i])
            f2[i] = tmp.xor(f2[i])
            f3[i] = tmp.xor(f3[i])
            f4[i] = tmp.xor(f4[i])
        }
        result[0] = f2; result[1] = f3
        result[2] = f4; result[3] = f1
        return result
    }

    /**
     * Does one reversed round of Feistel's network to decrypt data
     * Returns block (128 bits) of data
     */
    private fun doReversedRound(f1: ByteArray, f2: ByteArray, f3: ByteArray, f4: ByteArray, roundNum: Int):Array<ByteArray> {
        val result = Array(flowCount) {ByteArray(flowSize)}
        var tmp: Byte
        for (i in 0 until flowSize){
            tmp = f4[i].xor(keys[roundNum][i])
            f1[i] = tmp.xor(f1[i])
            f2[i] = tmp.xor(f2[i])
            f3[i] = tmp.xor(f3[i])
        }
        result[0] = f4; result[1] = f1
        result[2] = f2; result[3] = f3
        return result
    }

    /**
     * Separates input data to blocks 128 bits
     * Every block separated to 4 flows 32 bits
     * Extends input data to size multiple by block size
     */
    private fun separateToBlocks(data: ByteArray): Array<Array<ByteArray>> {
        val rowCount = ceil(data.size.toFloat() / blockSize).toInt()
        val blocks = Array(rowCount){Array(flowCount) { ByteArray(flowSize) } }
        var index = 0
        try{
            for (i in blocks.indices){
                for (j in blocks[i].indices){
                    for (k in blocks[i][j].indices){
                        blocks[i][j][k] = data[index]
                        index++
                    }
                }
            }
        }catch (e: IndexOutOfBoundsException){
            // data size is not multiple to block size
        }
        return blocks
    }

    /**
     * Convert block and flows to single Byte Array
     * Trims last block from extend data
     */
    private fun restoreSeparatedArray(blocks: Array<Array<ByteArray>>, trim: Boolean=false): ByteArray {
        var result = ByteArray(blocks.size * blocks[0].size * blocks[0][0].size)
        var index = 0
        for (i in blocks.indices)
            for (j in blocks[i].indices)
                for (k in blocks[i][j].indices){
                    if (i < blocks.size-1 || blocks[i][j][k] != (0).toByte()) {
                        result[index] = blocks[i][j][k]
                        index++
                    }
                }
        if (trim) result = result.sliceArray(0 until index)
        return result
    }

    /**
     * Does cyclic right shift of Byte Array
     */
    private fun cyclicShiftR(a: Array<Byte>, shift:Int): Array<Byte>{
        val result = Array<Byte>(a.size) {0}
        for (i in result.indices)
            result[(i+shift) % result.size] = a[i]
        return result
    }

    /**
     * Generate 16 keys by 32 bits from 128 bits key
     */
    private fun generateKeys(){
        val keyBytes = keyString.toByteArray()
        if (keyBytes.size != blockSize)
            throw Nyanception("Key size should be equal to block size ($blockSize bytes).")

        for (i in 0 until flowSize) {
            for (j in keys[i].indices){
                keys[i][j] = keyBytes[i * flowSize + j]
            }
            keys[i+4] = cyclicShiftR(keys[i], 1)
            keys[i+8] = cyclicShiftR(keys[i], 2)
            keys[i+12] = cyclicShiftR(keys[i], 3)
        }
    }
}