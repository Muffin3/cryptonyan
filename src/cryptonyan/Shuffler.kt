package cryptonyan

import kotlin.math.ceil


class Shuffler(private val firstKey: IntArray, private val secondKey: IntArray) {
    private val keysSize = firstKey.size

    /**
     * Encrypt string by shuffling rows and columns
     */
    fun encrypt(text: String): String{
        //First round of encrypting (shuffle columns)
        val rowCount = ceil(text.length.toFloat() / keysSize).toInt()
        val columnCount = keysSize
        var chars = this.textToTable(text, rowCount, columnCount)
        var result = this.columnShuffle(chars)  //result after first round

        //Second round of encrypting (shuffle rows)
        chars = this.textToTable(result, columnCount, rowCount)
        result = this.rowShuffle(chars)         //result after second round
        return result
    }

    /**
     * Decrypt string by unshuffling rows and columns
     */
    fun decrypt(text: String): String{
        // First round decrypting
        var chars = this.restoreRowShuffledTable(text)
        var result = this.tableToText(chars)

        // Second round decrypting
        chars = this.restoreColumnShuffledTable(result)
        result = this.tableToText(chars)
        return result.trim('|')
    }

    /**
     * Builds table of text chars, rows by rows (row size multiple by key size)
     * Expand text size if size isn't multiple to key size
     */
    private fun textToTable(text: String, rowCount: Int, columnCount: Int): Array<Array<Char>>{
        val chars = Array(rowCount) { Array(columnCount) {'|'} }
        var count = 0
        try {
            for (i in chars.indices)
                for (j in chars[i].indices) {
                    chars[i][j] = text[count]
                    count++
                }
        } catch (e: IndexOutOfBoundsException){
            //text size is not multiple to 5
        }
        return chars
    }

    /**
     * Shuffle columns by key
     * Returns encrypted text string
     */
    private fun columnShuffle(table: Array<Array<Char>>): String{
        val tmp = Array(table.size * table[0].size) {'|'}
        var index = 0
        for (j in table[0].indices) {
            for (i in table.indices) {
                tmp[index] = table[i][firstKey[j]-1]
                index++
            }
        }
        return tmp.joinToString(separator = "")
    }

    private fun rowShuffle(table: Array<Array<Char>>): String{
        var result = ""
        for (i in table.indices)
            result += table[secondKey[i]-1].joinToString(separator = "")
        return result
    }

    /**
     * Builds table of text chars, column by columns by key
     */
    private fun restoreColumnShuffledTable(text: String): Array<Array<Char>>{
        if (text.length % keysSize != 0)
            throw Nyanception("Cannot decrypt text. Text length doesn't multiple to key size")
        val rowCount = ceil(text.length.toFloat() / keysSize).toInt()
        val columnCount = keysSize
        val chars = Array(rowCount) { Array(columnCount) {'|'} }
        var index = 0
        for (j in chars[0].indices)
            for (i in chars.indices) {
                chars[i][firstKey[j]-1] = text[index]
                index++
            }
        return chars
    }

    private fun restoreRowShuffledTable(text: String): Array<Array<Char>>{
        if (text.length % keysSize != 0)
            throw Nyanception("Cannot decrypt text. Text length doesn't multiple to key size")
        val rowCount = keysSize
        val columnCount = ceil(text.length.toFloat() / keysSize).toInt()
        val chars = Array(rowCount) { Array(columnCount) {'|'} }
        var index = 0
        for (i in chars.indices)
            for (j in chars[i].indices) {
                chars[secondKey[i]-1][j] = text[index]
                index++
            }
        return chars
    }

    /**
     * Converts Array<Array<Char>> to String.
     */
    private fun tableToText(table: Array<Array<Char>>): String{
        val tmp = Array(table.size * table[0].size) {'|'}
        var count = 0
        for (i in table.indices) {
            for (j in table[i].indices) {
                tmp[count] = table[i][j]
                count++
            }
        }
        return tmp.joinToString(separator = "")
    }

    private fun printMatrix(m: Array<Array<Char>>){
        for (row in m){
            for (c in row){
                print(c)
            }
            println()
        }
    }
}