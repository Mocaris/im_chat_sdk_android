package com.lbe.imsdk

import com.lbe.imsdk.repository.remote.model.AgentUser
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    @Test
    fun testJson(){
        val json="{\"username\":\"Jeremiah\",\"faceUrl\":\"{\\\"key\\\":\\\"v1:lbeec21bd2cfc0ecdb42d818666d4\\\",\\\"url\\\":\\\"https://oss.imsz.online/private/openimttt/lbe_83913a528b2743da0b0a32c4694617.jpg\\\"}\",\"joinTime\":1741942927611}"
        val user = Json.decodeFromString<AgentUser>(json)
        println(user)
    }
}
