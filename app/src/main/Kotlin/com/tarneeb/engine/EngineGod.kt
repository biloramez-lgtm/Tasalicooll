package com.tarneeb.engine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

// ===================== MODELS =====================
enum class Suit(val symbol: String, val arabicName: String) {
    HEARTS("♥","قلوب"), DIAMONDS("♦","ماسات"), CLUBS("♣","نوادي"), SPADES("♠","بستم")
}

enum class Rank(val value: Int, val display: String) {
    TWO(2,"2"), THREE(3,"3"), FOUR(4,"4"), FIVE(5,"5"),
    SIX(6,"6"), SEVEN(7,"7"), EIGHT(8,"8"), NINE(9,"9"),
    TEN(10,"10"), JACK(11,"J"), QUEEN(12,"Q"), KING(13,"K"), ACE(14,"A")
}

data class Card(val suit: Suit, val rank: Rank) {
    override fun toString() = "${rank.display}${suit.symbol}"
}

data class Player(val id: Int, val name: String, val isAI: Boolean=false, val difficulty:AIDifficulty=AIDifficulty.MEDIUM) {
    val hand = mutableListOf<Card>()
    var bid = 0
    var tricksWon = 0
    var score = 0
    fun addCard(card: Card) = hand.add(card)
    fun removeCard(card: Card) = hand.remove(card)
    fun clearHand() { hand.clear(); bid=0; tricksWon=0 }
    fun sortHand() = hand.sortWith(compareBy({ it.suit.ordinal },{it.rank.value}))
    fun canFollowSuit(suit: Suit) = hand.any { it.suit==suit }
}

data class Team(val id:Int, val name:String, val players:List<Player>) {
    var totalScore=0
    var totalBid=0
    var tricksWon=0
    val isWinner get() = totalScore>=41
    fun addScore(points:Int){ totalScore+=points }
    fun resetBid(){ totalBid=0; players.forEach{it.bid=0} }
    fun calculateBid():Int { totalBid=players.sumOf{it.bid}; return totalBid }
}

data class Trick(val number:Int=0){
    val cardsPlayed = mutableMapOf<Int, Card>()
    val playOrder = mutableListOf<Int>()
    var winnerId:Int?=null
    val trickSuit get() = cardsPlayed.values.firstOrNull()?.suit
    fun playCard(playerId:Int, card:Card){ if(!cardsPlayed.containsKey(playerId)) playOrder.add(playerId); cardsPlayed[playerId]=card }
    fun isComplete(totalPlayers:Int) = cardsPlayed.size==totalPlayers
}

enum class GamePhase { WAITING, BIDDING, PLAYING, ROUND_END, GAME_END }
enum class AIDifficulty { EASY, MEDIUM, HARD }
enum class GameMode { SINGLE_PLAYER, MULTIPLAYER_LOCAL }

sealed class AIAction {
    data class PlacingBid(val playerId:Int,val bid:Int):AIAction()
    data class PlayingCard(val playerId:Int,val card:Card):AIAction()
}

data class TarneebGame(
    val team1:Team,
    val team2:Team,
    val players:List<Player>,
    val gameMode: GameMode = GameMode.SINGLE_PLAYER,
    val humanPlayerCount:Int=1
){
    var gamePhase=GamePhase.WAITING
    var currentPlayerIndex=0
    var dealerIndex=0
    var currentRound=1
    var currentTrickNumber=0
    var isGameOver=false
    val tricks= mutableListOf<Trick>()
    val currentPlayer get() = if(currentPlayerIndex<players.size) players[currentPlayerIndex] else null
    val currentTrick get() = tricks.lastOrNull()
    fun nextPlayerIndex() = (currentPlayerIndex+1)%players.size
    fun advancePlayer(){ currentPlayerIndex = nextPlayerIndex() }
    fun startBidding(){ gamePhase=GamePhase.BIDDING; currentPlayerIndex=(dealerIndex+1)%players.size; players.forEach{it.bid=0} }
    fun startPlaying(){ gamePhase=GamePhase.PLAYING; currentTrickNumber=1; tricks.clear(); team1.tricksWon=0; team2.tricksWon=0; players.forEach{it.tricksWon=0}; currentPlayerIndex=(dealerIndex+1)%players.size }
    fun endRound(){ gamePhase=GamePhase.ROUND_END }
    fun startNextRound(){ dealerIndex=(dealerIndex+1)%players.size; currentRound++; team1.resetBid(); team2.resetBid(); players.forEach{it.clearHand(); it.tricksWon=0} }
    fun endGame(){ gamePhase=GamePhase.GAME_END; isGameOver=true }
    fun getTeamByPlayerId(playerId:Int) = when{
        team1.players.any{it.id==playerId}->team1
        team2.players.any{it.id==playerId}->team2
        else->null
    }
    fun getOpponentTeam(playerId:Int):Team?{ val t=getTeamByPlayerId(playerId); return when(t?.id){team1.id->team2; team2.id->team1; else->null} }
    fun getInfo() = """
        |====== Tarneeb Game ======
        |${team1.name}: ${team1.totalScore} نقاط (البدية: ${team1.totalBid})
        |${team2.name}: ${team2.totalScore} نقاط (البدية: ${team2.totalBid})
        |النمط: ${gameMode.name}
        |المرحلة: ${gamePhase.name}
        |الجولة: $currentRound
        |الخدعات: ${tricks.size}/13
        |الدور: ${currentPlayer?.name ?: "لا أحد"}
    """.trimMargin()
}

// ===================== ENGINE GOD =====================
class EngineGod {
    private val _gameState = MutableStateFlow<TarneebGame?>(null)
    val gameState: StateFlow<TarneebGame?> = _gameState.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _aiAction = MutableStateFlow<AIAction?>(null)
    val aiAction: StateFlow<AIAction?> = _aiAction.asStateFlow()
    private val random = Random(System.currentTimeMillis())
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ===================== GAME INITIALIZATION =====================
    fun startSinglePlayer(playerName:String, difficulty:AIDifficulty=AIDifficulty.MEDIUM){
        val humanPlayer= Player(0,playerName,false,difficulty)
        val ai1=Player(1,"AI 1",true,difficulty)
        val ai2=Player(2,"AI 2",true,difficulty)
        val ai3=Player(3,"AI 3",true,difficulty)
        val team1=Team(1,"أنت والحليف", listOf(humanPlayer, ai2))
        val team2=Team(2,"الخصوم", listOf(ai1, ai3))
        val game=TarneebGame(team1,team2,listOf(humanPlayer,ai1,ai2,ai3))
        dealCards(game)
        game.startBidding()
        _gameState.value=game
    }

    // ===================== CARD DEALING =====================
    private fun dealCards(game:TarneebGame){
        val deck = generateDeck().shuffled()
        game.players.forEach{it.clearHand()}
        deck.chunked(13).forEachIndexed{index,cards-> game.players[index].hand.addAll(cards); game.players[index].sortHand() }
    }
    private fun generateDeck():List<Card>{
        val deck= mutableListOf<Card>()
        Suit.values().forEach{ s-> Rank.values().forEach{ r-> deck.add(Card(s,r)) } }
        return deck
    }

    // ===================== BIDDING =====================
    fun placeBid(playerId:Int,bid:Int):Boolean{
        val game=_gameState.value?:return false
        val playerTeam=game.getTeamByPlayerId(playerId)
        if(!isValidBid(bid,playerTeam)){
            _error.value="بدية غير صحيحة"
            return false
        }
        val player=game.players.find{it.id==playerId}?:return false
        player.bid=bid
        game.advancePlayer()
        _gameState.value=game
        if(game.players.all{it.bid>0}){ val t1=game.team1.calculateBid(); val t2=game.team2.calculateBid(); val min=getMinimumTotalBid(game); if(t1+t2<min){ resetBidsAndRedeal(game); return true } game.startPlaying(); _gameState.value=game }
        if(game.currentPlayer?.isAI==true && game.gamePhase==GamePhase.BIDDING) executeAIAction(game)
        return true
    }
    private fun isValidBid(bid:Int,team:Team?):Boolean{
        if(bid<2||bid>13) return false
        team?:return false
        val min=getMinimumBid(team.totalScore)
        return bid>=min
    }
    private fun getMinimumBid(score:Int) = when{
        score>=50->5
        score>=40->4
        score>=30->3
        else->2
    }
    private fun getMinimumTotalBid(game:TarneebGame):Int{
        val maxScore=maxOf(game.team1.totalScore,game.team2.totalScore)
        return when{
            maxScore>=50->14
            maxScore>=40->13
            maxScore>=30->12
            else->11
        }
    }
    private fun resetBidsAndRedeal(game:TarneebGame){
        game.team1.resetBid(); game.team2.resetBid(); game.players.forEach{it.bid=0}
        dealCards(game)
        game.startBidding()
        _gameState.value=game
        if(game.currentPlayer?.isAI==true) executeAIAction(game)
    }

    // ===================== PLAYING =====================
    fun playCard(playerId:Int,card:Card):Boolean{
        val game=_gameState.value?:return false
        if(game.gamePhase!=GamePhase.PLAYING){ _error.value="لا يمكن لعب ورقة الآن"; return false }
        val player=game.players.find{it.id==playerId}?:return false
        if(!canPlayCard(card,player,game)){ _error.value="ورقة غير صحيحة"; return false }
        if(game.tricks.isEmpty() || game.currentTrick?.isComplete(game.players.size)==true){ game.currentTrickNumber++; game.tricks.add(Trick(game.currentTrickNumber)) }
        val trick=game.currentTrick?:return false
        player.removeCard(card)
        trick.playCard(playerId,card)
        if(trick.isComplete(game.players.size)){
            val winnerId=calculateTrickWinner(trick)
            trick.winnerId=winnerId
            game.getTeamByPlayerId(winnerId)?.tricksWon = (game.getTeamByPlayerId(winnerId)?.tricksWon?:0)+1
            game.currentPlayerIndex=winnerId
            if(game.tricks.size==13){ endRound(game); return true }
        } else game.advancePlayer()
        _gameState.value=game
        if(game.currentPlayer?.isAI==true && game.gamePhase==GamePhase.PLAYING) executeAIAction(game)
        return true
    }
    private fun canPlayCard(card:Card,player:Player,game:TarneebGame):Boolean{
        if(!player.hand.contains(card)) return false
        val trick=game.currentTrick?:return true
        if(trick.cardsPlayed.isEmpty()) return true
        val s=trick.cardsPlayed.values.first().suit
        if(card.suit==s) return true
        if(player.hand.any{it.suit==s}) return false
        return true
    }
    fun getValidCards(playerId:Int):List<Card>{
        val game=_gameState.value?:return emptyList()
        val player=game.players.find{it.id==playerId}?:return emptyList()
        val trick=game.currentTrick?:return player.hand.toList()
        if(trick.cardsPlayed.isEmpty()) return player.hand.toList()
        val s=trick.cardsPlayed.values.first().suit
        val follow=player.hand.filter{it.suit==s}
        return if(follow.isNotEmpty()) follow else player.hand.toList()
    }
    private fun calculateTrickWinner(trick:Trick):Int{
        if(trick.cardsPlayed.isEmpty()) return -1
        var winnerId=trick.playOrder.first()
        var winningCard=trick.cardsPlayed[winnerId]!!
        trick.playOrder.drop(1).forEach{pid->
            val c=trick.cardsPlayed[pid]?:return@forEach
            if(c.suit==Suit.HEARTS && winningCard.suit!=Suit.HEARTS){ winnerId=pid; winningCard=c }
            else if(c.suit==Suit.HEARTS && winningCard.suit==Suit.HEARTS && c.rank.value>winningCard.rank.value){ winnerId=pid; winningCard=c }
            else if(c.suit==winningCard.suit && c.rank.value>winningCard.rank.value){ winnerId=pid; winningCard=c }
        }
        return winnerId
    }

    // ===================== ROUND & SCORING =====================
    private fun endRound(game:TarneebGame){
        game.players.forEach{p->
            val team=game.getTeamByPlayerId(p.id)
            val won=game.tricks.count{it.winnerId==p.id}
            val points=if(won>=p.bid) calculatePoints(p.bid,team?.totalScore?:0) else -p.bid
            team?.addScore(points)
        }
        if(game.team1.isWinner || game.team2.isWinner) game.endGame() else game.endRound()
        _gameState.value=game
    }
    private fun calculatePoints(bid:Int,currentScore:Int):Int{
        val table=if(currentScore>=30) mapOf(2 to 2,3 to 3,4 to 4,5 to 5,6 to 6,7 to 14,8 to 16,9 to 27,10 to 40,11 to 40,12 to 40,13 to 40)
        else mapOf(2 to 2,3 to 3,4 to 4,5 to 10,6 to 12,7 to 14,8 to 16,9 to 27,10 to 40,11 to 40,12 to 40,13 to 40)
        return table[bid]?:0
    }

    // ===================== AI =====================
    fun executeAIAction(game:TarneebGame){
        val player=game.currentPlayer?:return
        if(!player.isAI) return
        scope.launch {
            when(game.gamePhase){
                GamePhase.BIDDING ->{
                    val bid=decideBid(player,game)
                    _aiAction.value=AIAction.PlacingBid(player.id,bid)
                    delay(1500)
                    placeBid(player.id,bid)
                    _aiAction.value=null
                }
                GamePhase.PLAYING->{
                    val valid=getValidCards(player.id)
                    val card=decideCard(player,game,valid)
                    _aiAction.value=AIAction.PlayingCard(player.id,card)
                    delay(1000)
                    playCard(player.id,card)
                    _aiAction.value=null
                }
                else->Unit
            }
        }
    }

    private fun decideBid(player:Player,game:TarneebGame):Int{
        val min=getMinimumBid(game.getTeamByPlayerId(player.id)?.totalScore?:0)
        return when(player.difficulty){
            AIDifficulty.EASY->(min..13).random(random)
            AIDifficulty.MEDIUM->(min..13).random(random)
            AIDifficulty.HARD->(min..13).random(random)
        }
    }
    private fun decideCard(player:Player,game:TarneebGame,valid:List<Card>):Card = valid.random(random)

    // ===================== GAME MANAGEMENT =====================
    fun nextRound(){
        val game=_gameState.value?:return
        if(game.gamePhase==GamePhase.ROUND_END){
            game.startNextRound()
            dealCards(game)
            game.startBidding()
            _gameState.value=game
            if(game.currentPlayer?.isAI==true) executeAIAction(game)
        }
    }
    fun resetGame(){ _gameState.value=null; _error.value=null; _aiAction.value=null }
    fun getGameInfo() = _gameState.value?.getInfo()
    fun getPlayerHand(playerId:Int) = _gameState.value?.players?.find{it.id==playerId}?.hand?.toList()?:emptyList()
}
