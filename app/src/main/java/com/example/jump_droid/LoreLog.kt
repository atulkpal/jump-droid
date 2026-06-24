package com.example.jump_droid

enum class LoreCategory {
    SIGNAL, SURVIVOR, ANCIENT, VOID, EPILOGUE
}

data class LoreLog(
    val id: String,
    val title: String,
    val text: String,
    val unlockAltitude: Int,
    val category: LoreCategory
) {
    companion object {
        val ALL_LOGS = listOf(
            LoreLog(
                id = "log_500",
                title = "Atmospheric Static",
                text = "Day 4. The engines are humming at a frequency I've never heard before. It's almost like they're responding to something in the clouds. I caught a fragment of a transmission... it sounded like a heartbeat.",
                unlockAltitude = 500,
                category = LoreCategory.SIGNAL
            ),
            LoreLog(
                id = "log_1000",
                title = "The First Ascent",
                text = "My grandfather told me about the First Ascent. They didn't have shields then. Just raw thrust and a lot of hope. He said they saw shadows moving in the cloud layer. I always thought he was exaggerating. Now, I'm not so sure.",
                unlockAltitude = 1000,
                category = LoreCategory.SURVIVOR
            ),
            LoreLog(
                id = "log_2000",
                title = "Echoes of the Past",
                text = "Found a derelict chassis drifting at 2,000m. The markings were from the 1984 mission. That was the year they officially shut down the program after the 'Signal Breach'. What was it doing all the way up here?",
                unlockAltitude = 2000,
                category = LoreCategory.ANCIENT
            ),
            LoreLog(
                id = "log_3000",
                title = "Crystalline Formations",
                text = "The clouds are different here. They're starting to crystallize into structures. It's beautiful, and terrifying. The scanners are picking up high-energy signatures from within the towers. They look... built.",
                unlockAltitude = 3000,
                category = LoreCategory.ANCIENT
            ),
            LoreLog(
                id = "log_4000",
                title = "The Voice in the Static",
                text = "The transmission is clearer now. It's not a heartbeat. It's a sequence. 0-1-1-2-3-5-8... Fibonacci? Why would a cosmic signal be using terrestrial mathematics? Or maybe it's not terrestrial.",
                unlockAltitude = 4000,
                category = LoreCategory.SIGNAL
            ),
            LoreLog(
                id = "log_5000",
                title = "Beyond the Blue",
                text = "I've passed the 5,000m mark. The sky is turning black. I can see the curve of the world. It looks so fragile from here. Every pilot who reached this point described a 'sense of calling'. I feel it now. It's coming from above.",
                unlockAltitude = 5000,
                category = LoreCategory.SURVIVOR
            ),
            LoreLog(
                id = "log_6000",
                title = "Ghost in the Machine",
                text = "The onboard AI is acting up. It's trying to reroute navigation to a point in the Void. I've overwritten it three times, but it keeps reverting. It says 'THE SOURCE IS WAITING'. I don't remember programming that.",
                unlockAltitude = 6000,
                category = LoreCategory.VOID
            ),
            LoreLog(
                id = "log_7500",
                title = "The Great Silence",
                text = "Absolute silence. No wind, no engine hum, just the stars. They look... wrong. Their positions don't match my charts. It's like the further I go, the more the world I know fades away.",
                unlockAltitude = 7500,
                category = LoreCategory.VOID
            ),
            LoreLog(
                id = "log_9000",
                title = "The Signal Revealed",
                text = "I've decoded the final sequence. It's not a message. It's a map. A map to something inside the Void. Something they've been hiding from us. The Ascension Program wasn't about exploration. It was a search party.",
                unlockAltitude = 9000,
                category = LoreCategory.SIGNAL
            ),
            LoreLog(
                id = "log_10000",
                title = "Ascension",
                text = "I'm at the threshold. The Void is right in front of me. It's not empty. It's full of... light. I understand now. We didn't receive a signal. We were answering a call to return home. I'm crossing over.",
                unlockAltitude = 10000,
                category = LoreCategory.EPILOGUE
            )
        )
    }
}
