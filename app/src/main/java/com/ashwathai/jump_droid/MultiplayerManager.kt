package com.ashwathai.jump_droid

import android.util.Log
import androidx.compose.runtime.*
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class MultiplayerManager(
    private val loginManager: LoginManager
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    
    // --- State ---
    var connectionStatus by mutableStateOf("INITIALIZING...")
    var currentRoom by mutableStateOf<MultiplayerRoom?>(null)
        private set
    var opponentState by mutableStateOf<PlayerMultiplayerState?>(null)
    val broadcastMessages = mutableStateListOf<GlobalBroadcast>()
    
    private var roomListener: ListenerRegistration? = null
    private var opponentStateListener: ValueEventListener? = null
    private var broadcastListener: ValueEventListener? = null
    
    init {
        try {
            listenForBroadcasts()
            checkDatabaseConnection()
        } catch (e: Exception) {
            connectionStatus = "DATABASE CONFIG MISSING"
        }
    }

    private fun checkDatabaseConnection() {
        val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                connectionStatus = if (connected) "STABLE" else "CONNECTING..."
                Log.d("MultiplayerManager", "Connection state: $connectionStatus")
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --- Lobby Management ---
    
    suspend fun createRoom(): String {
        val userId = loginManager.playerId ?: return ""
        val code = (100000..999999).random().toString()
        val seed = Random.nextInt()
        
        val room = MultiplayerRoom(
            code = code,
            hostId = userId,
            hostName = loginManager.displayName ?: "Pilot",
            seed = seed,
            status = RoomStatus.LOBBY
        )
        
        firestore.collection("rooms").document(code).set(room).await()
        currentRoom = room
        observeRoom(code)
        return code
    }

    suspend fun joinRoom(code: String): Boolean {
        val userId = loginManager.playerId ?: return false
        val roomDoc = firestore.collection("rooms").document(code).get().await()
        
        if (!roomDoc.exists()) return false
        
        val room = roomDoc.toObject(MultiplayerRoom::class.java) ?: return false
        if (room.guestId != null && room.guestId != userId) return false // Room full
        
        val updatedRoom = room.copy(
            guestId = userId,
            guestName = loginManager.displayName ?: "Guest",
            status = RoomStatus.STARTING
        )
        
        firestore.collection("rooms").document(code).set(updatedRoom).await()
        currentRoom = updatedRoom
        observeRoom(code)
        startStateSync()
        return true
    }

    private fun observeRoom(code: String) {
        roomListener?.remove()
        roomListener = firestore.collection("rooms").document(code)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val room = snapshot?.toObject(MultiplayerRoom::class.java)
                currentRoom = room
                if (room?.status == RoomStatus.STARTING || room?.status == RoomStatus.ACTIVE) {
                    startStateSync()
                }
            }
    }

    // --- State Synchronization ---

    fun syncLocalState(state: PlayerMultiplayerState) {
        val room = currentRoom ?: return
        val userId = loginManager.playerId ?: return
        database.child("active_games").child(room.code).child(userId).setValue(state)
    }

    private fun startStateSync() {
        val room = currentRoom ?: return
        val userId = loginManager.playerId ?: return
        val opponentId = if (room.hostId == userId) room.guestId else room.hostId
        
        if (opponentId == null) return
        
        opponentStateListener?.let { database.child("active_games").child(room.code).child(opponentId).removeEventListener(it) }
        
        opponentStateListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                opponentState = snapshot.getValue(PlayerMultiplayerState::class.java)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("MultiplayerManager", "Opponent sync cancelled: ${error.message}")
            }
        }
        
        database.child("active_games").child(room.code).child(opponentId)
            .addValueEventListener(opponentStateListener!!)
    }

    // --- Phase 0: Broadcast Test ---

    fun sendBroadcast(message: String) {
        val userId = loginManager.playerId ?: "anon"
        val broadcast = GlobalBroadcast(
            senderId = userId,
            senderName = loginManager.displayName ?: "Pilot",
            message = message,
            timestamp = System.currentTimeMillis()
        )
        Log.d("MultiplayerManager", "Sending broadcast: $message from $userId")
        database.child("global_broadcast").setValue(broadcast)
            .addOnSuccessListener {
                Log.d("MultiplayerManager", "Broadcast sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("MultiplayerManager", "Failed to send broadcast: ${e.message}")
            }
    }

    private fun listenForBroadcasts() {
        Log.d("MultiplayerManager", "Initializing broadcast listener...")
        broadcastListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val broadcast = snapshot.getValue(GlobalBroadcast::class.java)
                Log.d("MultiplayerManager", "Broadcast received: ${broadcast?.message} from ${broadcast?.senderName}")
                if (broadcast != null) {
                    // Check if already in list to avoid duplicates from listener logic
                    if (broadcastMessages.none { it.timestamp == broadcast.timestamp && it.senderId == broadcast.senderId }) {
                        broadcastMessages.add(0, broadcast) // Add to top
                        if (broadcastMessages.size > 5) broadcastMessages.removeAt(5)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("MultiplayerManager", "Broadcast listener cancelled: ${error.message} (Code: ${error.code})")
            }
        }
        database.child("global_broadcast").addValueEventListener(broadcastListener!!)
    }

    fun cleanup() {
        roomListener?.remove()
        opponentStateListener?.let { 
            val room = currentRoom
            val userId = loginManager.playerId
            if (room != null && userId != null) {
                val opponentId = if (room.hostId == userId) room.guestId else room.hostId
                if (opponentId != null) {
                    database.child("active_games").child(room.code).child(opponentId).removeEventListener(it)
                }
            }
        }
        broadcastListener?.let { database.child("global_broadcast").removeEventListener(it) }
    }
}
